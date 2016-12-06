package com.digirati.pygar.consumer.impl;


import java.nio.file.FileSystems;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Map.Entry;

import org.apache.commons.lang3.StringUtils;
import org.apache.log4j.Logger;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.core.query.IndexQuery;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import com.amazonaws.AmazonClientException;
import com.amazonaws.AmazonServiceException;
import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.regions.Region;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.AmazonSQSClient;
import com.amazonaws.services.sqs.model.DeleteMessageRequest;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.digirati.pygar.W3CSearchAnnotation;
import com.digirati.pygar.consumer.AnnotationConsumer;
import com.digirati.pygar.indexer.exception.IndexingException;
import com.digirati.pygar.mapping.AnnotationMappingUtils;
import com.digirati.pygar.mapping.BodyTargetFieldData;
import com.digirati.pygar.repository.W3CAnnotationSearchRepository;

@Component(AnnotationConsumerImpl.COMPONENT_NAME)
public class AnnotationConsumerImpl implements AnnotationConsumer {

    private static final Logger LOG = Logger.getLogger(AnnotationConsumerImpl.class);

    public static final String COMPONENT_NAME = "annotationConsumer";

    private String queueUrl;
    private AmazonSQS amazonSqs;
    private AnnotationMappingUtils mappingUtils;

    private ReceiveMessageRequest receiveMessageRequest;
    
    @Autowired
    private W3CAnnotationSearchRepository repository;

    @Autowired
    private ElasticsearchTemplate template;
    
    @Autowired
    private ConfigurableApplicationContext applicationContext;

    @Autowired
    public AnnotationConsumerImpl(
	    @Value("${aws.sqs.accessKey}") String accessKey,
	    @Value("${aws.sqs.secretKey}") String secretKey,
	    @Value("${aws.sqs.region}") String region,
	    @Value("${aws.sqs.queueUrl}") String queueUrl) {
	this.queueUrl = queueUrl;
	this.amazonSqs = buildAmazonSqs(accessKey, secretKey, region);
	if (!validateQueueExists(queueUrl)) {
	    LOG.error("Unable to find the queue");
	    throw new IndexingException(String.format("Target AWS SQS queue [%s] not found", queueUrl));
	}
	mappingUtils = new AnnotationMappingUtils();
	receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
	receiveMessageRequest.withWaitTimeSeconds(10);
	receiveMessageRequest.withAttributeNames("All");
	receiveMessageRequest.withMessageAttributeNames("All");
    }
    
    
    public AnnotationConsumerImpl(String queueUrl,AmazonSQS  amazonSqs) {
	this.queueUrl = queueUrl;
	this.amazonSqs = amazonSqs;
	mappingUtils = new AnnotationMappingUtils();
	receiveMessageRequest = new ReceiveMessageRequest(queueUrl);
	receiveMessageRequest.withWaitTimeSeconds(10);
	receiveMessageRequest.withAttributeNames("All");
	receiveMessageRequest.withMessageAttributeNames("All");
    }

    @Override
    @Scheduled(initialDelay = 1, fixedDelay = 1)
    public void initialiseListener() {

	try {
	    readQueue();
	} catch (AmazonServiceException ase) {
	    LOG.error(
		    "Caught an AmazonServiceException, which means your request made it to Amazon SQS, but was rejected with an error response for some reason.", ase);
	    LOG.error(String.format("\t-> Error Message: [%s]", ase.getMessage()));
	    LOG.error(String.format("\t-> HTTP Status Code: [%s]", ase.getStatusCode()));
	    LOG.error(String.format("\t-> AWS Error Code: [%s]", ase.getErrorCode()));
	    LOG.error(String.format("\t-> Error Type: [%s]", ase.getErrorType()));
	    LOG.error(String.format("\t-> Request ID: [%s]", ase.getRequestId()));
	} catch (AmazonClientException ace) {
	    LOG.error(
		    "Caught an AmazonClientException, which means the client encountered a serious internal problem while trying to communicate with SQS, such as not being able to access the network.", ace);
	    LOG.error(String.format("\t-> Error Message: [%s]", ace.getMessage()));
	} catch (Exception e) {
	    LOG.error(String.format("An unexpected error occurred reading from AWS SQS queue [%s]", queueUrl), e);
	} 
    }

    private void readQueue() {
	

	List<Message> messages = amazonSqs.receiveMessage(receiveMessageRequest).getMessages();
	LOG.info(String.format("Got [%s] messages from AWS SQS queue [%s]:", messages.size(), queueUrl));

	for (Message message : messages) {
	    LOG.info("\t-> Messages:");
	    LOG.info(String.format("\t\t--> MessageId: [%s]", message.getMessageId()));
	    LOG.info(String.format("\t\t--> ReceiptHandle: [%s]", message.getReceiptHandle()));
	    LOG.info(String.format("\t\t--> MD5OfBody: [%s]", message.getMD5OfBody()));
	    LOG.info(String.format("\t\t--> Body: [%s]", message.getBody()));

	    for (Entry<String, String> entry : message.getAttributes().entrySet()) {
		LOG.info("\t\t--> Attributes:");
		LOG.info(String.format("\t\t\t---> [%s] : [%s]", entry.getKey(), entry.getValue()));
	    }
	    for (Entry<String, MessageAttributeValue> entry : message.getMessageAttributes().entrySet()) {
		LOG.info("\t\t--> Message Attributes:");

		LOG.info(String.format("\t\t\t---> [%s] : [%s]", entry.getKey(), entry.getValue().getStringValue()));
	    }

	    String operation = null;
	    String iri = null;

	    if (message.getMessageAttributes().containsKey("operation")) {
		operation = message.getMessageAttributes().get("operation").getStringValue();
		LOG.info("operation: " + operation);
	    }
	    if (message.getMessageAttributes().containsKey("iri")) {
		iri = message.getMessageAttributes().get("iri").getStringValue();
		LOG.info("iri: " + iri);
	    }

	    if ("CREATE".equals(operation) || "UPDATE".equalsIgnoreCase(operation)) {
		String body = message.getBody();
		LOG.info("body: " + body);
		if (null != body) {
		    BodyTargetFieldData bodyTargetFieldData = mappingUtils.determineJsonMappingType(body);
		    if (null != bodyTargetFieldData) {

			W3CSearchAnnotation annotation = mappingUtils.addAnnotations(bodyTargetFieldData.getFieldData(),
				bodyTargetFieldData.getTargetFieldData());
			try{
			    	template.createIndex(W3CSearchAnnotation.class);
        			template.putMapping(W3CSearchAnnotation.class);
        			IndexQuery indexQuery = new IndexQuery();
        			indexQuery.setId(annotation.getId());
        			indexQuery.setObject(annotation);
        			template.index(indexQuery);
        			template.refresh(W3CSearchAnnotation.class);
        
        			repository.save(annotation);
			}catch (Exception e){
			    LOG.error("Error within indexing " +e);
			}

			LOG.info(String.format("[%s] performed for [%s] with iri [%s]", operation,
				message.getMessageId(), iri));
		    }
		}
	    }
	    if ("DELETE".equalsIgnoreCase(operation)) {
		try{
		    repository.delete(iri);
		    LOG.info(String.format("[%s] performed for [%s] with iri [%s]", operation, message.getMessageId(),iri));
		}catch (Exception e){
		    LOG.error(String.format("[%s] not performed for [%s] with iri [%s]", operation, message.getMessageId(),iri), e);
		}
		
	    }

	    String messageReceiptHandle = messages.get(0).getReceiptHandle();
	    LOG.info(String.format("Deleting message [%s] with receipt handle [%s]", message.getMessageId(),
		    messageReceiptHandle));
	    amazonSqs.deleteMessage(new DeleteMessageRequest(queueUrl,messageReceiptHandle));
	}
    }

    private AmazonSQS buildAmazonSqs(String accessKey, String secretKey, String region) {
	AWSCredentials awsCredentials = new BasicAWSCredentials(accessKey, secretKey);
	AmazonSQS amazonSQS = new AmazonSQSClient(awsCredentials);
	amazonSQS.setRegion(Region.getRegion(Regions.valueOf(region)));
	return amazonSQS;
    }

    private boolean validateQueueExists(String queueUrl) {
	for (String targetQueueUrl : amazonSqs.listQueues().getQueueUrls()) {
	    if (StringUtils.equalsIgnoreCase(queueUrl, targetQueueUrl)) {
		return true;
	    }
	}
	return false;
    }
    
    @Override
    @Scheduled(initialDelay = 1000, fixedDelay = 1000)
    public void initialiseStopListener() {
	
	//TODO the default path is /home/digirati.. so this solution is not going to work when we add another environment(if we ever do!)
	Path path = FileSystems.getDefault().getPath("stopFile.txt");
	LOG.info("path "  + path.toAbsolutePath());
	if(Files.exists(path)){
	    LOG.info("found stopfile on path "  + path.toAbsolutePath());
	    LOG.info("closing applicationContext: " + applicationContext);
	    applicationContext.close();
	    
	}
    }

    
}
