package com.digirati.pygar.mapping;

import org.apache.log4j.Logger;
import org.springframework.context.ConfigurableApplicationContext;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.support.ClassPathXmlApplicationContext;



@Configuration
public class IndexAWSMessages {
    
    private static final Logger LOG = Logger.getLogger(IndexAWSMessages.class);
  
    public IndexAWSMessages(){}

    @SuppressWarnings("resource")
    public static void main (String args[]){
	
	String XML_APPLICATION_CONTEXT = "classpath:/applicationContext.xml";

	LOG.info(String.format("Initialising classpath XML application with [%s]", XML_APPLICATION_CONTEXT));
	ConfigurableApplicationContext applicationContext = new ClassPathXmlApplicationContext(XML_APPLICATION_CONTEXT);
	
	//LOGGER.info(String.format("Initialising annotation config application with [%s]", MainConfig.class));
        //new AnnotationConfigApplicationContext(MainConfig.class);

    }

}
