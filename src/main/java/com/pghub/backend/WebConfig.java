package com.pghub.backend;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.nio.file.Paths;
import java.nio.file.Path;


@Configuration
public class WebConfig implements WebMvcConfigurer{
    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry){
        Path uploadDir= Paths.get("./uploads");
        String uploadPath = uploadDir.toFile().getAbsolutePath();
        
        // 3. EXPLANATION BELOW
        registry.addResourceHandler("/uploads/**")
                .addResourceLocations("file:/" + uploadPath + "/");
    }
    
}
