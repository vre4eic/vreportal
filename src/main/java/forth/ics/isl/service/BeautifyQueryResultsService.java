/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import forth.ics.isl.triplestore.RestClient;
import java.io.IOException;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class BeautifyQueryResultsService {

    private static final String VREPrefix = "http://139.91.183.70:8090/vre4eic/";
    private static final String CERIFPrefix = "http://eurocris.org/ontology/cerif#";
    ///////
    private String authorizationToken;
    private String endpoint;
    private String namespace;

    private JSONObject instanceInfo;

    public JSONObject getInstanceRelations() {
        return instanceInfo;
    }

    public BeautifyQueryResultsService(String authorizationToken, String endpoint, String namespace) {
        this.authorizationToken = authorizationToken;
        this.endpoint = endpoint;
        this.namespace = namespace;
        this.instanceInfo = new JSONObject();
        this.instanceInfo.put("related_entity_types", new JSONArray());
    }

    public void enrichEntityResults(String entityUri, String fromClause) throws IOException, ParseException {
        String query = "prefix cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "prefix vre4eic: <http://139.91.183.70:8090/vre4eic/>\n"
                + "select distinct  ?instance_uri ?instance_label ?instance_name ?instance_title ?instance_acronym ?instance_type\n"
                + fromClause + " where \n"
                + "{\n"
                + "  ?instance_uri rdfs:label ?instance_label .\n"
                + "  ?instance_uri a ?instance_type. \n"
                + "  optional { ?instance_uri cerif:has_name ?instance_name.}\n"
                + "  optional { ?instance_uri cerif:has_title ?instance_title.}\n"
                + "  optional { ?instance_uri cerif:has_acronym ?instance_acronym.}\n"
                //                + "  optional { ?instance_uri cerif:has_description ?instance_description. }\n"
                + "  filter (?instance_uri = <" + entityUri + ">).\n"
                + "}";
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        Response resp = client.executeSparqlQuery(query, "application/json", 0);
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(resp.readEntity(String.class));
        JSONArray results = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
        JSONObject row = (JSONObject) results.get(0);
        String instanceUri = getJSONObjectValue(row, "instance_uri");
        String instanceType = getJSONObjectValue(row, "instance_type");
        String instanceLabel = getJSONObjectValue(row, "instance_label");
        String instanceName = getJSONObjectValue(row, "instance_name");
        String instanceTitle = getJSONObjectValue(row, "instance_title");
        String instanceAcronym = getJSONObjectValue(row, "instance_acronym");
        ////
        instanceInfo.put("instance_uri", instanceUri);
        if (instanceName != null) {
            instanceInfo.put("instance_label", instanceName);
        }
        if (instanceTitle != null) {
            instanceInfo.put("instance_label", instanceTitle);
        }
        if (instanceName == null && instanceTitle == null) {
            instanceInfo.put("instance_label", instanceLabel);
        }
        instanceInfo.put("instance_acronym", instanceAcronym);
        instanceInfo.put("instance_type", instanceType.replace(CERIFPrefix, ""));

    }

    public void enrichDstEntityResults(String entityUri, String fromClause) throws IOException, ParseException {
        String query = "prefix cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "prefix vre4eic: <http://139.91.183.70:8090/vre4eic/>\n"
                + "select distinct  ?ent ?rel_classif ?ent_label ?ent_name ?ent_title ?ent_acronym ?ent_type\n"
                + fromClause + " where \n"
                + "{\n"
                + "  ?instance_uri cerif:is_source_of ?x.\n"
                + "  ?x rdfs:label ?xlabel; \n"
                + "     cerif:has_classification [cerif:has_roleExpression ?rel_classif];\n"
                + "     cerif:has_destination ?ent.\n"
                + "  ?ent a ?ent_type.\n"
                + "  ?ent rdfs:label ?ent_label.\n"
                + "  optional {?ent cerif:has_name ?ent_name.} \n"
                + "  optional {?ent cerif:has_title ?ent_title.} \n"
                + "  optional {?ent cerif:has_acronym ?ent_acronym.} \n"
                //                + "   optional {?ent cerif:has_description ?ent_description. }\n"
                + "  filter (?instance_uri = <" + entityUri + ">).\n"
                + "} order by ?ent_type";
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        Response resp = client.executeSparqlQuery(query, "application/json", 0);
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(resp.readEntity(String.class));
        JSONArray results = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
        manageQueryResults(results);

    }

    public void enrichSrcEntityResults(String entityUri, String fromClause) throws IOException, ParseException {
        String query = "prefix cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "prefix vre4eic: <http://139.91.183.70:8090/vre4eic/>\n"
                + "select distinct ?ent ?rel_classif ?ent_label ?ent_name ?ent_title ?ent_acronym ?ent_type \n"
                + fromClause + " where \n"
                + "{\n"
                + "  ?ent cerif:is_source_of ?pfle. \n"
                + "  ?pfle rdfs:label ?pflelabel; \n"
                + "        cerif:has_classification [cerif:has_roleExpression ?rel_classif];\n"
                + "        cerif:has_destination ?instance_uri.\n"
                + "  ?ent rdfs:label ?ent_label.\n"
                + "  ?ent a ?ent_type.\n"
                + "  optional {?ent cerif:has_name ?ent_name.}         \n"
                + "  optional {?ent cerif:has_title ?ent_title.} \n"
                + "  optional {?ent cerif:has_acronym ?ent_acronym.} \n"
                //                + "    optional {?ent cerif:has_description ?ent_description.} \n"
                + "  filter (?instance_uri = <" + entityUri + ">).\n"
                + "} order by ?ent_type";
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        Response resp = client.executeSparqlQuery(query, "application/json", 0);
        JSONParser parser = new JSONParser();
        JSONObject result = (JSONObject) parser.parse(resp.readEntity(String.class));
        JSONArray results = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
        manageQueryResults(results);
    }

    private void manageQueryResults(JSONArray results) {
        String entType = "";
        JSONObject entitiesOfType;
        JSONArray relEntities = null;
        for (int i = 0; i < results.size(); i++) {
            JSONObject row = (JSONObject) results.get(i);
            String type = getJSONObjectValue(row, "ent_type");
            if (!entType.equals(type)) {
                entitiesOfType = new JSONObject();
                relEntities = new JSONArray();
                entitiesOfType.put("related_entity_type", getJSONObjectValue(row, "ent_type").replace(CERIFPrefix, ""));
                entitiesOfType.put("related_entities_of_type", relEntities);
                ((JSONArray) instanceInfo.get("related_entity_types")).add(entitiesOfType);
                entType = type;
            }
            String relation = getJSONObjectValue(row, "rel_classif");
            String entDstLabel = getJSONObjectValue(row, "ent_label");
            String entDstName = getJSONObjectValue(row, "ent_name");
            String entDstTitle = getJSONObjectValue(row, "ent_title");
            String entDstAcronym = getJSONObjectValue(row, "ent_acronym");
            String entUri = getJSONObjectValue(row, "ent");
            ////
            JSONObject relEntity = new JSONObject();
            relEntity.put("relation", relation);
            relEntity.put("related_entity_uri", entUri);
            if (entDstName != null) {
                relEntity.put("related_entity_label", entDstName);
            }
            if (entDstTitle != null) {
                relEntity.put("related_entity_label", entDstTitle);
            }
            if (entDstName == null && entDstTitle == null) {
                relEntity.put("related_entity_label", entDstLabel);
            }
            relEntity.put("related_entity_acronym", entDstAcronym);
            relEntities.add(relEntity);
        }
    }

    private String getJSONObjectValue(JSONObject obj, String key) {
        return obj.get(key) == null ? null : (String) ((JSONObject) obj.get(key)).get("value");
    }

    public JSONObject getInstanceInfo() {
        return instanceInfo;
    }

    public static void main(String[] args) throws Exception {

        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "vre4eic";
        String token = "a7846cdb-50c1-4681-828d-168a3a537e74";
        String entityUri = "http://139.91.183.70:8090/vre4eic/EKT.Person.20155";
        entityUri = "http://139.91.183.70:8090/vre4eic/EKT.OrgUnit.105201";
        entityUri = "http://139.91.183.70:8090/vre4eic/EKT.Project.7602";
        String fromClause = "from <http://ekt-data>";

        BeautifyQueryResultsService beauty = new BeautifyQueryResultsService(token, endpoint, namespace);
        beauty.enrichEntityResults(entityUri, fromClause);
        beauty.enrichDstEntityResults(entityUri, fromClause);
        beauty.enrichSrcEntityResults(entityUri, fromClause);
        System.out.println(beauty.getInstanceInfo());

    }

}
