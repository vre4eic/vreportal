package forth.ics.isl.controller;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Map;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
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

import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import forth.ics.isl.data.model.EndPointDataPage;
import forth.ics.isl.data.model.EndPointForm;
import forth.ics.isl.data.model.InputTagRequest;
import forth.ics.isl.service.H2Service;
import forth.ics.isl.triplestore.RestClient;

/**
 * The back-end controller for the H2 Service
 *
 * @author Vangelis Kritsotakis
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Controller
public class EntityManagerController {

    private H2Service h2Service;
    @Value("${h2.service.url}")
    private String h2ServiceUrl;

    @Value("${h2.service.username}")
    private String h2ServiceUsername;
    @Value("${h2.service.password}")
    private String h2ServicePassword;
    private RestClient restClient;

    @Value("${service.url}")
    private String serviceUrl;

    @PostConstruct
    public void init() throws IOException {
        h2Service = new H2Service();
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
        JSONArray arr = h2Service.retrieveAllEntities(h2ServiceUrl, h2ServiceUsername, h2ServicePassword);

        return arr;
    }

    @RequestMapping(value = "/get_entities", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONArray loadEntitiesDataForPagePOST(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException {
        System.out.println("Works");
        System.out.println("fromSearch:" + requestParams.get("fromSearch"));

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
        JSONArray arr = H2Service.retrieveAllEntities(h2ServiceUrl, h2ServiceUsername, h2ServicePassword);

        return arr;
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
        JSONArray arr = h2Service.retrieveAllNamedgraphs(h2ServiceUrl, h2ServiceUsername, h2ServicePassword);

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
        String entity = (String) requestParams.get("entity");
        String fromClause = (String) requestParams.get("fromSearch");
        String searchClause = (String) requestParams.get("searchText");
        JSONObject entityData = H2Service.retrieveEntity(h2ServiceUrl, h2ServiceUsername, h2ServicePassword, entity);
        String query = (String) ((JSONObject) entityData.get("queryModel")).get("query");
        query = query.replace("@#$%FROM%$#@", fromClause).replace("@#$%TERM%$#@", searchClause);

        JSONObject responseJsonObject = new JSONObject();
        responseJsonObject.put("query", query);
        return responseJsonObject;
    }

}
