package com.e.commerce.mini.config;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Value("${app.upload.dir:uploads/products}")
    private String uploadDirectory;

    @Override
    public void addResourceHandlers(
            ResourceHandlerRegistry registry
    ) {

        String resourceLocation =
                uploadDirectory.endsWith("/")
                        ? uploadDirectory
                        : uploadDirectory + "/";

        registry
                .addResourceHandler(
                        "/uploads/products/**"
                )
                .addResourceLocations(
                        "file:" + resourceLocation
                );
    }
}