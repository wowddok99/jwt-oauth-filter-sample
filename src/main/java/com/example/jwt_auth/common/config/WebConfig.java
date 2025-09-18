package com.example.jwt_auth.common.config;

import lombok.NonNull;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.net.URI;
import java.nio.file.Paths;

@Configuration
@RequiredArgsConstructor
public class WebConfig implements WebMvcConfigurer {

    private final ImageUploadProperties imageUploadProperties;

    @Override
    public void addResourceHandlers(@NonNull ResourceHandlerRegistry registry) {
        imageUploadProperties.targets().forEach((targetName, targetProps) -> {
            String baseUrl = targetProps.baseUrl();
            String directory = targetProps.directory();

            try {
                URI uri = new URI(baseUrl);
                String pathPattern = uri.getPath() + "/**";
                String location = Paths.get(directory).toUri().toString();

                registry.addResourceHandler(pathPattern)
                        .addResourceLocations(location);

            } catch (Exception e) {
                throw new RuntimeException("Failed to register resource handler for target: " + targetName, e);
            }
        });
    }
}