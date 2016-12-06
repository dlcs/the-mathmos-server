package com.digirati.pygar.consumer.impl;

import static org.junit.Assert.*;
import static org.mockito.Mockito.*;

import java.io.File;
import java.io.IOException;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.DefaultResourceLoader;
import org.springframework.core.io.Resource;
import org.springframework.core.io.ResourceLoader;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;

import com.amazonaws.services.sqs.AmazonSQS;
import com.amazonaws.services.sqs.model.AmazonSQSException;
import com.amazonaws.services.sqs.model.ListQueuesResult;
import com.amazonaws.services.sqs.model.Message;
import com.amazonaws.services.sqs.model.MessageAttributeValue;
import com.amazonaws.services.sqs.model.ReceiveMessageRequest;
import com.amazonaws.services.sqs.model.ReceiveMessageResult;
import com.digirati.pygar.consumer.impl.AnnotationConsumerImpl;

public class AnnotationConsumerImplTest {

    
    AnnotationConsumerImpl annotationConsumerImpl;
    private AmazonSQS amazonSqs;
    
    private ElasticsearchTemplate template;
    
    private ResourceLoader resourceLoader;
    
    @BeforeClass
    public static void setUpBeforeClass() throws Exception {
    }

    @Before
    public void setUp() throws Exception {
	
	resourceLoader = new DefaultResourceLoader();
	try{
	annotationConsumerImpl = new AnnotationConsumerImpl("accessKey", "secretKey", "EU_WEST_1", "queueUrl");
	}catch (AmazonSQSException ase){
	    assertNotNull(ase);
	    System.out.println(ase);
	}
	amazonSqs = mock(AmazonSQS.class);
	ListQueuesResult listQueuesResult = mock(ListQueuesResult.class); 	
	when(amazonSqs.listQueues()).thenReturn(listQueuesResult);
	List<String> queuesList = new ArrayList<>();
	queuesList.add("queueUrl");
	when(listQueuesResult.getQueueUrls()).thenReturn(queuesList);
	
	annotationConsumerImpl = new AnnotationConsumerImpl("queueUrl", amazonSqs);  
	
    }

    @Test
    public void test() throws IOException {
	Message message = mock(Message.class);
	List<Message> messages = new ArrayList<>();
	messages.add(message);
	ReceiveMessageResult result = mock(ReceiveMessageResult.class);
	when(result.getMessages()).thenReturn(messages);
	when(amazonSqs.receiveMessage((ReceiveMessageRequest) anyObject())).thenReturn(result);
	
	when (message.getMessageId()).thenReturn("getMessageId");
	when(message.getReceiptHandle()).thenReturn("getReceiptHandle");
	when(message.getMD5OfBody()).thenReturn("getMD5OfBody");
	
	String body = getFileContents("test-message-1.json");
	when(message.getBody()).thenReturn(body);
	
	Map <String, MessageAttributeValue> map = new HashMap<>();
	
	MessageAttributeValue createValue = new MessageAttributeValue();
	createValue.setStringValue("CREATE");
	
	MessageAttributeValue iriValue = new MessageAttributeValue();
	iriValue.setStringValue("http://www.example.com.anno1");
	
	map.put("operation", createValue);
	map.put("iri", iriValue);
	
	when(message.getMessageAttributes()).thenReturn(map);
	
	
	annotationConsumerImpl.initialiseListener();
	
	createValue.setStringValue("UPDATE");
	annotationConsumerImpl.initialiseListener();
	
	createValue.setStringValue("DELETE");
	annotationConsumerImpl.initialiseListener();
    }
    
    
    
    static String readFile(String path, Charset encoding) throws IOException {
   	byte[] encoded = Files.readAllBytes(Paths.get(path));
   	return new String(encoded, encoding);
       }
       
       public String getFileContents(String filename) throws IOException{
   	String testmessage = "classpath:free/"+filename;
   	Resource resource =  resourceLoader.getResource(testmessage);
   	File resourcefile = resource.getFile();
   	return readFile(resourcefile.getPath(), StandardCharsets.UTF_8);
       }

}
