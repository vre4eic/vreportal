package forth.ics.isl.configuration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;
import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;

/**
 * Configuration class, loading properties from a property file
 * 
 * @author Vangelis Kritsotakis
 */

// or i.e. for many packages use @ComponentScan({"forth.ics.isl.configuration", "forth.ics.isl.service"})

@Configuration
@ComponentScan(basePackages = "forth.ics.isl.configuration")
@PropertySource(value = { "classpath:config.properties" })
public class AppConfig {
 
    /*
     * PropertySourcesPlaceHolderConfigurer Bean only required for @Value("{}") annotations.
     * Remove this bean if you are not using @Value annotations for injecting properties.
     */
    @Bean
    public static PropertySourcesPlaceholderConfigurer propertySourcesPlaceholderConfigurer() {
        return new PropertySourcesPlaceholderConfigurer();
    }
    
}
