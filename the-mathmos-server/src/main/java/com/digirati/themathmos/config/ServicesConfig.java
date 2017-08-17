package com.digirati.themathmos.config;
import static org.apache.commons.lang.StringUtils.*;

import java.net.InetAddress;
import java.net.UnknownHostException;

import org.apache.log4j.Logger;

import org.elasticsearch.client.Client;
import org.elasticsearch.client.transport.TransportClient;
import org.elasticsearch.common.settings.Settings;
import org.elasticsearch.common.transport.InetSocketTransportAddress;
import org.elasticsearch.transport.client.PreBuiltTransportClient;
import org.springframework.beans.factory.annotation.Autowired;

import org.springframework.cache.CacheManager;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.cache.ehcache.EhCacheCacheManager;
import org.springframework.cache.ehcache.EhCacheManagerFactoryBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.env.Environment;
import org.springframework.core.io.ClassPathResource;
import org.springframework.data.elasticsearch.client.TransportClientFactoryBean;
import org.springframework.data.elasticsearch.core.ElasticsearchTemplate;
import org.springframework.scheduling.annotation.EnableScheduling;
import org.springframework.util.Assert;



@Configuration
@EnableCaching
@EnableScheduling
@ComponentScan(basePackages = {ServicesConfig.SERVICE_PACKAGE})


public class ServicesConfig {

    private static final Logger LOG = Logger.getLogger(MainConfig.class);


    public static final String SERVICE_PACKAGE = "com.digirati.themathmos.service";

    static final String COLON = ":";
    static final String COMMA = ",";


    @Autowired
    private Environment environment;



    @Bean(name = "elasticsearchTemplate")
    public ElasticsearchTemplate template() throws Exception {

	return new ElasticsearchTemplate(createTransportClient());
    }

    private Client createTransportClient()  {
	    TransportClientFactoryBean factory = new TransportClientFactoryBean();
	    factory.setClusterNodes(environment.getRequiredProperty("cluster.nodes"));
	    factory.setClusterName(environment.getRequiredProperty("cluster.name"));
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


  /*
    private Client createTransportClient() throws Exception  {
	    Assert.hasText(environment.getRequiredProperty("cluster.nodes"), "[Assertion failed] clusterNodes settings missing.");

	    Settings settings = Settings.builder().put("cluster.name", environment.getRequiredProperty("cluster.name")).build();

	    TransportClient client = new PreBuiltTransportClient(settings);
	    for (String clusterNode : split(environment.getRequiredProperty("cluster.nodes"), COMMA)) {
		String hostName = substringBeforeLast(clusterNode, COLON);
		String port = substringAfterLast(clusterNode, COLON);
		Assert.hasText(hostName, "[Assertion failed] missing host name in 'clusterNodes'");
		Assert.hasText(port, "[Assertion failed] missing port in 'clusterNodes'");
		LOG.info("adding transport node : " + clusterNode);
		client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName(hostName), Integer.valueOf(port)));
	}
	    try {

		client.addTransportAddress(new InetSocketTransportAddress(InetAddress.getByName("localhost"), 9300));
		LOG.info("Transport Addresses: "+client.transportAddresses());
		return client;
	    } catch (UnknownHostException e1) {
		LOG.error("Error getting transportclient from PreBuiltTransportClient", e1);
		return null;

	    }

	 }*/

        @Bean(name = "cacheManager")
	public CacheManager cacheManager() {
		return new EhCacheCacheManager(ehCacheCacheManager().getObject());
	}

	@Bean
	public EhCacheManagerFactoryBean ehCacheCacheManager() {
		EhCacheManagerFactoryBean ehcache = new EhCacheManagerFactoryBean();
		ehcache.setConfigLocation(new ClassPathResource("ehcache.xml"));
		ehcache.setShared(true);
		return ehcache;
	}
}
