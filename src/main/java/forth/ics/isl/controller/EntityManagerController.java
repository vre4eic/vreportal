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
    JSONArray loadDataForPage(@RequestParam Map<String, String> requestParams) {//, Model model) {
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
        JSONArray arr = h2Service.retrieveAllentities(h2ServiceUrl, h2ServiceUsername, h2ServicePassword);

        return arr;
    }
    	
    /*
    @RequestMapping(value = "/search_entity_results", method = RequestMethod.GET, produces = {"application/json"})
    public @ResponseBody JSONArray searchEntityResults(@RequestParam Map<String, String> requestParams) {
    */
    @RequestMapping(value = "/compute_related_entity_query", method = RequestMethod.POST, produces = {"application/json"}) 
    public @ResponseBody JSONObject searchEntityResults(@RequestHeader(value="Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException {

    	System.out.println("running executequery_json...");
		System.out.println("searchText:" + requestParams.get("searchText"));
		System.out.println("fromSearch:" + requestParams.get("fromSearch"));
		System.out.println("authorizationToken: " + authorizationToken);

        String queryToExecute = "PREFIX cerif: <http://eurocris.org/ontology/cerif#>\n"
				              + "select distinct ?persName ?Service (?pers as ?uri) " + requestParams.get("fromSearch") + "\n"
				              + "where {\n"
				              + "?pers cerif:is_source_of ?FLES.  \n"
				              + "?FLES cerif:has_destination ?Ser.  \n"
				              + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
				              + "?Ser cerif:has_acronym ?Service.\n"
				              + "?pers a cerif:Person.  \n"
				              + "?pers rdfs:label ?persName. \n"
				              + "?persName bds:search " + requestParams.get("searchText") + ".  \n"
				              + "?persName bds:matchAllTerms \"true\".  \n"
				              + "?persName bds:relevance ?score. \n"
				              + "}  ORDER BY desc(?score) ?pers limit 100";
        
        JSONObject responseJsonObject = new JSONObject();
        responseJsonObject.put("query", queryToExecute);
        		
        
        return responseJsonObject;
    }

}
