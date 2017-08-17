package com.digirati.themathmos.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Import;




@Configuration
@Import({MVCConfig.class, ServicesConfig.class, RepositoryConfig.class})
public class MainConfig {



}
