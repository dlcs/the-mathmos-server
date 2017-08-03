package com.digirati.themathmos.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.EnableWebMvc;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurationSupport;



@EnableWebMvc
@Configuration
@ComponentScan(basePackages = MVCConfig.WEB_PACKAGE)
public class MVCConfig extends WebMvcConfigurationSupport {

    public static final String WEB_PACKAGE = "com.digirati.themathmos.web.controller";

    
}
