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
        // 1. Get the current working directory
        String userDir = System.getProperty("user.dir");

        // 2. Logic to find the 'output' folder correctly
        String path;
        if (userDir.endsWith("hub")) {
            path = userDir + "/src/main/resources/static/output/";
        } else {
            path = userDir + "/hub/src/main/resources/static/output/";
        }

        // 3. Ensure the string starts with 'file:' and ends with a slash
        String finalPath = "file:" + path;

        // This log will appear in your IntelliJ console.
        // IMPORTANT: Copy this path and verify it exists on your Mac!
        System.out.println("DEBUG: AetherShell is serving images from: " + finalPath);

        registry.addResourceHandler("/output/**")
                .addResourceLocations(finalPath)
                .setCachePeriod(0); // This is vital for seeing new frames immediately
    }

    @Override
    public void addCorsMappings(CorsRegistry registry) {
        registry.addMapping("/**")
                .allowedOrigins("*")
                .allowedMethods("GET", "POST", "OPTIONS");
    }
}