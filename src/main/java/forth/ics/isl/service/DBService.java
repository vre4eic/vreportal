/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import forth.ics.isl.runnable.H2Manager;
import forth.ics.isl.triplestore.RestClient;
import java.io.File;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.CannotGetJdbcConnectionException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.DataSourceUtils;
import org.springframework.stereotype.Repository;

/**
 * The back-end service used for applying the required communication with the
 * database database
 *
 * @author rousakis
 * @author Vangelis Kritsotakis
 */
@Repository
public class DBService {

    @Autowired
    private static JdbcTemplate jdbcTemplate;
    private static DataSource dataSource;

    @Autowired
    public void setDataSource(DataSource dataSource) {
        this.jdbcTemplate = new JdbcTemplate(dataSource);
    }

    private static Connection connection;

    public static Connection getConnection() {
        return connection;
    }

    public static void setConnection(Connection connection) {
        DBService.connection = connection;
    }

    static boolean jdbcTemplateUsed;// = true;

    public static boolean isJdbcTemplateUsed() {
        return jdbcTemplateUsed;
    }

    public static void setJdbcTemplateUsed(boolean jdbcTemplateUsed) {
        DBService.jdbcTemplateUsed = jdbcTemplateUsed;
    }

    @PostConstruct
    public void init() {
        System.out.println("@PostConstruct - DBService");
        setJdbcTemplateUsed(true);
    }

    public static Connection initConnection() throws CannotGetJdbcConnectionException, SQLException {
        if (jdbcTemplateUsed) // Used only when a jdbcTemplate is spring injected
        {
            return DataSourceUtils.getConnection(jdbcTemplate.getDataSource());
        } else {
            return connection;
        }
    }

    private String getFilePath(String fileName) {
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }

    public static JSONObject retrieveEntityFromName(String entity) {
        JSONObject entityJSON = new JSONObject();
        try {
            //initStatement();
            Connection connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
            setConnection(connection);
            setJdbcTemplateUsed(false);
            Statement statement = connection.createStatement();
            ResultSet entities = statement.executeQuery("select * from entity where name = '" + entity + "'");
            while (entities.next()) {
                entityJSON.put("id", entities.getInt("id"));
                entityJSON.put("uri", entities.getString("uri"));
                entityJSON.put("name", entities.getString("name"));
                entityJSON.put("thesaurus", entities.getString("thesaurus"));
                JSONObject queryModel = new JSONObject();
                entityJSON.put("queryModel", queryModel);
                queryModel.put("format", "application/json");
                queryModel.put("query", entities.getString("query"));
                entityJSON.put("geospatial", entities.getBoolean("geospatial"));
            }
            entities.close();
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entityJSON;
    }

    public static JSONObject retrieveEntityFromURI(String uri) {
        JSONObject entityJSON = new JSONObject();
        try {
            //initStatement();
            Connection connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
            setConnection(connection);
            setJdbcTemplateUsed(false);
            Statement statement = connection.createStatement();
            ResultSet entities = statement.executeQuery("select * from entity where uri = '" + uri + "'");
            while (entities.next()) {
                entityJSON.put("id", entities.getInt("id"));
                entityJSON.put("uri", entities.getString("uri"));
                entityJSON.put("name", entities.getString("name"));
                entityJSON.put("thesaurus", entities.getString("thesaurus"));
                JSONObject queryModel = new JSONObject();
                entityJSON.put("queryModel", queryModel);
                queryModel.put("format", "application/json");
                queryModel.put("query", entities.getString("query"));
                entityJSON.put("geospatial", entities.getBoolean("geospatial"));
            }
            entities.close();
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entityJSON;
    }

    public static List<String> retrieveAllEntityNames() {
        List<String> entities = new ArrayList<>();
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
            setConnection(connection);
            setJdbcTemplateUsed(false);
            Statement statement = connection.createStatement();
            ResultSet result = statement.executeQuery("select name from entity");
            while (result.next()) {
                entities.add(result.getString("name"));
            }
            result.close();
            statement.close();
            connection.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entities;
    }

    public static JSONArray retrieveAllEntities() {
        JSONArray results = new JSONArray();
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
            setConnection(connection);
            setJdbcTemplateUsed(false);
            Statement statement = connection.createStatement();
            ResultSet entities = statement.executeQuery("select * from entity");
            while (entities.next()) {
                JSONObject entity = new JSONObject();
                entity.put("id", entities.getInt("id"));
                entity.put("name", entities.getString("name"));
                entity.put("thesaurus", entities.getString("thesaurus"));
                entity.put("uri", entities.getString("uri"));
                JSONObject queryModel = new JSONObject();
                entity.put("queryModel", queryModel);
                queryModel.put("format", "application/json");
                queryModel.put("query", entities.getString("query"));
                queryModel.put("geo_query", entities.getString("geo_query"));
                queryModel.put("text_geo_query", entities.getString("text_geo_query"));
                entity.put("geospatial", entities.getString("geospatial"));
                results.add(entity);
            }
            entities.close();
            statement.close();
            connection.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    public static List<String> retrieveAllNamedgraphUris() {
        List<String> uris = new ArrayList<>();
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("select uri from namedgraph");
            while (result.next()) {
                uris.add(result.getString("uri"));
            }
            result.close();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return uris;
    }

    public static JSONArray retrieveAllNamedgraphs() {
        JSONArray results = new JSONArray();
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            ResultSet namedGraphs = statement.executeQuery("select g.uri, g.name, c.id, c.name from \n"
                    + "namedgraph g, namedgraph_category c where g.category = c.id");
            while (namedGraphs.next()) {
                String gUri = namedGraphs.getString(1);
                String gName = namedGraphs.getString(2);
                int cID = namedGraphs.getInt(3);
                String cName = namedGraphs.getString(4);
                boolean found = false;
                for (int i = 0; i < results.size(); i++) {
                    JSONObject category = (JSONObject) results.get(i);
                    if (cID == (int) category.get("id")) {
                        found = true;
                        JSONArray children = (JSONArray) category.get("children");
                        JSONObject child = new JSONObject();
                        child.put("label", gName);
                        child.put("value", gUri);
                        children.add(child);
                    }
                }
                if (!found) {
                    JSONObject category = new JSONObject();
                    category.put("id", cID);
                    category.put("label", cName);
                    JSONArray children = new JSONArray();
                    category.put("children", children);
                    JSONObject child = new JSONObject();
                    child.put("label", gName);
                    child.put("value", gUri);
                    children.add(child);
                    results.add(category);
                }
            }
            namedGraphs.close();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    public static void createRelationsTable(H2Manager h2, String authorizationToken, String endpoint, String namespace) throws SQLException, UnsupportedEncodingException, ClassNotFoundException, IOException {
        DBService dbService = new DBService();
        DBService.setConnection(h2.getConnection());
        dbService.setJdbcTemplateUsed(false);
        JSONArray entities = DBService.retrieveAllEntities();
        h2.terminate();
        h2 = new H2Manager();
        dbService = new DBService();
        dbService.setConnection(h2.getConnection());
        dbService.setJdbcTemplateUsed(false);
        List<String> uris = DBService.retrieveAllNamedgraphUris();
        h2.terminate();
////
        h2 = new H2Manager();
        RestClient client = new RestClient(endpoint, namespace);
/////////
        for (String graphURI : uris) {
            for (int i = 0; i < entities.size(); i++) {
                JSONObject targetEntity = (JSONObject) entities.get(i);
                String targetEntityURI = (String) targetEntity.get("uri");
                int targetEntityID = (int) targetEntity.get("id");
                int cnt = 0;
                for (int j = 0; j < entities.size(); j++) {
                    if (j == i) {
                        continue;
                    }
                    StringBuilder sparqlQuery = new StringBuilder();
                    JSONObject relatedEntity = (JSONObject) entities.get(j);
                    String relatedEntityURI = (String) relatedEntity.get("uri");
                    int relatedEntityID = (int) relatedEntity.get("id");
                    sparqlQuery.append("select distinct ?relation from <" + graphURI + "> where {\n").
                            append("?target_inst a <" + targetEntityURI + ">.\n").
                            append("?target_inst ?relation [a <" + relatedEntityURI + ">].\n").
                            append("}");
                    String response = client.executeSparqlQuery(sparqlQuery.toString(), namespace, "text/csv", authorizationToken).readEntity(String.class);
                    String[] data = response.split("\\n");
                    for (int k = 1; k < data.length; k++) {
                        String relationUri = data[k];
                        String relationName = URLDecoder.decode(relationUri, "UTF-8").substring(relationUri.lastIndexOf("/") + 1);
                        h2.insertRelation(relationUri.trim(), relationName.trim(), targetEntityID, relatedEntityID, graphURI);
                    }
                }
            }
        }
        h2.terminate();
    }

    public static JSONArray retrieveRelationsEntities(List<String> graphs, String targetEntityName) {
        JSONObject targetEntity = DBService.retrieveEntityFromName(targetEntityName);
        JSONArray entities = DBService.retrieveAllEntities();
        HashMap<Integer, JSONObject> entitiesMap = new HashMap<>();
        for (int i = 0; i < entities.size(); i++) {
            JSONObject entity = (JSONObject) entities.get(i);
            entitiesMap.put((int) entity.get("id"), entity);
        }
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
            setConnection(connection);
            setJdbcTemplateUsed(false);
            Statement statement = connection.createStatement();
            Connection conn = initConnection();
            StringBuilder query = new StringBuilder("select * from relation where source_entity = " + targetEntity.get("id") + " and (");
            int cnt = 0;
            for (String graph : graphs) {
                query.append("graph = '" + graph + "'");
                cnt++;
                if (cnt < graphs.size()) {
                    query.append(" or ");
                }
            }
            query.append(")");
            ResultSet relations = statement.executeQuery(query.toString());
            JSONArray result = new JSONArray();
            while (relations.next()) {
                String relationURI = relations.getString("uri");
                String relationName = relations.getString("name");
                JSONObject destinationEntity = entitiesMap.get(relations.getInt("destination_entity"));
                String destinationEntityURI = (String) destinationEntity.get("uri");
                String destinationEntityName = (String) destinationEntity.get("name");
                JSONObject obj = new JSONObject();
                obj.put("relation_uri", relationURI);
                obj.put("relation_name", relationName);
                obj.put("related_entity_uri", destinationEntityURI);
                obj.put("related_entity_name", destinationEntityName);
                result.add(obj);
            }
            relations.close();
            statement.close();
            conn.close();
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }

    public static JSONArray retrieveRelations(List<String> graphs, String targetEntityName, String relatedEntityName) {
        JSONObject targetEntity = DBService.retrieveEntityFromName(targetEntityName);
        JSONObject relatedEntity = DBService.retrieveEntityFromName(relatedEntityName);
        try {
            Connection connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
            setConnection(connection);
            setJdbcTemplateUsed(false);
            Statement statement = connection.createStatement();
            Connection conn = initConnection();
            StringBuilder query = new StringBuilder("select * from relation where source_entity = " + targetEntity.get("id") + " "
                    + "and destination_entity = " + relatedEntity.get("id") + "and (");
            int cnt = 0;
            for (String graph : graphs) {
                query.append("graph = '" + graph + "'");
                cnt++;
                if (cnt < graphs.size()) {
                    query.append(" or ");
                }
            }
            query.append(")");
            ResultSet relations = statement.executeQuery(query.toString());
            JSONArray result = new JSONArray();
            while (relations.next()) {
                String relationURI = relations.getString("uri");
                String relationName = relations.getString("name");
                JSONObject obj = new JSONObject();
                obj.put("uri", relationURI);
                obj.put("name", relationName);
                result.add(obj);
            }
            relations.close();
            statement.close();
            conn.close();
            return result;
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return null;
    }
}
