package forth.ics.isl.controller;

import forth.ics.isl.data.model.parser.Utils;
import java.io.IOException;
import java.util.Map;

import javax.annotation.PostConstruct;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
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
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.databind.ObjectMapper;
import forth.ics.isl.data.model.parser.QueryDataModel;
import forth.ics.isl.data.model.suggest.EntitiesSuggester;

import forth.ics.isl.service.DBService;
import static forth.ics.isl.service.QueryGenService.EntityQuery;
import static forth.ics.isl.service.QueryGenService.GeoEntityQuery;
import forth.ics.isl.triplestore.VirtuosoRestClient;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import javax.ws.rs.ClientErrorException;
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

//    private RestClient restClient;
    private VirtuosoRestClient restClient;

    @Value("${service.url}")
    private String serviceUrl;
    @Value("${service.timeout}")
    private int timeout;
    @Value("${triplestore.namespace}")
    private String namespace;
    // Holding the results of the final query
    private JSONObject currFinalQueryResult;

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
//    @RequestMapping(value = "/get_all_entities", method = RequestMethod.GET, produces = {"application/json"})
//    public @ResponseBody
//    JSONArray loadEntitiesDataForPage(@RequestParam Map<String, String> requestParams) {//, Model model) {
//        System.out.println("Works");
//
//        /*
//    	int page = new Integer(requestParams.get("page")).intValue();
//    	int itemsPerPage = new Integer(requestParams.get("itemsPerPage")).intValue();
//
//    	// The EndPointForm for the page
//    	EndPointDataPage endPointDataPage = new EndPointDataPage();
//    	endPointDataPage.setPage(page);
//    	endPointDataPage.setTotalItems(currQueryResult.get("results").get("bindings").size());
//    	endPointDataPage.setResult(getDataOfPageForCurrentEndPointForm(page, itemsPerPage));
//    	    	
//		return endPointDataPage;
//         */
//        //JSONArray arr = retrieveAllEntities(h2ServiceUrl, h2ServiceUsername, h2ServicePassword);
//        JSONArray entities = DBService.retrieveAllEntities();
//        return entities;
//    }
    /**
     * This service retrieves the entities which are stored within the H2
     * database and for each entity it is examined if it has geospatial nature
     * in the considered namedgraphs (provided in the fromSearch parameter).
     */
    @RequestMapping(value = "/get_entities", method = RequestMethod.POST, produces = {"application/json;charset=utf-8"})
    public @ResponseBody
    JSONObject loadEntitiesDataForPagePOST(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
        System.out.println("Works");
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        JSONArray resultEntitiesJSON = new JSONArray();
        JSONObject finalResult = new JSONObject();
        String fromSearch = (String) requestParams.get("fromSearch");
        if (fromSearch == null || fromSearch.equals("")) {
            finalResult.put("remote_status", 200);
            finalResult.put("entities", resultEntitiesJSON);
            return finalResult;
        }
        List<String> graphs = Utils.getGraphsFromClause(fromSearch);
        JSONArray initEntitiesJSON = DBService.retrieveAllEntities(false);
        StringBuilder from = new StringBuilder();
        for (String graph : graphs) {
            from.append("from <" + graph + "> ");
        }
        for (int i = 0; i < initEntitiesJSON.size(); i++) {
            JSONObject entityJSON = (JSONObject) initEntitiesJSON.get(i);
            JSONParser parser = new JSONParser();
            String query = EntityQuery((String) entityJSON.get("uri"), from.toString());
            restClient = new VirtuosoRestClient(serviceUrl, authorizationToken);
            Response response = restClient.executeSparqlQuery(query, 0, "application/json", authorizationToken);
            if (response.getStatus() != 200) {
                System.out.println(response.readEntity(String.class));
                finalResult.put("remote_status", response.getStatus());
                return finalResult;
            }
            JSONObject result = (JSONObject) parser.parse(response.readEntity(String.class));
            JSONArray bindings = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
            if (bindings.isEmpty()) {
                continue;
            }
            if (entityJSON.get("geo_search") == null || entityJSON.get("geo_search").equals("")) {
                resultEntitiesJSON.add(entityJSON);
            } else {
                String geoQuery = GeoEntityQuery((String) entityJSON.get("uri"), from.toString());
                restClient = new VirtuosoRestClient(serviceUrl, authorizationToken);
                response = restClient.executeSparqlQuery(geoQuery, 0, "application/json", authorizationToken);
                if (response.getStatus() != 200) {
                    System.out.println(response.readEntity(String.class));
                    finalResult.put("remote_status", response.getStatus());
                    return finalResult;
                }
                result = (JSONObject) parser.parse(response.readEntity(String.class));
                bindings = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
                if (!bindings.isEmpty()) {
                    entityJSON.put("geospatial", true);
                } else {
                    entityJSON.put("geospatial", false);
                }
                resultEntitiesJSON.add(entityJSON);
            }
        }
        finalResult.put(
                "remote_status", 200);
        finalResult.put(
                "entities", resultEntitiesJSON);
        return finalResult;
    }

    /**
     * This service retrieves all the namedgraphs which are stored within the H2
     * database.
     */
    @RequestMapping(value = "/get_all_namedgraphs", method = RequestMethod.GET, produces = {"application/json;charset=utf-8"})
    public @ResponseBody
    JSONArray loadNGraphsDataForPage(@RequestParam Map<String, String> requestParams) {//, Model model) {
        System.out.println("Works");
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
    @RequestMapping(value = "/related_entity_query", method = RequestMethod.POST, produces = {"application/json;charset=utf-8"})
    public @ResponseBody
    JSONObject searchEntityQuery(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException {
        System.out.println("running executequery_json...");
        System.out.println("relatedEntity:" + requestParams.get("entity"));
        System.out.println("searchText:" + requestParams.get("searchText"));
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        // without authorization at the moment
//        System.out.println("authorizationToken: " + authorizationToken);
//        String entity = (String) requestParams.get("entity");
        String fromClause = (String) requestParams.get("fromSearch");
        String searchClause = (String) requestParams.get("searchText");
        JSONArray searchChips = new JSONArray();
        for (LinkedHashMap chip : (ArrayList<LinkedHashMap>) requestParams.get("relatedChips")) {
            searchChips.add(new JSONObject(chip));
        }
        if (requestParams.get("query") != null) {
            searchClause = Utils.getChipsFilter(searchChips).toString();
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
                geoQuery = geoQuery.replace("@#$%FROM%$#@", fromClause).
                        replace("@#$%NORTH%$#@", northClause).
                        replace("@#$%SOUTH%$#@", southClause).
                        replace("@#$%EAST%$#@", eastClause).
                        replace("@#$%WEST%$#@", westClause);
                JSONObject responseJsonObject = new JSONObject();
                responseJsonObject.put("query", geoQuery);
                return responseJsonObject;
            } else if (requestParams.get("text_geo_query") != null) {
                searchClause = Utils.getChipsFilter(searchChips).toString();
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
    }

    /**
     * This service populates the lists containing relations and related
     * entities considering the selected target entity and the selected named
     * graphs.
     *
     * @param authorizationToken A string holding the authorization token
     * @param requestParams A Json object holding the selected entity URI and
     * the from section of the query
     * @return A JSONArray in which each object contains information about the
     * relation and the corresponding related entity w.r.t. the target entity.
     * @throws IOException
     */
    @RequestMapping(value = "/get_relations_related_entities", method = RequestMethod.POST, produces = {"application/json;charset=utf-8"})
    public @ResponseBody
    JSONArray populateRelationsEntities(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
//        System.out.println("targetEntity:" + requestParams.get("targetEntity"));
//        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
//        String fromClause = (String) requestParams.get("fromSearch");
//        String targetEntity = (String) requestParams.get("name");
        // without authorization at the moment
//        System.out.println("authorizationToken: " + authorizationToken);
        EntitiesSuggester suggester = new EntitiesSuggester((String) requestParams.get("model"), namespace, serviceUrl, authorizationToken);
        QueryDataModel model = suggester.getModel();
        String fromClause = model.getSelectedGraphsClause();
        String targetEntity = model.getTargetModel().getName();
        ArrayList<LinkedHashMap> entities = (ArrayList) requestParams.get("entities");
        List<String> graphs = new ArrayList<>();
        Pattern regex = Pattern.compile("(?<=<)[^>]+(?=>)");
        Matcher regexMatcher = regex.matcher(fromClause);
        while (regexMatcher.find()) {
            graphs.add(regexMatcher.group());
        }
        return DBService.retrieveRelationsEntities(graphs, targetEntity, entities);
    }

    /**
     *
     * @param authorizationToken
     * @param requestParams
     * @return
     * @throws IOException
     */
    @RequestMapping(value = "/get_relations", method = RequestMethod.POST, produces = {"application/json;charset=utf-8"})
    public @ResponseBody
    JSONArray populateRelations(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException {
//        System.out.println("targetEntity:" + requestParams.get("targetEntity"));
//        System.out.println("relatedEntity:" + requestParams.get("relatedEntity"));
//        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        // without authorization at the moment
//        System.out.println("authorizationToken: " + authorizationToken);
//        String fromClause = (String) requestParams.get("fromSearch");
//        String targetEntity = (String) requestParams.get("targetEntity");
//        String relatedEntity = (String) requestParams.get("relatedEntity");
        EntitiesSuggester suggester = new EntitiesSuggester((String) requestParams.get("model"), namespace, serviceUrl, namespace);
        QueryDataModel model = suggester.getModel();
        String fromClause = model.getSelectedGraphsClause();
        String targetEntity = model.getTargetModel().getName();
        String relatedEntity = suggester.getRowModel().getRelatedName();
        List<String> graphs = Utils.getGraphsFromClause(fromClause);
        return DBService.retrieveRelations(graphs, targetEntity, relatedEntity);
    }

    /**
     * Method used for calling the respective service in order to run a query,
     * the results of which are stored in a variable
     *
     * @param authorizationToken A valid token ensuring security
     * @param requestParams A JSON Object holding the parameters (query and
     * format)
     *
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/execute_final_query", method = RequestMethod.POST, produces = {"application/json; charset=utf-8"})
    public @ResponseBody
    ResponseEntity<?> executeFinalQuery(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
//        restClient = new RestClient(serviceUrl, namespace, authorizationToken);
        restClient = new VirtuosoRestClient(serviceUrl, authorizationToken);
        String query = (String) requestParams.get("query");
//        System.out.println("query:" + query);
        JSONObject responseJsonObject = new JSONObject();
        responseJsonObject.put("query", query);

        try {
//            Response serviceResponce = restClient.executeSparqlQuery(requestParams.get("query").toString(), namespace, timeout, "application/json", authorizationToken);
            Response serviceResponce = restClient.executeSparqlQuery(requestParams.get("query").toString(), 0, "application/json", authorizationToken);
            System.out.println("serviceResponce.getStatus(): " + serviceResponce.getStatusInfo());

            // Setting Response status to POJO
            responseJsonObject.put("statusCode", serviceResponce.getStatus());
            responseJsonObject.put("statusInfo", serviceResponce.getStatusInfo().toString());

            // In case of OK status handle the response
            if (serviceResponce.getStatus() == 200) {
                //custom code which creates a graph to store the service data w.r.t. the user profile 
                //and the dynamic generated query
                if (query.indexOf("http://eurocris.org/ontology/cerif#WebService") > 0) {
                    createUserGraph(requestParams, query);
                }
                ////////////////////
                // Serializing in pojo
                ObjectMapper mapper = new ObjectMapper();
                // Holding JSON result in jsonNode globally (The whole results, which can be a lot)
                JSONParser parser = new JSONParser();
                currFinalQueryResult = (JSONObject) parser.parse(serviceResponce.readEntity(String.class));

                // Total Items
                int totalItems = ((JSONArray) ((JSONObject) currFinalQueryResult.get("results")).get("bindings")).size();

                // Setting total items for the response
                responseJsonObject.put("totalItems", totalItems);

                // Holding the first page results in a separate JsonNode
                JSONObject firstPageQueryResult = getDataOfPageForCurrentFinalQuery(1, (int) requestParams.get("itemsPerPage"), totalItems);

                // Setting results for the response (for now we set them all and 
                // later we will replace them with those at the first page)
                responseJsonObject.put("results", firstPageQueryResult);

                return ResponseEntity.status(HttpStatus.OK)
                        .body(responseJsonObject);

            } else if (serviceResponce.getStatus() == 400) {
                return new ResponseEntity<>(HttpStatus.BAD_REQUEST);
            } else if (serviceResponce.getStatus() == 401) {
                return new ResponseEntity<>(HttpStatus.UNAUTHORIZED);
            } else if (serviceResponce.getStatus() == 408) {
                return new ResponseEntity<>(HttpStatus.REQUEST_TIMEOUT);
            } else {
                return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
            }
        } catch (IOException e) {
            e.printStackTrace();
            return new ResponseEntity<>(HttpStatus.INTERNAL_SERVER_ERROR);
        }

        //return responseJsonObject;
    }

    @RequestMapping(value = "/enrich_profile_graph", method = RequestMethod.POST, produces = {"application/json; charset=utf-8"})
    public @ResponseBody
    ResponseEntity<?> enrichProfileGraph(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
        ResponseEntity responseEntity = new ResponseEntity(HttpStatus.NOT_ACCEPTABLE);
//        restClient = new RestClient(serviceUrl, namespace, authorizationToken);
        restClient = new VirtuosoRestClient(serviceUrl, authorizationToken);
        String query = (String) requestParams.get("query");
        System.out.println("query:" + query);
        JSONObject responseJsonObject = new JSONObject();
        responseJsonObject.put("query", query);

        //custom code which creates a graph to store the service data w.r.t. the user profile 
        //and the dynamic generated query
        Response response;
        if (query.indexOf("http://eurocris.org/ontology/cerif#WebService") > 0) {
            response = createUserGraph(requestParams, query);
            return ResponseEntity.status(response.getStatus()).body(response.readEntity(String.class));
        } else {
            return ResponseEntity.status(HttpStatus.OK).body("");
        }
        ////////////////////
    }

    private Response createUserGraph(JSONObject requestParams, String query) throws ClientErrorException {
        String userId = (String) ((LinkedHashMap) requestParams.get("userProfile")).get("userId");
        String graph = "http://profile/" + userId;
        int start = query.indexOf("from");  //consider selected namedgraphs
//        int start = query.indexOf("where");
        int end = query.lastIndexOf("}");
        StringBuilder updateQuery = new StringBuilder();
        updateQuery.append("insert into <" + graph + "> {\n")
                .append("?webserv a <http://eurocris.org/ontology/cerif#WebService>.\n")
                .append("?webserv <http://eurocris.org/ontology/cerif#has_URI>         ?webservUri .\n")
                .append("?webserv <http://eurocris.org/ontology/cerif#has_description> ?webservDescr.\n")
                .append("?webserv <http://eurocris.org/ontology/cerif#has_keywords>    ?webservKeywords .\n")
                .append("?webserv <http://eurocris.org/ontology/cerif#has_name>        ?webservName .\n")
                .append("?webserv <http://eurocris.org/ontology/cerif#is_source_of> ?webservmediumLE .\n")
                .append("?webservmediumLE  <http://eurocris.org/ontology/cerif#has_destination> ?webservMedium.\n")
                .append("?webservMedium a <http://eurocris.org/ontology/cerif#Medium>. \n")
                .append("?webserv <http://searchable_text> ?webservLabel. \n")
                .append("} where {\n")
                .append("select distinct ?webserv ?webservUri ?webservDescr ?webservKeywords ?webservName ?webservmediumLE ?webservMedium ?webservLabel ")
                .append(query.substring(start, end + 1) + "\n }");
        restClient.executeUpdatePOSTJSON("clear graph <" + graph + ">");
        return restClient.executeUpdatePOSTJSON(updateQuery.toString());
    }

    /**
     * Method used for calling the respective service in order to run a query,
     * the results of which are stored in a variable
     *
     * @param authorizationToken A valid token ensuring security
     * @param requestParams A JSON Object holding the parameters (query and
     * format)
     *
     * @return
     * @throws ParseException
     */
    @RequestMapping(value = "/get_final_query_results_per_page", method = RequestMethod.POST, produces = {"application/json;charset=utf-8"})
    public @ResponseBody
    JSONObject getFinalQueryResultsPerPage(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {

        JSONObject responseJsonObject = new JSONObject();

        // Total Items
        int totalItems = ((JSONArray) ((JSONObject) currFinalQueryResult.get("results")).get("bindings")).size();

        // Setting total items for the response
        responseJsonObject.put("totalItems", totalItems);
        JSONObject pageQueryResult = getDataOfPageForCurrentFinalQuery((int) requestParams.get("page"), (int) requestParams.get("itemsPerPage"), totalItems);
        responseJsonObject.put("results", pageQueryResult);

        return responseJsonObject;
    }

    /**
     * Constructs an ObjectNode for one page only, based on the passed page and
     * the whole data
     *
     * @param page	The page number
     * @param itemsPerPage The number of items per page
     * @return the constructed ObjectNode
     */
    private JSONObject getDataOfPageForCurrentFinalQuery(int page, int itemsPerPage, int totalItems) {

        //{"head":{"vars":["s","p","o"]},"results":{"bindings":[{"s":{... "p":{...
        // bindings
        JSONArray bindingsJsonArray = new JSONArray();

        JSONObject resultsObj = (JSONObject) currFinalQueryResult.get("results");
        JSONArray bindingsArr = (JSONArray) resultsObj.get("bindings");

        List<String> bindingsList = new ArrayList<String>();

        if (((page - 1) * itemsPerPage) + itemsPerPage + 1 > totalItems) {
            bindingsList = bindingsArr.subList(((page - 1) * itemsPerPage), totalItems);
        } else {
            bindingsList = bindingsArr.subList(((page - 1) * itemsPerPage) + 1, ((page - 1) * itemsPerPage) + itemsPerPage + 1);
        }

        for (int i = 0; i < bindingsList.size(); i++) {
            bindingsJsonArray.add(bindingsList.get(i));
        }

        JSONObject pageResultsObj = new JSONObject();
        pageResultsObj.put("bindings", bindingsJsonArray);

        return pageResultsObj;

    }

    public static void main(String[] args) throws SQLException, IOException {
        String fromClause = "from <http://ekt-data> from <http://rcuk-data>";
        String uri = "http://eurocris.org/ontology/cerif#Person";
        String targetName = "Person";
        String relatedName = "Project";

        List<String> graphs = new ArrayList<String>();
        Pattern regex = Pattern.compile("(?<=<)[^>]+(?=>)");
        Matcher regexMatcher = regex.matcher(fromClause);
        while (regexMatcher.find()) {
            graphs.add(regexMatcher.group());
        }
//        DBService.retrieveRelationsEntities(graphs, targetName);

//        DBService.retrieveRelations(graphs, targetName, relatedName);
    }

}
