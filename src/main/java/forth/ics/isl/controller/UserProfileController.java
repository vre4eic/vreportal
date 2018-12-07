/*******************************************************************************
 * Copyright (c) 2018 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package forth.ics.isl.controller;

import java.io.IOException;
import java.sql.SQLException;

import javax.annotation.PostConstruct;

import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import forth.ics.isl.service.DBService;
import java.util.LinkedHashMap;

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
     * @param authorizationToken A string holding the authorization token
     * @param username The username of the user
     * @param username The title of the user
     * @param username The description of the user
     * @param queryModel The query JSON model used by the QueryBuilder
     */
    @RequestMapping(value = "/save_into_favorites", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject saveIntoFavorites(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {

    	JSONObject statusObject = new JSONObject();
        JSONObject queryModel = new JSONObject((LinkedHashMap)requestParams.get("queryModel"));
        
        // Update existing favorite
        if (requestParams.get("id") != null) {
        	statusObject = dbService.saveIntoFavorites(
	                requestParams.get("username").toString(),
	                requestParams.get("title").toString(),
	                requestParams.get("description").toString(),
	                queryModel.toString(),
	                requestParams.get("id").toString()
	        );
        }
        // Save new favorite (passing id as null)
        else {
	        statusObject = dbService.saveIntoFavorites(
	                requestParams.get("username").toString(),
	                requestParams.get("title").toString(),
	                requestParams.get("description").toString(),
	                queryModel.toString(),
	                null
	        );
        }

        return statusObject;
    }
    
    
    /**
     * Removing a queryBuilder JSON model by the given ID
     *
     * @param id A string representation of the query model's ID to be removed
     */
    @RequestMapping(value = "/remove_from_favorites_by_id", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONObject removeFromFavoritesById(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
        JSONObject statusObject = new JSONObject();
        statusObject = dbService.removeFromFavoritesById(requestParams.get("dbTableId").toString());
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
    JSONObject retrieveFavoriteQueryModelsByUsername(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException {
    	JSONObject statusObject = new JSONObject();
        statusObject = dbService.retrieveFavoriteQueryModelsByUsername(requestParams.get("username").toString());
        return statusObject;
    }

}
