package forth.ics.isl.triplestore;

import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;

import javax.ws.rs.client.Client;
import javax.ws.rs.client.ClientBuilder;
import javax.ws.rs.client.Entity;
import javax.ws.rs.client.Invocation;
import javax.ws.rs.client.WebTarget;
import javax.ws.rs.core.HttpHeaders;
import javax.ws.rs.core.Response;

import org.apache.http.client.ClientProtocolException;
import org.openrdf.rio.RDFFormat;
import org.springframework.beans.factory.annotation.Value;

import com.fasterxml.jackson.databind.JsonNode;

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
        } 
        else {
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
        Invocation.Builder invocationBuilder = webTarget.request().header(HttpHeaders.AUTHORIZATION, authorizationToken);
        
        Response response = invocationBuilder.get();
    	
        return response;
    }
    
}
