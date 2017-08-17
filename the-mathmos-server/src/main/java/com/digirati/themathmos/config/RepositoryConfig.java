package com.digirati.themathmos.config;


import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;


@Configuration
@ComponentScan(basePackages = {RepositoryConfig.REPOSITORY_PACKAGE})
public class RepositoryConfig {

    public static final String REPOSITORY_PACKAGE = "com.digirati.themathmos.repository";


}
