package forth.ics.isl.controller;

import org.springframework.context.annotation.ScopedProxyMode;

import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import forth.ics.isl.data.model.EndPointDataPage;
import forth.ics.isl.data.model.EndPointForm;
import forth.ics.isl.data.model.InputAdvancedRequest;
import forth.ics.isl.data.model.InputGeoRequest;
import forth.ics.isl.data.model.InputTagRequest;
import forth.ics.isl.data.model.NgTag;
import forth.ics.isl.data.model.parser.QueryDataModel;
import forth.ics.isl.service.BeautifyQueryResultsService;
import forth.ics.isl.service.DBService;
import forth.ics.isl.triplestore.RestClient;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

/**
 * The back-end controller for the query service
 *
 * @author Vangelis Kritsotakis
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Controller
public class QueryController {

    @Value("${service.url}")
    private String serviceUrl;
    @Value("${triplestore.namespace}")
    private String namespace;
    private JsonNode currQueryResult;
    private RestClient restClient;

    //@Autowired
    private BeautifyQueryResultsService beautifyQueryResultsService;

    @PostConstruct
    public void init() throws IOException, SQLException {
    }

    @RequestMapping(value = "/final_search_query", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject searchEntityQuery(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody String queryModel) throws IOException {
        QueryDataModel model = new QueryDataModel(queryModel);
        JSONObject responseJsonObject = new JSONObject();
        responseJsonObject.put("query", model.toSPARQL());
        return responseJsonObject;
    }

    @RequestMapping(value = "/retrieve_entity_info", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject retrieveEntityInfo(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {

        // Handling request parameters
        String entityUriStr = null;
        String fromSearchStr = null;

        if (requestParams.get("entityUri") != null) {
            entityUriStr = requestParams.get("entityUri").toString();
        }
        if (requestParams.get("fromSearch") != null) {
            fromSearchStr = requestParams.get("fromSearch").toString();
        }

        System.out.println("entityUriStr: " + entityUriStr);
        System.out.println("fromSearchStr: " + fromSearchStr);

        // Initializing service
        beautifyQueryResultsService = new BeautifyQueryResultsService(authorizationToken, serviceUrl);
        // Calling service
        beautifyQueryResultsService.enrichEntityResults(entityUriStr, fromSearchStr);
        beautifyQueryResultsService.enrichDstEntityResults(entityUriStr, fromSearchStr);
        beautifyQueryResultsService.enrichSrcEntityResults(entityUriStr, fromSearchStr);
        JSONObject responseJsonObject = new JSONObject(); // JSON Object to hold response
        // Getting JSON output and place it into response JSON Object
        responseJsonObject = beautifyQueryResultsService.getInstanceInfo();
        return responseJsonObject;
    }

}
