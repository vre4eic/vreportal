/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import forth.ics.isl.triplestore.RestClient;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import javax.ws.rs.core.Response;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;

/**
 *
 * @author rousakis
 */
public class QueryGenService {

    public static String geoEntityQuery(String entityUri, String graphsClause) {
        String query = "PREFIX cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT ?object \n"
                + graphsClause + " \n"
                + "WHERE {\n"
                + "?object a <" + entityUri + ">.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination [a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>]."
                + "} limit 1";
        return query;
    }

    public static void main(String[] args) throws IOException, ParseException, SQLException {
//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        String forClause = "from <http://rcuk-data> from <http://epos-data>";
        //JSONArray initEntitiesJSON = H2Service.retrieveAllEntities("jdbc:h2:~/evre", "sa", "");

        Connection connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        DBService dbService = new DBService();
        dbService.setConnection(connection);
        dbService.setJdbcTemplateUsed(false);
        JSONArray initEntitiesJSON = DBService.retrieveAllEntities();

        JSONArray resultEntitiesJSON = new JSONArray();
        String authorizationToken = "20181e37-3b77-4b45-9f68-351c582bc5f7";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "vre4eic";
        JSONParser parser = new JSONParser();
        for (int i = 0; i < initEntitiesJSON.size(); i++) {
            JSONObject entityJSON = (JSONObject) initEntitiesJSON.get(i);
            String query = geoEntityQuery((String) entityJSON.get("uri"), forClause);
            RestClient client = new RestClient(endpoint, namespace);
            Response response = client.executeSparqlQuery(query, namespace, "application/json", authorizationToken);
            JSONObject result = (JSONObject) parser.parse(response.readEntity(String.class));
            JSONArray bindings = (JSONArray) ((JSONObject) result.get("results")).get("bindings");
            if (!bindings.isEmpty()) {
                entityJSON.put("geospatial", true);
            }
            resultEntitiesJSON.add(entityJSON);
        }
        System.out.println(resultEntitiesJSON.toJSONString());
    }
}
