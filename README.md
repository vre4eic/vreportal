<img src="https://github.com/vre4eic/vreportal/blob/master/src/main/resources/static/images/AQub.2Qubes.v2.png" width="100">

# VRE4EIC Portal - AQuB
This is a platform that facilitates the exploration, discovery and management of semantic metadata. It incorporates a multitude of features on top of an intuitive and user friendly environment, in order for both novice and expert users to execute complex queries. The platform is agnostic to the underlying conceptual model, yet it can be configured to take advantage of the main concepts designed.

## Technologies Used

- **Spring Boot** – A project built on the top of the Spring framework. It provides a simpler and faster way to set up, configure, and run both simple and web-based (Spring Web MVC) applications;
- **AngularJS** - A structural framework for dynamic web apps based on HTML and JavaScript;
- **Material Design & Bootstrap-UI** - UI component frameworks;
- **H2** – A relational database management system written in Java, that can be embedded in Java applications;

## Requirements
Regarding the execution environment, only Java 8 or above has to be installed.

The VRE4EIC Portal communicates with two independent components that need to be running.
These components are:
-	[EVREMetadata Services](https://github.com/vre4eic/EVREMetadataServices), responsible for querying/importing data; and
-	[Node Service](https://github.com/vre4eic/NodeService), responsible for the user authentication, RBAC and user profiling 

For that communication to be achieved, a minimum configuration, to be applied on two property files, is required. These property files are i) [application.property](https://github.com/vre4eic/vreportal/blob/master/src/main/resources/application.properties), and ii) [config.property](https://github.com/vre4eic/vreportal/blob/master/src/main/resources/config.properties)

As an initial and minimal configuration, please edit the values at the properties

#### config.property:
- **service.url** (the URI of the EVREMetadataServices component)
- **uri.prefix** (the prefix of the URIs of the entities that should be resolvable through the URI Reslover Component. A similar configuration parameter is available for that component and the two configuration parameters should actually match)
- **portal.state** (can be either “public” denoting that this is a public running instance where users cannot set their own user roles or “private” denoting that this is an instance where users can set their own user roles. “public” should be used in most cases)
#### application.property:
- **spring.datasource.url** (the URL of the H2 database where to connect (i.e. jdbc:h2:~/evre;DB_CLOSE_ON_EXIT=FALSE))

## Installation Instructions
Since this is a maven web application, it can easily be deployed by first creating the big fat JAR and then executing it. The application includes an embedded Jetty server container and thus requires nothing more. Finally, the portal uses an external H2 lightweight relational SQL database for managing portal’s and users’ configuration options.
Steps:

1. Create the H2 database by executing the java file by running the java command:
java H2Manager.java
2. The JAR can be created by running the maven command 
mvn package
3.	The JAR is executed by running the Java command
java -jar <Name_Of_The_JAR.jar 
The port to be used can also be defined by adding the parameter “server.port”
For example:
java -jar vreportal-0.0.1-SNAPSHOT.jar --server.port=8099

