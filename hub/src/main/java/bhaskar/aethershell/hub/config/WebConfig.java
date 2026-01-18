package bhaskar.aethershell.hub.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.ResourceHandlerRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;
import java.io.File;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    @Override
    public void addResourceHandlers(ResourceHandlerRegistry registry) {

        String userDir = System.getProperty("user.dir");
        String finalPath = "file:" + userDir + "/static/";

        System.out.println("SERVERSIDE CHECK: Serving images from: " + finalPath);

        registry.addResourceHandler("/output/**")
                .addResourceLocations(finalPath + "output/")
                .setCachePeriod(0);
    }
    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}