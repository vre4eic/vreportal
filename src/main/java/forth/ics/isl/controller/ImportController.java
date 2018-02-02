package forth.ics.isl.controller;

import java.io.IOException;
import java.util.Iterator;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import forth.ics.isl.triplestore.RestClient;
import javax.ws.rs.core.Response;

/**
 * The back-end controller for the import service
 *
 * @author Vangelis Kritsotakis
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Controller
public class ImportController {

    @Value("${service.url}")
    private String serviceUrl;
    @Value("${triplestore.namespace}")
    private String namespace;
    private RestClient restClient;
    private JsonNode currQueryResult;

    @PostConstruct
    public void init() throws IOException {
        currQueryResult = new ObjectNode(JsonNodeFactory.instance);
        restClient = new RestClient(serviceUrl, namespace);
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity uploadFile(MultipartHttpServletRequest request) {
        System.out.println("Uploading...");
        String contentTypeParam = request.getParameter("contentTypeParam"); // Retrieving param that holds the file's content-type
        String namedGraphParam = request.getParameter("namedGraphParam"); 	// Retrieving param that holds the namedGraph where to store data
        String authorizationToken = request.getParameter("authorizationParam");//.getHeader("Authorization");		// Retrieving the authorization token
        System.out.println("authorizationToken: " + authorizationToken);
        System.out.println("contentTypeParam: " + contentTypeParam);
        System.out.println("namedGraphParam: " + namedGraphParam);
        String importResponseJsonString = null;
        try {
            Iterator<String> itr = request.getFileNames();

            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile multipartFile = request.getFile(uploadedFile);
                String mimeType = multipartFile.getContentType();
                String filename = multipartFile.getOriginalFilename();
                byte[] bytes = multipartFile.getBytes();
                
                String fileContent = new String(bytes);
                Response importResponse = restClient.importFile(fileContent, contentTypeParam, namespace, namedGraphParam, authorizationToken);
                //System.out.println("Status: " + importResponse.getStatus() + " " + importResponse.getStatusInfo());
                //System.out.println("Status: " + importResponse.readEntity(String.class).toString());

                //NOT___ Convert to JSon and hold in a string to return
                //importResponseJsonString = XML.toJSONObject(importResponse.readEntity(String.class)).toString();
                //importResponseJsonString = importResponse.readEntity(String.class);
                // Get the response in JSON directly from server (nothing else is needed)
                importResponseJsonString = importResponse.readEntity(String.class);

                // Fix for invalid message returned as a String with Json encrypted
//                String responseStr = importResponse.readEntity(String.class);
//                JSONParser parser = new JSONParser();
//                JSONObject responseJsonObj = (JSONObject) parser.parse(responseStr);
//                String messageString = (String) responseJsonObj.get("message");
//                JSONObject messageJsonObj = (JSONObject) parser.parse(messageString);
//                responseJsonObj.put("message", messageJsonObj);
//                importResponseJsonString = responseJsonObj.toString();

                if (importResponseJsonString.equals("{}")) {
                    importResponseJsonString = "There was an internal error. please check that you have selected the correct content-type.";
                    System.out.println("importResponseJsonString");
                    return new ResponseEntity<>(importResponseJsonString, HttpStatus.INTERNAL_SERVER_ERROR);
                }
                 
//                DBService.executeRelationsMatQueries(serviceUrl, namespace, authorizationToken, namedGraphParam);
//                H2Manager.enrichMatRelationsTable(serviceUrl, namespace, authorizationToken, namedGraphParam);
//
//                Connection conn = DBService.initConnection();
//                H2Manager h2 = new H2Manager(conn.createStatement(), conn);
//                h2.insertNamedGraph("http://test-graph", "TEST", "", 2);
//                System.out.println(importResponseJsonString);

            }
        } catch (Exception e) {
            return new ResponseEntity<>(importResponseJsonString, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(importResponseJsonString, HttpStatus.OK);
    }

}
