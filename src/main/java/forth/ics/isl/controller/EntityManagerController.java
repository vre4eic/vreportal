package forth.ics.isl.controller;

import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import forth.ics.isl.service.DBService;
import static forth.ics.isl.service.DBService.retrieveAllEntities;
import static forth.ics.isl.service.QueryGenService.geoEntityQuery;
import forth.ics.isl.triplestore.RestClient;
import java.sql.SQLException;
import java.util.List;
import javax.ws.rs.core.Response;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 * The back-end controller for the Database Service
 *
 * @author Vangelis Kritsotakis
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Controller
public class EntityManagerController {

    @Value("${h2.service.username}")
    private String h2ServiceUsername;
    @Value("${h2.service.password}")
    private String h2ServicePassword;
    private RestClient restClient;

    @Value("${service.url}")
    private String serviceUrl;
    @Value("${triplestore.namespace}")
    private String namespace;

    @Autowired
    private DBService dbService;

    @PostConstruct
    public void init() throws IOException, SQLException {
        dbService = new DBService();
    }

    /**
     * Request get for retrieving a number of items that correspond to the
     * passed page and returning them in the form of EndPointDataPage object.
     *
     * @param requestParams A map that holds all the request parameters
     * @return An EndPointDataPage object that holds the items of the passed
     * page.
     */
    @RequestMapping(value = "/get_all_entities", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody
    JSONArray loadEntitiesDataForPage(@RequestParam Map<String, String> requestParams) {//, Model model) {
        System.out.println("Works");

        /*
    	int page = new Integer(requestParams.get("page")).intValue();
    	int itemsPerPage = new Integer(requestParams.get("itemsPerPage")).intValue();

    	// The EndPointForm for the page
    	EndPointDataPage endPointDataPage = new EndPointDataPage();
    	endPointDataPage.setPage(page);
    	endPointDataPage.setTotalItems(currQueryResult.get("results").get("bindings").size());
    	endPointDataPage.setResult(getDataOfPageForCurrentEndPointForm(page, itemsPerPage));
    	    	
		return endPointDataPage;
         */
        //JSONArray arr = retrieveAllEntities(h2ServiceUrl, h2ServiceUsername, h2ServicePassword);
        JSONArray arr = dbService.retrieveAllEntities();

        return arr;
    }

    @RequestMapping(value = "/get_entities", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONArray loadEntitiesDataForPagePOST(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
        System.out.println("Works");
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        String fromClause = (String) requestParams.get("fromSearch");
        JSONArray initEntitiesJSON = DBService.retrieveAllEntities();
        JSONArray resultEntitiesJSON = new JSONArray();
        String endpoint = serviceUrl;
        JSONParser parser = new JSONParser();
        for (int i = 0; i < initEntitiesJSON.size(); i++) {
            JSONObject entityJSON = (JSONObject) initEntitiesJSON.get(i);
            String query = geoEntityQuery((String) entityJSON.get("uri"), fromClause);
            RestClient client = new RestClient(endpoint, namespace);
            Response response = client.executeSparqlQuery(query, namespace, "application/json", authorizationToken);
            JSONObject result = (JSONObject) parser.parse(response.readEntity(String.class));
            JSONArray bindings = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
            if (!bindings.isEmpty()) {
                JSONObject resultSetJSON = (JSONObject) bindings.get(0);
                if (resultSetJSON.containsKey("east")) { //there exists spatial info 
                    entityJSON.put("geospatial", true);
                }
                resultEntitiesJSON.add(entityJSON);
            }
        }
        return resultEntitiesJSON;
    }

    @RequestMapping(value = "/get_all_namedgraphs", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody
    JSONArray loadNGraphsDataForPage(@RequestParam Map<String, String> requestParams) {//, Model model) {
        System.out.println("Works");
        /*
    	int page = new Integer(requestParams.get("page")).intValue();
    	int itemsPerPage = new Integer(requestParams.get("itemsPerPage")).intValue();

    	// The EndPointForm for the page
    	EndPointDataPage endPointDataPage = new EndPointDataPage();
    	endPointDataPage.setPage(page);
    	endPointDataPage.setTotalItems(currQueryResult.get("results").get("bindings").size());
    	endPointDataPage.setResult(getDataOfPageForCurrentEndPointForm(page, itemsPerPage));
    	    	
		return endPointDataPage;
         */
        JSONArray arr = DBService.retrieveAllNamedgraphs();

        return arr;
    }

    /**
     * Web Service that accepts two parameters and returns a dynamically
     * constructed query for the related entity search
     *
     * @param authorizationToken A string holding the authorization token
     * @param requestParams A Json object holding the search-text and the from
     * section of the query
     * @return An EndPointDataPage object that holds the items of the passed
     * page.
     */
    @RequestMapping(value = "/related_entity_query", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject searchEntityQuery(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException {
        System.out.println("running executequery_json...");
        System.out.println("relatedEntity:" + requestParams.get("entity"));
        System.out.println("searchText:" + requestParams.get("searchText"));
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
//        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        // without authorization at the moment
//        System.out.println("authorizationToken: " + authorizationToken);
//        String entity = (String) requestParams.get("entity");
        String fromClause = (String) requestParams.get("fromSearch");
        String searchClause = (String) requestParams.get("searchText");
//        String northClause = "" + requestParams.get("north");
//        String southClause = "" + requestParams.get("south");
//        String eastClause = "" + requestParams.get("east");
//        String westClause = "" + requestParams.get("west");

//        Boolean geospatial = (Boolean) requestParams.get("geospatial");
        if (requestParams.get("query") != null) {
            String query = (String) requestParams.get("query");
            query = query.replace("@#$%FROM%$#@", fromClause).replace("@#$%TERM%$#@", searchClause);
            JSONObject responseJsonObject = new JSONObject();
            responseJsonObject.put("query", query);
            return responseJsonObject;
        } else if (requestParams.get("geo_query") != null) {
            if (searchClause == null || searchClause.equals("")) {
                String geoQuery = (String) requestParams.get("geo_query");
                String northClause = "" + requestParams.get("north");
                String southClause = "" + requestParams.get("south");
                String eastClause = "" + requestParams.get("east");
                String westClause = "" + requestParams.get("west");
                geoQuery = geoQuery.replace("@#$%FROM%$#@", fromClause).replace("@#$%NORTH%$#@", northClause).
                        replace("@#$%SOUTH%$#@", southClause).
                        replace("@#$%EAST%$#@", eastClause).
                        replace("@#$%WEST%$#@", westClause);
                JSONObject responseJsonObject = new JSONObject();
                responseJsonObject.put("query", geoQuery);
                return responseJsonObject;
            } else if (requestParams.get("text_geo_query") != null) {
                String textGeoQuery = (String) requestParams.get("text_geo_query");
                String northClause = "" + requestParams.get("north");
                String southClause = "" + requestParams.get("south");
                String eastClause = "" + requestParams.get("east");
                String westClause = "" + requestParams.get("west");
                textGeoQuery = textGeoQuery.replace("@#$%FROM%$#@", fromClause).
                        replace("@#$%TERM%$#@", searchClause).
                        replace("@#$%NORTH%$#@", northClause).
                        replace("@#$%SOUTH%$#@", southClause).
                        replace("@#$%EAST%$#@", eastClause).
                        replace("@#$%WEST%$#@", westClause);
                JSONObject responseJsonObject = new JSONObject();
                responseJsonObject.put("query", textGeoQuery);
                return responseJsonObject;
            } else {
                JSONObject responseJsonObject = new JSONObject();
                responseJsonObject.put("query", null);
                return responseJsonObject;
            }
        } else {
            JSONObject responseJsonObject = new JSONObject();
            responseJsonObject.put("query", null);
            return responseJsonObject;
        }

//        JSONObject entityData = DBService.retrieveEntity(entity);
//        Boolean geospatial = (Boolean) entityData.get("geospatial");
//        String query = (String) ((JSONObject) entityData.get("queryModel")).get("query");
//        query = query.replace("@#$%FROM%$#@", fromClause).replace("@#$%TERM%$#@", searchClause);
//        if (geospatial && northClause != null) {
//            query = query.replace("@#$%NORTH%$#@", northClause).
//                    replace("@#$%SOUTH%$#@", southClause).
//                    replace("@#$%EAST%$#@", eastClause).
//                    replace("@#$%WEST%$#@", westClause);
//        }
//        query = query.replace("@#$%FROM%$#@", fromClause);
//        JSONObject responseJsonObject = new JSONObject();
//        responseJsonObject.put("query", query);
//        return responseJsonObject;
    }

}
