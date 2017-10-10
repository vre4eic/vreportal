package forth.ics.isl;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * SparQL Endpoint client application for blazegraph using SpringMVC and AngulaJS
 * 
 * URL: 
 * 	http://localhost:8080/index.html#!/login
 * 	http://localhost:8080/index.html
 * 	http://localhost:8080/index.html#!/tabs/query
 * 
 * @author Vangelis Kritsotakis
 */

@SpringBootApplication
public class Application {

    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }

}