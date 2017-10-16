/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 * The back-end service used for applying the required communication with H2
 * database
 *
 * @author rousakis
 * @author Vangelis Kritsotakis
 */
public class H2Service {

    private static Statement statement;
    private static Connection connection;

    private String getFilePath(String fileName) {
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }

    /**
     * Initiates connection with the H2 database
     */
    private static void initConn(String URL, String username, String password) throws SQLException {
        //connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        connection = DriverManager.getConnection(URL, username, password);
        statement = connection.createStatement();
    }

    /**
     * Terminates connection with the H2 database
     */
    private static void terminateConn() throws SQLException {
        if (statement != null && !statement.isClosed()) {
            statement.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    public static JSONArray retrieveAllentities(String URL, String username, String password) {
        JSONArray results = new JSONArray();
        try {
            initConn(URL, username, password); // Initiates connection to H2
            ResultSet entities = statement.executeQuery("select * from entity");
            while (entities.next()) {
                JSONObject entity = new JSONObject();
                entity.put("name", entities.getString("name"));
                entity.put("thesaurus", entities.getString("thesaurus"));

                JSONObject queryModel = new JSONObject();
                entity.put("queryModel", queryModel);
                queryModel.put("format", "application/json");
                queryModel.put("query", entities.getString("query"));
                entity.put("geospatial", entities.getString("geospatial"));

                results.add(entity);
            }

            entities.close();
            terminateConn(); // Terminates connection to H2

        } catch (SQLException ex) {
            Logger.getLogger(H2Service.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

}
