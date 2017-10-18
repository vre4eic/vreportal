package forth.ics.isl;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
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
public class Application implements CommandLineRunner {
	
	@Autowired
    DataSource dataSource;
	
    public static void main(String[] args) throws Exception {
        SpringApplication.run(Application.class, args);
    }
    
    public void run(String... args) throws Exception {

        System.out.println("DATASOURCE = " + dataSource);

    }

    
}