package forth.ics.isl.controller;

import java.io.IOException;
import java.util.Iterator;
import java.util.LinkedHashMap;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.multipart.MultipartHttpServletRequest;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;
import forth.ics.isl.runnable.H2Manager;
import forth.ics.isl.service.DBService;

import forth.ics.isl.triplestore.RestClient;
import java.sql.Connection;
import java.util.Set;
import javax.ws.rs.core.Response;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.web.bind.annotation.ResponseBody;

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

    /*
     * Saving meta-data information for the file to be uploaded in the database
     */
    @RequestMapping(value = "/createGraphMetadata", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject importData(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {

        System.out.println("Saving metadata into the database...");

        String namedGraphLabelParam = null;
        String namedGraphIdParam = null;
        String selectedCategoryLabel = null;
        String selectedCategoryId = null;

        // Retrieving the label of the named graph
        if (requestParams.get("namedGraphLabelParam") != null) {
            namedGraphLabelParam = requestParams.get("namedGraphLabelParam").toString();
        }
        // Retrieving the id of the named graph
        if (requestParams.get("namedGraphIdParam") != null) {
            namedGraphIdParam = requestParams.get("namedGraphIdParam").toString();
        }
        // Retrieving the label of the category of the named graph
        if (requestParams.get("selectedCategoryLabel") != null) {
            selectedCategoryLabel = requestParams.get("selectedCategoryLabel").toString();
        }
        // Retrieving the id of the category of the named graph
        if (requestParams.get("selectedCategoryId") != null) {
            selectedCategoryId = requestParams.get("selectedCategoryId").toString();
        }

        System.out.println("namedGraphLabelParam: " + namedGraphLabelParam);
        System.out.println("namedGraphIdParam: " + namedGraphIdParam);
        System.out.println("selectedCategoryLabel: " + selectedCategoryLabel);
        System.out.println("selectedCategoryId: " + selectedCategoryId);
        String graphUri = null;

        JSONObject responseJsonObject = new JSONObject();
        try {
            if (namedGraphIdParam == null) {
                Connection conn = DBService.initConnection();
                H2Manager h2 = new H2Manager(conn.createStatement(), conn);
                if (h2.namedGraphExists(namedGraphLabelParam)) {
                    responseJsonObject.put("success", false);
                    responseJsonObject.put("message", "Name: \"" + namedGraphLabelParam + "\" is already assigned.");
                    responseJsonObject.put("namedGraphIdParam", null);

                } else {
                    graphUri = "http://graph/" + System.currentTimeMillis();
                    responseJsonObject.put("success", true);
                    responseJsonObject.put("message", "The graph was successfully created.");
                    responseJsonObject.put("namedGraphIdParam", graphUri);
                    h2.insertNamedGraph(graphUri, namedGraphLabelParam, "", Integer.parseInt(selectedCategoryId));
                }
                conn.close();
            } else {
                responseJsonObject.put("success", true);
                responseJsonObject.put("message", "The graph will be enriched with new data.");
                responseJsonObject.put("namedGraphIdParam", namedGraphIdParam);
            }
        } catch (Exception e) {
            responseJsonObject.put("success", false);
            responseJsonObject.put("message", e.getMessage());
            responseJsonObject.put("namedGraphIdParam", namedGraphIdParam);
        }

        return responseJsonObject;
    }

    @RequestMapping(value = "/upload", method = RequestMethod.POST)
    public ResponseEntity uploadFile(MultipartHttpServletRequest request) {

        System.out.println("Uploading...");
        String contentTypeParam = request.getParameter("contentTypeParam"); // Retrieving param that holds the file's content-type
        String namedGraphIdParam = request.getParameter("namedGraphIdParam");
        String authorizationToken = request.getParameter("authorizationParam");//.getHeader("Authorization");		// Retrieving the authorization token
        System.out.println("authorizationToken: " + authorizationToken);
        System.out.println("contentTypeParam: " + contentTypeParam);
        System.out.println("namedGraphIdParam: " + namedGraphIdParam);
        String importResponseJsonString = null;
        /////
        try {
            ///////
            Iterator<String> itr = request.getFileNames();
            while (itr.hasNext()) {
                String uploadedFile = itr.next();
                MultipartFile multipartFile = request.getFile(uploadedFile);
                String mimeType = multipartFile.getContentType();
                String filename = multipartFile.getOriginalFilename();
                byte[] bytes = multipartFile.getBytes();

                String fileContent = new String(bytes);
                Response importResponse = restClient.importFile(fileContent, contentTypeParam, namespace, namedGraphIdParam, authorizationToken);
                int status = importResponse.getStatus();
                importResponseJsonString = importResponse.readEntity(String.class);
                if (status == 200) {
                    Set<String> matRelationEntities = DBService.executeRelationsMatQueries(serviceUrl, namespace, authorizationToken, namedGraphIdParam);
                    H2Manager.enrichMatRelationsTable(serviceUrl, namespace, authorizationToken, namedGraphIdParam, matRelationEntities);
                } else if (status == 500) {
                    importResponseJsonString = "There was an internal error. please check that you have selected the correct content-type.";
                    System.out.println("importResponseJsonString");
                    return new ResponseEntity<>(importResponseJsonString, HttpStatus.INTERNAL_SERVER_ERROR);
                } //
            }
        } catch (Exception e) {
            return new ResponseEntity<>(importResponseJsonString, HttpStatus.INTERNAL_SERVER_ERROR);
        }

        return new ResponseEntity<>(importResponseJsonString, HttpStatus.OK);
    }

}
