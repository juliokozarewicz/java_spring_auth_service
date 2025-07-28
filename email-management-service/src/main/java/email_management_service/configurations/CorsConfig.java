package email_management_service.configurations;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.CorsRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

@Configuration
public class CorsConfig implements WebMvcConfigurer {

    @Value("${PUBLIC_DOMAIN}")
    private String publicDomain;

    @Override
    public void addCorsMappings(CorsRegistry registry) {

        String[] allowedOrigins = publicDomain.split(",");

        for (int i = 0; i < allowedOrigins.length; i++) {
            String origin = allowedOrigins[i].trim();

            if (!origin.startsWith("http://") && !origin.startsWith("https://")) {
                allowedOrigins[i] = "http://" + origin;
            }
        }

        registry.addMapping("/**")
            .allowedOrigins(allowedOrigins)
            .allowedMethods("*")
            .allowedHeaders("*")
            .allowCredentials(true);
    }

}