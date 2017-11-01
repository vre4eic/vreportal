package forth.ics.isl.triplestore;

import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.HashSet;
import java.util.Set;
import javax.ws.rs.ClientErrorException;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.json.simple.JSONObject;
import org.slf4j.LoggerFactory;

import java.util.Set;
import java.util.Arrays;
import java.util.HashSet;
import ch.qos.logback.classic.Level;
import ch.qos.logback.classic.Logger;
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

    public RestClient(String serviceUrl, String namespace) throws IOException {
        Set<String> loggers = new HashSet<>(Arrays.asList("org.apache.http", "groovyx.net.http"));
        for (String log : loggers) {
            Logger logger = (Logger) LoggerFactory.getLogger(log);
            logger.setLevel(Level.INFO);
            logger.setAdditive(false);
        }
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
        String restURL = serviceUrl + "/import/namespace/" + namespace;

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
        WebTarget webTarget = client.target(restURL).queryParam("namegraph", namedGraph);
        System.out.println("authorizationToken NEW: " + authorizationToken);
        Response response = webTarget.request()
                .header(HttpHeaders.AUTHORIZATION, authorizationToken)
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
    public Response executeSparqlQuery(String queryStr, String namespace, String format, String authorizationToken) throws UnsupportedEncodingException {
        Client client = ClientBuilder.newClient();
        WebTarget webTarget = client.target(serviceUrl + "/query/namespace/" + namespace)
                .queryParam("format", format)
                .queryParam("query", URLEncoder.encode(queryStr, "UTF-8").replaceAll("\\+", "%20"));
        //System.out.println("HttpHeaders.AUTHORIZATION: " + authorizationToken);
        Invocation.Builder invocationBuilder = webTarget.request().header("Authorization", authorizationToken);
        Response response = invocationBuilder.get();
        return response;
    }

    public Response executeUpdatePOSTJSON(String update, String namespace, String token) throws ClientErrorException {
        Client client = ClientBuilder.newClient();
        JSONObject json = new JSONObject();
        json.put("query", update);
        WebTarget webTarget = client.target(serviceUrl + "/update/namespace/" + namespace);
        return webTarget.request(MediaType.APPLICATION_JSON).
                header("Authorization", token).post(Entity.json(json.toJSONString()));
    }

    public static void main(String[] args) throws IOException {
        String query = "PREFIX cerif:   <http://eurocris.org/ontology/cerif#> \n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) from <http://ekt-data> from <http://rcuk-data> from <http://fris-data> from <http://epos-data> from <http://envri-data>  \n"
                + "where { \n"
                + "?org cerif:is_source_of ?FLES. \n"
                + "?FLES cerif:has_destination ?Ser. \n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>. \n"
                + "?Ser cerif:has_acronym ?Service. \n"
                + "?org a cerif:OrganisationUnit. \n"
                + "?org cerif:has_name ?orgName. \n"
                + "?org cerif:has_acronym ?orgAcronym. \n"
                + "?org cerif:is_source_of ?FLE1. \n"
                + "?FLE1 cerif:has_destination ?PA. \n"
                + "?PA cerif:is_source_of ?FLE2. \n"
                + "?FLE2 cerif:has_destination ?GBB. \n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east. \n"
                + "?GBB cerif:has_westBoundaryLongitude ?west. \n"
                + "?GBB cerif:has_northBoundaryLatitude ?north. \n"
                + "?GBB cerif:has_southBoundaryLatitude ?south. \n"
                + "FILTER(xsd:float(?east) <= 25.317762826546776 && xsd:float(?west) >= -0.5220809234532258 && xsd:float(?north) <= 46.10500855922692 && xsd:float(?south) >= 33.798966629888795) \n"
                + "}";

        String authorizationToken = "83a73585-895c-4234-91aa-4a9e681fb8a8";
        String endpoint = "http://139.91.183.70:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "vre4eic";

        RestClient client = new RestClient(endpoint, namespace);
        String response = client.executeSparqlQuery(query, namespace, "application/json", authorizationToken).readEntity(String.class);
        System.out.println(response);

    }

}
