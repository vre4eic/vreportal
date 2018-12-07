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

    public static String GeoEntityQuery(String entityUri, String graphsClause) {
        String query = "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select ?object " + graphsClause + " \n"
                + "where {\n"
                + "?object a <" + entityUri + ">. \n"
                + "{\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> [a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>].\n"
                + "} UNION {\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> [a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>].\n"
                + "} UNION {\n"
                + "?object a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>\n"
                + "}\n"
                + "} limit 1";
        return query;
    }

    public static String EntityQuery(String entityUri, String graphsClause) {
        String query = "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select ?object " + graphsClause + " \n"
                + "where {\n"
                + "?object a <" + entityUri + ">. \n"
                + "} limit 1";
        return query;
    }

    public static void main(String[] args) throws IOException, ParseException, SQLException {
//        System.setProperty("org.apache.commons.logging.Log", "org.apache.commons.logging.impl.NoOpLog");
        String from = "from <http://rcuk-data> from <http://epos-data>";
        //JSONArray initEntitiesJSON = H2Service.retrieveAllEntities("jdbc:h2:~/evre", "sa", "");

        JSONArray initEntitiesJSON = DBService.retrieveAllEntities(true);

        JSONArray resultEntitiesJSON = new JSONArray();
        String authorizationToken = "05ca2485-d8b3-4709-b347-ccc3f5f76e4c";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "vre4eic";
        JSONParser parser = new JSONParser();
        for (int i = 0; i < initEntitiesJSON.size(); i++) {
            JSONObject entityJSON = (JSONObject) initEntitiesJSON.get(i);
            String query = GeoEntityQuery((String) entityJSON.get("uri"), from);
//            VirtuosoRestClient client = new RestClient(endpoint, namespace, authorizationToken);
            RestClient client = new RestClient(endpoint, authorizationToken);
            Response response = client.executeSparqlQuery(query, namespace, 0, "application/json", authorizationToken);
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
