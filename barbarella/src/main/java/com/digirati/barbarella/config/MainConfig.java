package com.digirati.barbarella.config;


import org.apache.log4j.Logger;
import org.elasticsearch.client.Client;

import org.springframework.data.elasticsearch.client.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.config.MethodInvokingFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.core.env.Environment;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.data.elasticsearch.repository.config.EnableElasticsearchRepositories;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.Log4jConfigurer;
import org.springframework.util.MethodInvoker;


@Configuration
@EnableScheduling
@SuppressWarnings("deprecation")
@PropertySource("${barbarella-aws-consumer.properties}")
@ComponentScan(basePackages = {MainConfig.SERVICE_PACKAGE})
@EnableElasticsearchRepositories(basePackages = MainConfig.REPOSITORY_PACKAGE)

public class MainConfig {
    
    private static final Logger LOG = Logger.getLogger(MainConfig.class);

    
    public static final String SERVICE_PACKAGE = "com.digirati.barbarella";
    
    public static final String REPOSITORY_PACKAGE = "com.digirati.barbarella.repository";
    

    @Autowired
    private Environment environment;

    @Bean(name = "log4jInitialization")
    public MethodInvoker log4j() {
        MethodInvokingFactoryBean methodInvoker = new MethodInvokingFactoryBean();
        methodInvoker.setTargetClass(Log4jConfigurer.class);
        methodInvoker.setTargetMethod("initLogging");
        methodInvoker.setArguments(getLog4jArgs());
        return methodInvoker;
    }
    
    @Bean(name = "elasticsearchTemplate")
    public ElasticsearchTemplate template() {
	
	return new ElasticsearchTemplate(createTransportClient());
    }
    
    
    private Client createTransportClient()  {
	    TransportClientFactoryBean factory = new TransportClientFactoryBean();
	    factory.setClusterNodes(environment.getRequiredProperty("cluster.nodes"));
	    try {
		factory.afterPropertiesSet();
	    } catch (Exception e) {
		LOG.error("Error calling afterPropertiesSet TransportClientFactoryBean", e);
	    }
	    try {
		return factory.getObject();
	    } catch (Exception e) {
		LOG.error("Error getting transportclient from TransportClientFactoryBean", e);
	    }
	    return null;
	  }
    

    private Object[] getLog4jArgs() {
        return new Object[] {environment.getRequiredProperty("log4j.config.location"), environment.getRequiredProperty("log4j.refresh.interval")};
    }
}
