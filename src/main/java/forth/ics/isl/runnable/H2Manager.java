/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.runnable;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 *
 * @author rousakis
 */
public class H2Manager {

    Statement statement;
    Connection connection;

    public H2Manager() throws ClassNotFoundException, SQLException {
        Class.forName("org.h2.Driver");
        connection = DriverManager.getConnection("jdbc:h2:~/evre", "sa", "");
        statement = connection.createStatement();
//        st.executeUpdate(
    }

    public void init() throws SQLException {
        deleteTable("namedgraph_category");
        deleteTable("namedgraph");
        deleteTable("entity");
        createTableCategory();
        createTableNamedgraph();
        createTableEntity();
        insertEntities();
        insertNamedgraphCategories();
        insertNamedgraphs();
    }

    public int deleteTable(String tableName) throws SQLException {
        return statement.executeUpdate("drop table " + tableName + " if exists");
    }

    public int insertNamedGraph(String uri, String name, String description, int category) throws SQLException {
        return statement.executeUpdate("insert into namedgraph values ('" + uri + "','" + name + "','" + description + "'," + category + ")");
    }

    public int insertNamedGraphCategory(String name) throws SQLException {
        return statement.executeUpdate("insert into namedgraph_category (`name`,`description`) values ('" + name + "', '')");
    }

    public int insertEntity(String name, String thesaurus, String query, boolean geospatial) throws SQLException {
        return statement.executeUpdate("insert into entity(`name`, `query`, `thesaurus`, `geospatial`)"
                + " values ('" + name + "','" + query + "','" + thesaurus + "'," + geospatial + ")");
    }

    public int createTableNamedgraph() throws SQLException {
        return statement.executeUpdate("CREATE TABLE namedgraph ( \n"
                + "uri varchar(20) not null, \n"
                + "name varchar(20), \n"
                + "description clob, \n"
                + "category int, \n"
                + "PRIMARY KEY (`uri`), \n"
                + "FOREIGN KEY (`category`) REFERENCES `namedgraph_category` (`id`) ON DELETE CASCADE"
                + ");");
    }

    public int createTableCategory() throws SQLException {
        return statement.executeUpdate("CREATE TABLE namedgraph_category ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "name varchar(20), "
                + "description clob,\n"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public ResultSet executeSelectQuery(String query) throws SQLException {
        return statement.executeQuery(query);
    }

    public int executeUpdateQuery(String update) throws SQLException {
        return statement.executeUpdate(update);
    }

    public int createTableEntity() throws SQLException {
        return statement.executeUpdate("CREATE TABLE entity ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "name varchar(20), \n"
                + "query VARCHAR(1000), \n"
                + "thesaurus varchar(50), \n"
                + "geospatial boolean, \n"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public void terminate() throws SQLException {
        if (statement != null && !statement.isClosed()) {
            statement.close();
        }
        if (connection != null && !connection.isClosed()) {
            connection.close();
        }
    }

    private void insertEntities() throws SQLException {
        insertEntity("Person",
                "thesaurus/Person",
                "PREFIX cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?pers as ?uri) ?persName ?Service @#$%FROM%$#@\n"
                + "where {\n"
                + "?pers cerif:is_source_of ?FLES.  \n"
                + "?FLES cerif:has_destination ?Ser.  \n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?pers a cerif:Person.  \n"
                + "?pers rdfs:label ?persName. \n"
                + "?persName bds:search \"@#$%TERM%$#@\".  \n"
                + "?persName bds:matchAllTerms \"true\".  \n"
                + "?persName bds:relevance ?score. \n"
                + "}  ORDER BY desc(?score) ?pers",
                false);
        insertEntity("Project",
                "thesaurus/Project",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?proj as ?uri) (?projectTitle as ?title) ?projectAcronym ?Service @#$%FROM%$#@ \n"
                + "where {\n"
                + "?proj cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?proj a cerif:Project.\n"
                + "?proj cerif:has_title ?projectTitle.\n"
                + "OPTIONAL {?proj cerif:has_URI ?projURI.}\n"
                + "BIND(if(bound(?projURI),?projURI,?proj) as ?projectURI).\n"
                + "?proj cerif:has_acronym ?projectAcronym.\n"
                + "?proj rdfs:label ?projName.\n"
                + "?projName bds:search \"@#$%TERM%$#@\".\n"
                + "?projName bds:matchAllTerms \"true\".\n"
                + "?projName bds:relevance ?score \n"
                + "} ORDER BY desc(?score)",
                false);
        insertEntity("Publication",
                "thesaurus/Publication",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?pub as ?uri) (?pubTitle as ?title) (?pubDate as ?publication_date) ?Service @#$%FROM%$#@ \n"
                + "where {\n"
                + "?pub cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?pub a cerif:Publication.\n"
                + "?pub cerif:has_title ?pubTitle.\n"
                + "?pub cerif:has_publicationDate ?pubDate.\n"
                + "?pubTitle bds:search \"@#$%TERM%$#@\".\n"
                + "?pubTitle bds:matchAllTerms \"true\".\n"
                + "?pubTitle bds:relevance ?score.\n"
                + "}ORDER BY desc(?score)",
                false);
        insertEntity("OrganisationUnit",
                "thesaurus/OrganisationUnit",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?org as ?uri) (?orgName as ?name) (?orgAcronym as ?acronym) ?Service @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org cerif:has_acronym ?orgAcronym.\n"
                + "?orgName bds:search \"@#$%TERM%$#@\".\n"
                + "?orgName bds:matchAllTerms \"true\".\n"
                + "?orgName bds:relevance ?score.\n"
                + "}ORDER BY desc(?score)",
                false);
    }

    private void insertNamedgraphCategories() throws SQLException {
        insertNamedGraphCategory("VREs");
        insertNamedGraphCategory("RIs");
    }

    private void insertNamedgraphs() throws SQLException {
        insertNamedGraph("http://ekt-data", "EKT", "", 1);
        insertNamedGraph("http://rcuk-data", "RCUK", "", 1);
        insertNamedGraph("http://fris-data", "FRIS", "", 1);
        insertNamedGraph("http://epos-data", "EPOS", "", 2);
        insertNamedGraph("http://envri-data", "ENVRI", "", 2);
    }

    public ResultSet fetchEntities() throws SQLException {
        return statement.executeQuery("select * from entity");
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException {
        H2Manager h2 = new H2Manager();
        h2.init();

        ResultSet results = h2.fetchEntities();
        while (results.next()) {
            System.out.println(results.getString(2));
        }

        h2.terminate();
    }

}
