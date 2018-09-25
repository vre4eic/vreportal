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
public class VirtuosoRestClient {

    private String serviceUrl;
    private String authorizationToken;

    public VirtuosoRestClient(String serviceUrl, String authorizationToken) throws IOException {
        // Be put in comments due to conflicts with log4j when creating the fat jar
    	Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
        for (String log : loggers) {
            Logger logger = (Logger) LoggerFactory.getLogger(log);
            logger.setLevel(Level.INFO);
            logger.setAdditive(false);
        }
        ///
        this.serviceUrl = serviceUrl;
        this.authorizationToken = authorizationToken;
    }

    public VirtuosoRestClient(String serviceUrl) throws IOException {
        // Be put in comments due to conflicts with log4j when creating the fat jar
//        Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
//        for (String log : loggers) {
//            Logger logger = (Logger) LoggerFactory.getLogger(log);
//            logger.setLevel(Level.INFO);
//            logger.setAdditive(false);
//        }
        ///
        this.serviceUrl = serviceUrl;
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
    public Response importFile(String content, String format, String namedGraph, String authorizationToken)
            throws ClientProtocolException, IOException {
        String restURL;
        restURL = serviceUrl + "/import/virtuoso";
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
    public Response executeSparqlQuery(String queryStr, int timeout, String format, String authorizationToken) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serviceUrl + "/query/virtuoso")
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
        WebTarget webTarget = client.target(serviceUrl + "/query/virtuoso")
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
        WebTarget webTarget = client.target(serviceUrl + "/query/virtuoso/batch");
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", authorizationToken).post(Entity.json(json.toJSONString()));
    }

    public Response executeCountQueryPOST(String query, String format) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        JSONObject json = new JSONObject();
        json.put("query", query);
        json.put("format", format);
        WebTarget webTarget = client.target(serviceUrl + "/query/virtuoso/count");
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", authorizationToken).post(Entity.json(json.toJSONString()));
    }

    public Response executeUpdatePOSTJSON(String updateQuery) throws ClientErrorException {
        Client client = ClientBuilder.newClient();
        JSONObject json = new JSONObject();
        json.put("query", updateQuery);
        WebTarget webTarget = client.target(serviceUrl + "/update/virtuoso");
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", authorizationToken).post(Entity.json(json.toJSONString()));
    }

    public static void main(String[] args) throws IOException {
        String query = "select ?service ?component ?base ?response ?status ?request_parameter ?style ?type from <http://epos-data-services>\n"
                + "\n"
                + "where {\n"
                + "\n"
                + "?service_uri a <http://www.cidoc-crm.org/cidoc-crm/Service> .\n"
                + "?service_uri <http://www.w3.org/2000/01/rdf-schema#label> ?service .\n"
                + "\n"
                + "?component_uri <http://www.cidoc-crm.org/cidoc-crm/has_service> ?service_uri .\n"
                + "?component_uri <http://www.w3.org/2000/01/rdf-schema#label> ?component . \n"
                + "\n"
                + "?component_uri <http://www.cidoc-crm.org/cidoc-crm/has_base> ?base_uri .\n"
                + "?base_uri <http://www.w3.org/2000/01/rdf-schema#label> ?base .\n"
                + "\n"
                + "?service_uri <http://www.cidoc-crm.org/cidoc-crm/has_method> ?method_uri .\n"
                + "?method_uri <http://www.w3.org/2000/01/rdf-schema#label> ?method .\n"
                + "\n"
                + "?method_uri <http://www.cidoc-crm.org/cidoc-crm/has_response> ?response_uri .\n"
                + "?response_uri <http://www.w3.org/2000/01/rdf-schema#label> ?response .\n"
                + "\n"
                + "\n"
                + "OPTIONAL{\n"
                + "\n"
                + "?response_uri <http://www.cidoc-crm.org/cidoc-crm/has_status> ?status_uri .\n"
                + "?status_uri <http://www.w3.org/2000/01/rdf-schema#label> ?status.\n"
                + "\n"
                + "}\n"
                + "\n"
                + "OPTIONAL{\n"
                + "\n"
                + "?method_uri <http://www.cidoc-crm.org/cidoc-crm/has_request_parameter> ?parameter_uri .\n"
                + "?parameter_uri <http://www.w3.org/2000/01/rdf-schema#label> ?request_parameter .\n"
                + "\n"
                + "?parameter_uri <http://www.cidoc-crm.org/cidoc-crm/has_style> ?style_uri .\n"
                + "?style_uri <http://www.w3.org/2000/01/rdf-schema#label> ?style.\n"
                + "\n"
                + "?parameter_uri <http://www.cidoc-crm.org/cidoc-crm/has_type> ?type_uri .\n"
                + "?type_uri <http://www.w3.org/2000/01/rdf-schema#label> ?type .\n"
                + "\n"
                + "}\n"
                + "\n"
                + "}";

        String authorizationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOb2RlU2VydmljZSJ9.3ORP6OmfHdl0Pq0osxy_YCdROwyMoq-cQJkcodBJ5UQ";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
//        endpoint = "http://139.91.183.48:8181/EVREMetadataServices";
        VirtuosoRestClient client = new VirtuosoRestClient(endpoint, authorizationToken);
        
        System.out.println(query);
        
        Response response = client.executeSparqlQuery(query, 0, "application/json", authorizationToken);

        System.out.println(response.readEntity(String.class));
//        String folder = "E:/RdfData/VREData/EKT RDF";
//        Response importResponse = client.importFile(
//                readFileData("C:/Users/rousakis/AppData/Roaming/Skype/My Skype Received Files/organizationUnits2.nt"), //small dataset
//                "text/plain", // content type
//                "http://test", // namedGraph
//                authorizationToken);

//        System.out.println(importResponse.readEntity(String.class));
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
