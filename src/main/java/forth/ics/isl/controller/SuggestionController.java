/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor. 
 */
package forth.ics.isl.controller;

import com.fasterxml.jackson.databind.JsonNode;
import forth.ics.isl.data.model.suggest.EntitiesSuggester;
import forth.ics.isl.service.DBService;
import forth.ics.isl.triplestore.RestClient;
import java.io.IOException;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.Map;
import javax.annotation.PostConstruct;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.context.annotation.ScopedProxyMode;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

/**
 *
 * @author rousakis
 */
@Scope(scopeName = "session", proxyMode = ScopedProxyMode.TARGET_CLASS)
@Controller
public class SuggestionController {

    @Value("${service.url}")
    private String serviceUrl;
    @Value("${triplestore.namespace}")
    private String namespace;
    private JsonNode currQueryResult;
    private RestClient restClient;

    @PostConstruct
    public void init() throws IOException {
        // before controller
    }

    @RequestMapping(value = "/dynamic/get_relations_related_entities", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONArray populateRelationsEntities(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException, ParseException, SQLException {
        EntitiesSuggester suggester = new EntitiesSuggester((String) requestParams.get("model"), namespace, serviceUrl, authorizationToken);
        ArrayList<Map> entities = (ArrayList) requestParams.get("entities");
//        ArrayList<Map> entities = new ArrayList();
//        JSONArray entitiesJSON = DBService.retrieveAllEntities(false);
//        for (int i = 0; i < entitiesJSON.size(); i++) {
//            JSONObject obj = (JSONObject) entitiesJSON.get(i);
//            Map map = (Map) obj;
//            entities.add(map);
//        }
        JSONArray result = suggester.retrieveRelationsEntities(entities);
        return result;
    }

    @RequestMapping(value = "/dynamic/get_relations", method = RequestMethod.POST, produces = {"application/json"})
    public @ResponseBody
    JSONArray populateRelations(@RequestHeader(value = "Authorization") String authorizationToken, @RequestBody JSONObject requestParams) throws IOException {
        EntitiesSuggester suggester = new EntitiesSuggester((String) requestParams.get("model"), namespace, serviceUrl, authorizationToken);
        String relatedEntity = suggester.getRowModel().getRelatedName();
        JSONArray relEntityRelationTuples = suggester.getRowModel().getRelatedEntityRelationTuples();

//        ArrayList<LinkedHashMap> entities = (ArrayList) suggester.getRowModel().get("relatedEntityRelationTuples");
//        String targetEntity = (String) requestParams.get("targetEntity");
//        String relatedEntity = (String) requestParams.get("relatedEntity");
        //this ArrayList is returned from service: /dynamic/get_relations_related_entities
        JSONArray relations = new JSONArray();
        for (int i = 0; i < relEntityRelationTuples.size(); i++) {
            JSONObject obj = (JSONObject) relEntityRelationTuples.get(i);
            String relEntity = (String) ((JSONObject) (obj.get("related_entity"))).get("name");
            if (relEntity.equals(relatedEntity)) {
                JSONObject relation = new JSONObject();
                relation.put("uri", (String) ((JSONObject) (obj.get("relation"))).get("uri"));
                relation.put("name", (String) ((JSONObject) (obj.get("relation"))).get("name"));
                relations.add(relation);
            }
        }
        return relations;
    }
}
