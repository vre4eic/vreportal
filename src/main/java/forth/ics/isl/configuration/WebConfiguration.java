package forth.ics.isl.configuration;

import org.h2.server.web.WebServlet;
import org.springframework.boot.web.servlet.ServletRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class WebConfiguration {
    @Bean
    ServletRegistrationBean h2servletRegistration(){
        ServletRegistrationBean registrationBean = new ServletRegistrationBean( new WebServlet());
        registrationBean.addUrlMappings("/console/*");
        return registrationBean;
    }
    // Use:
    // http://localhost:8080/console
    // To enable remote access edit the file .h2.server.properties 
    // by changing property 'webAllowOthers' from false to true
    // The file .h2.server.properties is located in the home folder 
    // of the user that executed the jar (i.e. widows machine example
    // 'C:\Users\vkrits') (respectively for unix)
}
