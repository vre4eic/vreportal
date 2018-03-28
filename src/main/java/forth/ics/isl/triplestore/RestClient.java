package forth.ics.isl.triplestore;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import javax.ws.rs.ClientErrorException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONObject;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import org.json.simple.JSONArray;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import org.slf4j.LoggerFactory;

/**
 * A client for our Custom Restful Web service for the triple store
 *
 * @author Vangelis Kritsotakis
 *
 */
public class RestClient {

    private String serviceUrl;
    private String namespace;
    private String authorizationToken;

    public RestClient(String serviceUrl, String namespace, String authorizationToken) throws IOException {
        // Be put in comments due to conflicts with log4j when creating the fat jar
        Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
        for (String log : loggers) {
            Logger logger = (Logger) LoggerFactory.getLogger(log);
            logger.setLevel(Level.INFO);
            logger.setAdditive(false);
        }
        ///
        this.serviceUrl = serviceUrl;
        this.namespace = namespace;
        this.authorizationToken = authorizationToken;
    }

    public RestClient(String serviceUrl, String namespace) throws IOException {
        // Be put in comments due to conflicts with log4j when creating the fat jar
//        Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
//        for (String log : loggers) {
//            Logger logger = (Logger) LoggerFactory.getLogger(log);
//            logger.setLevel(Level.INFO);
//            logger.setAdditive(false);
//        }
        ///
        this.serviceUrl = serviceUrl;
        this.namespace = namespace;
    }

    /**
     * Imports an RDF like file on the server using post synchronously
     *
     * @param file A String holding the path of the file, the contents of which
     * will be uploaded.
     * @param format	The RDF format
     * @param namespace A String representation of the nameSpace
     * @param namedGraph A String representation of the nameGraph
     * @return A response from the service.
     */
    public Response importFile(String content, String format, String namespace, String namedGraph, String authorizationToken)
            throws ClientProtocolException, IOException {
        String restURL;
        // Taking into account nameSpace in the construction of the URL
        if (namespace != null) {
            restURL = serviceUrl + "/import/namespace/" + namespace;
        } else {
            restURL = serviceUrl + "/import";
        }
        // Taking into account nameGraph in the construction of the URL
        if (namedGraph != null && !namedGraph.isEmpty()) {
            restURL = restURL + "?graph=" + namedGraph;
        }
        System.out.println("restURL: " + restURL);

        String mimeType = format;
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(restURL).queryParam("graph", namedGraph);
        System.out.println("authorizationToken NEW: " + authorizationToken);
        Response response = webTarget.request()
                .header("Authorization", authorizationToken)
                .post(Entity.entity(content, mimeType));
        return response;
    }

    /**
     * Imports an RDF-like file on the server
     *
     * @param queryStr A String that holds the query to be submitted on the
     * server.
     * @param namespace A String representation of the nameSpace to be used
     * @param format
     * @return The output of the query
     */
    public Response executeSparqlQuery(String queryStr, String namespace, int timeout, String format, String authorizationToken) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serviceUrl + "/query/namespace/" + namespace)
                .queryParam("timeout", timeout)
                .queryParam("format", format)
                .queryParam("query", URLEncoder.encode(queryStr, "UTF-8").replaceAll("\\+", "%20"));
        //System.out.println("HttpHeaders.AUTHORIZATION: " + authorizationToken);
        Invocation.Builder invocationBuilder = webTarget.request().header("Authorization", authorizationToken);
        Response response = invocationBuilder.get();
//        System.out.println(response.getStatus());
        return response;
    }

    public Response executeSparqlQuery(String queryStr, String format, int timeout) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serviceUrl + "/query/namespace/" + namespace)
                .queryParam("format", format)
                .queryParam("timeout", timeout)
                .queryParam("query", URLEncoder.encode(queryStr, "UTF-8").replaceAll("\\+", "%20"));
        //System.out.println("HttpHeaders.AUTHORIZATION: " + authorizationToken);
        Invocation.Builder invocationBuilder = webTarget.request().header("Authorization", authorizationToken);
        Response response = invocationBuilder.get();
        return response;
    }

    public Response executeBatchSparqlQueryPOST(JSONArray queries, String format) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        JSONObject json = new JSONObject();
        json.put("query", queries.toJSONString());
        json.put("format", format);
        WebTarget webTarget = client.target(serviceUrl + "/query/batch/namespace/" + namespace);
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", authorizationToken).post(Entity.json(json.toJSONString()));
    }

    public Response executeUpdatePOSTJSON(String update, String namespace, String token) throws ClientErrorException {
        Client client = ClientBuilder.newClient();
        JSONObject json = new JSONObject();
        json.put("query", update);
        WebTarget webTarget = client.target(serviceUrl + "/update/namespace/" + namespace);
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", token).post(Entity.json(json.toJSONString()));
    }

    public Response executeUpdatePOSTJSON(String updateQuery) throws ClientErrorException {
        Client client = ClientBuilder.newClient();
        JSONObject json = new JSONObject();
        json.put("query", updateQuery);
        WebTarget webTarget = client.target(serviceUrl + "/update/namespace/" + namespace);
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", authorizationToken).post(Entity.json(json.toJSONString()));
    }

    public static void main(String[] args) throws IOException {
        String query = "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) from <http://ekt-data> from <http://rcuk-data> from <http://fris-data> from <http://epos-data> from <http://envri-data>  where {\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?org <http://eurocris.org/ontology/cerif#has_name> ?orgName.\n"
                + "?org <http://eurocris.org/ontology/cerif#has_acronym> ?orgAcronym.\n"
                + "{\n"
                + "?pers_0 <http://eurocris.org/ontology/cerif#Person-OrganisationUnit/is%20member%20of> ?org_1.\n"
                + "} UNION {\n"
                + "?pers_0 <http://eurocris.org/ontology/cerif#Person-Publication/is%20author%20of> ?pub_2.\n"
                + "}\n"
                + "}";

        String authorizationToken = "1aa6b0df-c75d-4dc9-bdba-7a871467a64c";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
//        endpoint = "http://139.91.183.48:8181/EVREMetadataServices";
        String namespace = "vre4eictest";
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
//        String response = client.executeSparqlQuery(query, namespace, 0, "application/json", authorizationToken).readEntity(String.class);
//        System.out.println(response);
        String folder = "E:/RdfData/VREData/EKT RDF";
        Response importResponse = client.importFile(
                readFileData("C:/Users/rousakis/AppData/Roaming/Skype/My Skype Received Files/organizationUnits2.nt"), //small dataset
                "text/plain", // content type
                namespace, // namespace
                "http://test", // namedGraph
                authorizationToken);

        System.out.println(importResponse.readEntity(String.class));

    }

    public static String readFileData(String filename) {
        File f = new File(filename);
        StringBuilder sb = new StringBuilder();
        try {
            BufferedReader br = new BufferedReader(new FileReader(f));
            String line = null;
            while ((line = br.readLine()) != null) {
                sb.append(line + "\n");
            }
            br.close();
        } catch (Exception e) {
            System.out.println("Exception while reading import data occured .");
            return null;
        }
        return sb.toString();
    }

}
