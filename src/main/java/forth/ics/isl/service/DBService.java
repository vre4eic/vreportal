/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import forth.ics.isl.triplestore.RestClient;
import java.io.File;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.annotation.PostConstruct;
import javax.ws.rs.ClientErrorException;
import javax.ws.rs.core.Response;

import org.apache.tomcat.jdbc.pool.DataSource;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
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
            return DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
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
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
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
                entityJSON.put("selection_list", entities.getString("selection_list"));
                entityJSON.put("keyword_search", entities.getString("keyword_search"));
                entityJSON.put("geo_search", entities.getString("geo_search"));
                entityJSON.put("filter_geo_search", entities.getString("filter_geo_search"));
                entityJSON.put("var_name", entities.getString("var_name"));
                entityJSON.put("selection_pattern", entities.getString("selection_pattern"));
            }
            entities.close();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entityJSON;
    }

    public static JSONObject retrieveEntityFromURI(String uri) {
        JSONObject entityJSON = new JSONObject();
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
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
                entityJSON.put("selection_list", entities.getString("selection_list"));
                entityJSON.put("keyword_search", entities.getString("keyword_search"));
                entityJSON.put("geo_search", entities.getString("geo_search"));
                entityJSON.put("filter_geo_search", entities.getString("filter_geo_search"));
                entityJSON.put("var_name", entities.getString("var_name"));
                entityJSON.put("selection_pattern", entities.getString("selection_pattern"));
            }
            entities.close();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entityJSON;
    }

    public static List<String> retrieveAllEntityNames() {
        List<String> entities = new ArrayList<>();
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("select name from entity");
            while (result.next()) {
                entities.add(result.getString("name"));
            }
            result.close();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entities;
    }

    public static JSONArray retrieveAllEntities(boolean fetchTargetEntities) {
        JSONArray results = new JSONArray();
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            String query = "select * from entity";
            if (fetchTargetEntities) {
                query += " where selection_list!=''";
            }
            ResultSet entities = statement.executeQuery(query); //ignore location
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
                entity.put("selection_list", entities.getString("selection_list"));
                entity.put("keyword_search", entities.getString("keyword_search"));
                entity.put("geo_search", entities.getString("geo_search"));
                entity.put("filter_geo_search", entities.getString("filter_geo_search"));
                entity.put("var_name", entities.getString("var_name"));
                entity.put("selection_pattern", entities.getString("selection_pattern"));
                results.add(entity);
            }
            entities.close();
            statement.close();
            conn.close();
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

    public static JSONArray retrieveAllRelationsMatUpdates() {
        JSONArray queries = new JSONArray();
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("select related_entities, update from relations_material");
            while (result.next()) {
                JSONObject obj = new JSONObject();
                obj.put("update", result.getString("update"));
                obj.put("related_entities", result.getString("related_entities"));
                queries.add(obj);
            }
            result.close();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return queries;
    }

    public static Map<String, String> retrieveAllRelations() {
        Map<String, String> relations = new HashMap<>();
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            ResultSet result = statement.executeQuery("select uri, name from relation");
            while (result.next()) {
                relations.put(result.getString("uri"), result.getString("name"));
            }
            result.close();
            statement.close();
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return relations;
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
                        child.put("id", gUri);
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
                    child.put("id", gUri);
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

    public static JSONArray retrieveRelationsEntities(List<String> graphs, String targetEntityName, ArrayList<LinkedHashMap> entities) {
        JSONObject targetEntity = DBService.retrieveEntityFromName(targetEntityName);
//        JSONArray entities = DBService.retrieveAllEntities();
        HashMap<Integer, JSONObject> entitiesMap = new HashMap<>();
        for (int i = 0; i < entities.size(); i++) {
            JSONObject entity = new JSONObject(entities.get(i));
            entitiesMap.put((int) entity.get("id"), entity);
        }
        try {
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            StringBuilder query = new StringBuilder("select distinct uri, name, destination_entity from relation where source_entity = " + targetEntity.get("id") + " and (");
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
            int id = 0;
            while (relations.next()) {
                String relationURI = relations.getString("uri");
                String relationName = relations.getString("name");
                JSONObject relatedEntity = entitiesMap.get(relations.getInt("destination_entity"));
                //if we want to omit an entity (e.g., location)
                if (relatedEntity == null) {
                    continue;
                }
                relatedEntity.put("id", id);
                JSONObject obj = new JSONObject();
                obj.put("related_entity", relatedEntity);
                JSONObject relJSON = new JSONObject();
                relJSON.put("uri", relationURI);
                relJSON.put("name", relationName);
                obj.put("relation", relJSON);
                result.add(obj);
                id++;
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
            Connection conn = initConnection();
            Statement statement = conn.createStatement();
            StringBuilder query = new StringBuilder("select * from relation where source_entity = " + targetEntity.get("id") + " "
                    + "and destination_entity = " + relatedEntity.get("id") + " and (");
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

    public static JSONObject saveIntoFavorites(String username, String title, String description, String queryModel, String favoriteId) {
        JSONObject statusObject = new JSONObject();
        try {
            Connection conn = initConnection();

            String sql = "";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);

            if (favoriteId == null) {
                sql = "INSERT INTO user_favorites (username, title, description, query_model)"
                        + "VALUES (?, ?, ?, ?)";

                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, username);
                preparedStatement.setString(2, title);
                preparedStatement.setString(3, description);
                preparedStatement.setString(4, queryModel);

            } else {
                sql = "UPDATE user_favorites "
                        + "SET query_model=? "
                        + "WHERE id=?";

                preparedStatement = conn.prepareStatement(sql);
                preparedStatement.setString(1, queryModel);
                preparedStatement.setString(2, favoriteId);

            }

            preparedStatement.executeUpdate();

            // Get the autogenerated id
            ResultSet generatedKeys = preparedStatement.getGeneratedKeys();
            if (generatedKeys.next()) {
                long id = generatedKeys.getLong(1);
                statusObject.put("generatedId", id);
            }

            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (conn != null) {
                conn.close();
            }

            statusObject.put("dbStatus", "success");

        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
            statusObject.put("dbStatus", "fail");
        }
        return statusObject;
    }

    public static JSONObject removeFromFavoritesById(String id) {
        JSONObject statusObject = new JSONObject();
        try {
            Connection conn = initConnection();

            String sql = "DELETE FROM user_favorites WHERE id = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, id);
            preparedStatement.executeUpdate();

            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (conn != null) {
                conn.close();
            }

            statusObject.put("dbStatus", "success");

        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
            statusObject.put("dbStatus", "fail");
        }
        return statusObject;
    }

    public static JSONObject retrieveFavoriteQueryModelsByUsername(String usernameStr) throws ParseException {
        JSONObject statusObject = new JSONObject();
        try {

            JSONArray favoriteModels = new JSONArray();

            Connection conn = initConnection();
            String sql = "SELECT * FROM user_favorites WHERE username = ?";
            PreparedStatement preparedStatement = conn.prepareStatement(sql);
            preparedStatement.setString(1, usernameStr);

            ResultSet rs = preparedStatement.executeQuery();
            while (rs.next()) {
                long favoriteId = rs.getLong("id");
                String username = rs.getString("username");
                String title = rs.getString("title");
                String description = rs.getString("description");
                String queryModel = rs.getString("query_model");

                JSONObject favoriteModel = new JSONObject();
                favoriteModel.put("favoriteId", favoriteId);
                favoriteModel.put("username", username);
                favoriteModel.put("title", title);
                favoriteModel.put("description", description);
                JSONParser parser = new JSONParser();
                JSONObject queryModelJson = (JSONObject) parser.parse(queryModel);
                favoriteModel.put("queryModel", queryModelJson);
                favoriteModels.add(favoriteModel);
            }

            if (rs != null) {
                rs.close();
            }
            if (preparedStatement != null) {
                preparedStatement.close();
            }
            if (conn != null) {
                conn.close();
            }

            statusObject.put("dbStatus", "success");
            statusObject.put("favoriteModels", favoriteModels);

        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
            statusObject.put("dbStatus", "fail");
        }
        return statusObject;
    }

//    executes the SPARQL update queries stored in table RELATIONS_MATERIAL 
//    of the H2 database to materialize the relation "shortcuts" between the 
//    interesting entities for the GUI
    public static Set<String> executeRelationsMatQueries(String endpoint, String namespace, String authorizationToken, String graphUri) throws SQLException, ParseException, ClientErrorException, IOException {
        Set<String> matRelationsEntities = new HashSet<>();
        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        JSONArray updates = DBService.retrieveAllRelationsMatUpdates();
        StringBuilder sb = new StringBuilder();
        sb.append(graphUri + "\n");
        for (int i = 0; i < updates.size(); i++) {
            JSONObject obj = (JSONObject) updates.get(i);
            String update = ((String) obj.get("update")).replace("@#$%FROM%$#@", "<" + graphUri + ">");
            String relatedEntities = (String) obj.get("related_entities");
            Response response = client.executeUpdatePOSTJSON(update, namespace, authorizationToken);
            int status = response.getStatus();
            String respString = response.readEntity(String.class);
            //the update query added new triples
            if (status == 200 && !respString.contains("mutationCount=0")) {
                String[] entit = relatedEntities.split("-");
                if (entit.length > 1) {
                    matRelationsEntities.add(entit[0]);
                    matRelationsEntities.add(entit[1]);
                }
            }
            sb.append(relatedEntities + " -> " + ((JSONObject) new JSONParser().parse(respString)).get("status") + "\n");
        }
        sb.append("-------\n");
        System.out.println(sb.toString());
        return matRelationsEntities;
    }

}
