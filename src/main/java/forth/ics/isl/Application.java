/*******************************************************************************
 * Copyright (c) 2018 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

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