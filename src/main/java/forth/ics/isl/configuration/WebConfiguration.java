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
    
    
    // To access the H2 Web Console use:
    // http://localhost:8080/console
    //
    // If you got the message "Sorry, remote connections ('webAllowOthers') are disabled on this server",
    // Then you need to enable remote access.
    //
    // To enable remote access edit the file .h2.server.properties 
    // by changing property 'webAllowOthers' from false to true
    // The file .h2.server.properties is located in the home folder 
    // of the user that executed the jar (i.e. widows machine example
    // 'C:\Users\vkrits\.h2.server.properties', UNIX example: '/home/vkrits/.h2.server.properties')
    // 
    // If it doesn't exist you can create it yourself.
    //
    // The contents of the file look like this (the numbered lines are preset settings in the respectibe web component inputs):
    //
    // #H2 Server Properties
    // #Tue Sep 25 11:40:59 EEST 2018
    // 0=Generic JNDI Data Source|javax.naming.InitialContext|java\:comp/env/jdbc/Test|sa
    // 1=Generic Firebird Server|org.firebirdsql.jdbc.FBDriver|jdbc\:firebirdsql\:localhost\:c\:/temp/firebird/test|sysdba
    // 10=Generic Derby (Server)|org.apache.derby.jdbc.ClientDriver|jdbc\:derby\://localhost\:1527/test;create\=true|sa
    // 11=Generic Derby (Embedded)|org.apache.derby.jdbc.EmbeddedDriver|jdbc\:derby\:test;create\=true|sa
    // 12=Generic H2 (Server)|org.h2.Driver|jdbc\:h2\:tcp\://localhost/~/evre|sa
    // 13=Generic H2 (Embedded)|org.h2.Driver|jdbc\:h2\:~/evrepaper|sa
    // 2=Generic SQLite|org.sqlite.JDBC|jdbc\:sqlite\:test|sa
    // 3=Generic DB2|com.ibm.db2.jcc.DB2Driver|jdbc\:db2\://localhost/test|
    // 4=Generic Oracle|oracle.jdbc.driver.OracleDriver|jdbc\:oracle\:thin\:@localhost\:1521\:XE|sa
    // 5=Generic MS SQL Server 2000|com.microsoft.jdbc.sqlserver.SQLServerDriver|jdbc\:microsoft\:sqlserver\://localhost\:1433;DatabaseName\=sqlexpress|sa
    // 6=Generic MS SQL Server 2005|com.microsoft.sqlserver.jdbc.SQLServerDriver|jdbc\:sqlserver\://localhost;DatabaseName\=test|sa
    // 7=Generic PostgreSQL|org.postgresql.Driver|jdbc\:postgresql\:test|
    // 8=Generic MySQL|com.mysql.jdbc.Driver|jdbc\:mysql\://localhost\:3306/test|
    // 9=Generic HSQLDB|org.hsqldb.jdbcDriver|jdbc\:hsqldb\:test;hsqldb.default_table_type\=cached|sa
    // webAllowOthers=true
    // webPort=8082
    // webSSL=false

}
