/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.runnable;

import forth.ics.isl.triplestore.RestClient;
import java.io.IOException;

/**
 *
 * @author rousakis
 */
public class Utils {

    public static boolean hasGeospatialNature(String entity, String graphs) throws IOException {
        String query = "PREFIX cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT ?name  ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Person.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "} limit 1";

        String endpoint = "http://139.91.183.70:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "vre4eic";

        RestClient client = new RestClient(endpoint, namespace);
        client.executeSparqlQuery(query, namespace, "application/json", endpoint);
        return false;
    }

}
