package com.example.demo.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfiguration implements WebMvcConfigurer {

    @Value("http://${PUBLIC_DOMAIN}")
    private String publicDomain;

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins(publicDomain)
                .allowedMethods("*")
                .allowedHeaders("*")
                .allowCredentials(true);
    }
}

