package forth.ics.isl.controller;

import static forth.ics.isl.service.QueryGenService.geoEntityQuery;

import java.io.IOException;
import java.sql.SQLException;
import java.util.List;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import forth.ics.isl.service.DBService;
import forth.ics.isl.triplestore.RestClient;

/**
 * The back-end controller for the User Profile Service activities
 *
 * @author Vangelis Kritsotakis
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Controller
public class UserProfileController {

	@Autowired
	private DBService dbService;

    @PostConstruct
    public void init() throws IOException, SQLException {
        dbService = new DBService();
    }
    
    /**
     * Saving the queryBuilder JSON model into the database for specific user
     * 
     * @param authorizationToken 	A string holding the authorization token
     * @param username 				The username of the user
     * @param username 				The title of the user
     * @param username 				The description of the user
     * @param queryModel 			The query JSON model used by the QueryBuilder
     */
    @RequestMapping(value = "/save_into_favorites", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject saveIntoFavorites(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
    	System.out.println("fromSearch:" + requestParams.get("username"));
    	System.out.println("fromSearch:" + requestParams.get("queryModel"));
    	System.out.println("fromSearch:" + requestParams.get("title"));
    	System.out.println("fromSearch:" + requestParams.get("description"));
        
    	JSONObject statusObject = new JSONObject();
    	statusObject = dbService.saveIntoFavorites(
    			requestParams.get("username").toString(), 
	    		requestParams.get("title").toString(), 
	    		requestParams.get("description").toString(), 
	    		requestParams.get("queryModel").toString()
    	);
    	
        return statusObject;
    }
    
    /** 
     * Retrieving user's list of query models from favorites
     * 
     * @param authorizationToken 	A string holding the authorization token
     * @param username 				The username of the user
     */
    @RequestMapping(value = "/retrieve_favorite_query_models_by_user", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONArray loadFavoriteQueryModel(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
    	System.out.println("fromSearch:" + requestParams.get("username"));
        
    	JSONArray queryModelsArray = new JSONArray();
        return queryModelsArray;
    }
    
    
}
