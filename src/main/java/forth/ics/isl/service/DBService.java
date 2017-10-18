/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.service;

import java.io.File;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
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
 * The back-end service used for applying the required communication with the database
 * database
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
	
	private static Statement statement;
	
	public static Statement getStatement() {
		return statement;
	}
	public static void setStatement(Statement statement) {
		DBService.statement = statement;
	}
	
	static boolean jdbcTemplateUsed;// = true;
	
	public static boolean isJdbcTemplateUsed() {
		return jdbcTemplateUsed;
	}
	public static void setJdbcTemplateUsed(boolean jdbcTemplateUsed) {
		DBService.jdbcTemplateUsed = jdbcTemplateUsed;
	}
	
	@PostConstruct
	public void init(){
		System.out.println("@PostConstruct - DBService");
		setJdbcTemplateUsed(true);
	}
	
	public static Statement initStatement() throws CannotGetJdbcConnectionException, SQLException {
		if(jdbcTemplateUsed) // Used only when a jdbcTemplate is spring injected
			return DataSourceUtils.getConnection(jdbcTemplate.getDataSource()).createStatement();
		
		else
			return statement;
	}
	
    private String getFilePath(String fileName) {
        //Get file from resources folder
        ClassLoader classLoader = getClass().getClassLoader();
        File file = new File(classLoader.getResource(fileName).getFile());
        return file.getAbsolutePath();
    }

    public static JSONObject retrieveEntity(String entity) {
        JSONObject entityJSON = new JSONObject();
        try {
        	//initStatement();
            ResultSet entities = initStatement().executeQuery("select * from entity where name = '" + entity + "'");
            while (entities.next()) {
                entityJSON.put("name", entities.getString("name"));
                entityJSON.put("thesaurus", entities.getString("thesaurus"));
                JSONObject queryModel = new JSONObject();
                entityJSON.put("queryModel", queryModel);
                queryModel.put("format", "application/json");
                queryModel.put("query", entities.getString("query"));
                entityJSON.put("geospatial", entities.getString("geospatial"));
            }
            entities.close();
            //statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entityJSON;
    }

    public static List<String> retrieveAllEntityNames() {
        List<String> entities = new ArrayList<>();
        try {
        	//initStatement();
            ResultSet result = initStatement().executeQuery("select name from entity");
            while (result.next()) {
                entities.add(result.getString("name"));
            }
            result.close();
            //statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return entities;
    }

    public static JSONArray retrieveAllEntities() {
        JSONArray results = new JSONArray();
        try {
        	//initStatement();
            ResultSet entities = initStatement().executeQuery("select * from entity");
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
            //statement.close();

        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

    public static JSONArray retrieveAllNamedgraphs() {
        JSONArray results = new JSONArray();
        try {
        	//initStatement();
            ResultSet namedGraphs = initStatement().executeQuery("select g.uri, g.name, c.id, c.name from \n"
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
            //statement.close();
        } catch (SQLException ex) {
            Logger.getLogger(DBService.class.getName()).log(Level.SEVERE, null, ex);
        }
        return results;
    }

}
