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
import static forth.ics.isl.service.QueryGenService.geoEntityQuery;
import forth.ics.isl.triplestore.RestClient;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
        JSONArray entities = DBService.retrieveAllEntities();
        return entities;
    }

    /**
     * This service retrieves the entities which are stored within the H2
     * database and for each entity it is examined if it has geospatial nature
     * in the considered namedgraphs (provided in the fromSearch parameter).
     */
    @RequestMapping(value = "/get_entities", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject loadEntitiesDataForPagePOST(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
        System.out.println("Works");
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        String fromClause = (String) requestParams.get("fromSearch");
        JSONArray initEntitiesJSON = DBService.retrieveAllEntities();
        JSONArray resultEntitiesJSON = new JSONArray();
        String endpoint = serviceUrl;
        JSONParser parser = new JSONParser();
        JSONObject finalResult = new JSONObject();
        for (int i = 0; i < initEntitiesJSON.size(); i++) {
            JSONObject entityJSON = (JSONObject) initEntitiesJSON.get(i);
            String query = geoEntityQuery((String) entityJSON.get("uri"), fromClause);
            RestClient client = new RestClient(endpoint, namespace);
            Response response = client.executeSparqlQuery(query, namespace, "application/json", authorizationToken);
            if (response.getStatus() != 200) {
                System.out.println(response.readEntity(String.class));
                finalResult.put("remote_status", response.getStatus());
                return finalResult;
            }
            JSONObject result = (JSONObject) parser.parse(response.readEntity(String.class));
            JSONArray bindings = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
            if (!bindings.isEmpty()) {
                entityJSON.put("geospatial", true);
            }
            resultEntitiesJSON.add(entityJSON);
        }
        finalResult.put("remote_status", 200);
        finalResult.put("entities", resultEntitiesJSON.toJSONString());
        return finalResult;
    }

    /**
     * This service retrieves all the namedgraphs which are stored within the H2
     * database.
     */
    @RequestMapping(value = "/get_all_namedgraphs", method = RequestMethod.GET, produces = {"application/json"})
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
    @RequestMapping(value = "/related_entity_query", method = RequestMethod.POST, produces = {"application/json"})
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
    @RequestMapping(value = "/get_relations_related_entities", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONArray populateRelationsEntities(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
        System.out.println("targetEntity:" + requestParams.get("targetEntity"));
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        // without authorization at the moment
//        System.out.println("authorizationToken: " + authorizationToken);
        String fromClause = (String) requestParams.get("fromSearch");
        String targetEntity = (String) requestParams.get("name");
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
    @RequestMapping(value = "/get_relations", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONArray populateRelations(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException {
        System.out.println("targetEntity:" + requestParams.get("targetEntity"));
        System.out.println("relatedEntity:" + requestParams.get("relatedEntity"));
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));
        // without authorization at the moment
//        System.out.println("authorizationToken: " + authorizationToken);
        String fromClause = (String) requestParams.get("fromSearch");
        String targetEntity = (String) requestParams.get("targetEntity");
        String relatedEntity = (String) requestParams.get("relatedEntity");
        List<String> graphs = new ArrayList<>();
        Pattern regex = Pattern.compile("(?<=<)[^>]+(?=>)");
        Matcher regexMatcher = regex.matcher(fromClause);
        while (regexMatcher.find()) {
            graphs.add(regexMatcher.group());
        }
        return DBService.retrieveRelations(graphs, targetEntity, relatedEntity);
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
