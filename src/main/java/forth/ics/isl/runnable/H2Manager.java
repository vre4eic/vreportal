/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.runnable;

import forth.ics.isl.service.DBService;
import forth.ics.isl.triplestore.RestClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;

/**
 *
 * @author rousakis
 */
public class H2Manager {

    Statement statement;
    static Connection connection;

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
        deleteTable("relation");
        createTableCategory();
        createTableNamedgraph();
        createTableEntity();
        createTableRelation();
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

    public int insertEntity(String name, String uri, String thesaurus, String query, String geoQuery, String textGeoQuery, boolean geospatial) throws SQLException {
        return statement.executeUpdate("insert into entity(`name`, `uri`, `thesaurus`, `query`, `geo_query`, `text_geo_query`, `geospatial`)"
                + " values ('" + name + "','" + uri + "','" + thesaurus + "','" + query + "','" + geoQuery + "','" + textGeoQuery + "', " + geospatial + ")");
    }

    public int insertRelation(String uri, String name, int sourceEntity, int destinationEntity, String graph) throws SQLException {
        return statement.executeUpdate("insert into relation(`uri`, `name`, `source_entity`, `destination_entity`, `graph`)"
                + " values ('" + uri + "','" + name + "', " + sourceEntity + ", " + destinationEntity + ", '" + graph + "')");
    }

    public int updateEntityGeospatial(String entityName, String columnName, boolean columnValue) throws SQLException {
        return statement.executeUpdate("update entity set geospatial = " + columnValue + " where name = '" + entityName + "'");
    }

    public int createTableNamedgraph() throws SQLException {
        return statement.executeUpdate("CREATE TABLE namedgraph ( \n"
                + "uri varchar(30) not null, \n"
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
                + "uri clob, \n"
                + "name varchar(30), \n"
                + "query clob, \n"
                + "geo_query clob, \n"
                + "text_geo_query clob, \n"
                + "thesaurus varchar(50), \n"
                + "geospatial boolean, \n"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public int createTableRelation() throws SQLException {
        return statement.executeUpdate("CREATE TABLE relation ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "uri clob, \n"
                + "name varchar(30), \n"
                + "source_entity int, \n"
                + "destination_entity int, \n"
                + "graph varchar(30),"
                + "PRIMARY KEY (`id`),\n"
                + "FOREIGN KEY (`source_entity`) REFERENCES `entity` (`id`) ON DELETE CASCADE,"
                + "FOREIGN KEY (`destination_entity`) REFERENCES `entity` (`id`) ON DELETE CASCADE,"
                + "FOREIGN KEY (`graph`) REFERENCES `namedgraph` (`uri`) ON DELETE CASCADE"
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

    //to do: na elegxoume an tha einai matchAllTerms i oxi
    private void insertEntities() throws SQLException {
        insertEntity("Person",
                "http://eurocris.org/ontology/cerif#Person",
                "thesaurus/persons-firstAndLastNames.json",
                "PREFIX cerif: <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?persName ?Service (?pers as ?uri) @#$%FROM%$#@\n"
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
                "",
                "",
                false);
        insertEntity("Project",
                "http://eurocris.org/ontology/cerif#Project",
                "thesaurus/project-acronyms.json",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?projectTitle as ?title) ?projectAcronym ?Service (?proj as ?uri) @#$%FROM%$#@ \n"
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
                "",
                "",
                false);
        insertEntity("Publication",
                "http://eurocris.org/ontology/cerif#Publication",
                "thesaurus/publications-titles.json",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?pubTitle as ?title) (?pubDate as ?publication_date) ?Service (?pub as ?uri) @#$%FROM%$#@ \n"
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
                "",
                "",
                false);
        insertEntity("OrganisationUnit",
                "http://eurocris.org/ontology/cerif#OrganisationUnit",
                "thesaurus/organizationUnits-acronyms.json",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) @#$%FROM%$#@ \n"
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
                "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org cerif:has_acronym ?orgAcronym.\n"
                + "?prg cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "}",
                "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org cerif:has_acronym ?orgAcronym.\n"
                + "?org cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?orgName bds:search \"@#$%TERM%$#@\".\n"
                + "?orgName bds:matchAllTerms \"true\".\n"
                + "?orgName bds:relevance ?score.\n"
                + "}ORDER BY desc(?score)",
                false);
        insertEntity("Product",
                "http://eurocris.org/ontology/cerif#Product",
                "",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:matchAllTerms \"true\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) ",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:matchAllTerms \"true\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score)",
                false);
        insertEntity("Equipment",
                "http://eurocris.org/ontology/cerif#Equipment",
                "",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:matchAllTerms \"true\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) ",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:matchAllTerms \"true\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score)",
                false);
        insertEntity("Facility",
                "http://eurocris.org/ontology/cerif#Facility",
                "",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:matchAllTerms \"true\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) ",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object cerif:is_source_of ?FLES.\n"
                + "?FLES cerif:has_destination ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object cerif:is_source_of ?FLE1.\n"
                + "?FLE1 cerif:has_destination ?PA.\n"
                + "?PA cerif:is_source_of ?FLE2.\n"
                + "?FLE2 cerif:has_destination ?GBB.\n"
                + "?GBB cerif:has_eastBoundaryLongitude ?east.\n"
                + "?GBB cerif:has_westBoundaryLongitude ?west.\n"
                + "?GBB cerif:has_northBoundaryLatitude ?north.\n"
                + "?GBB cerif:has_southBoundaryLatitude ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:matchAllTerms \"true\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP cerif:is_source_of ?FLE4.?FLE4 cerif:has_destination ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score)",
                false);
    }

    private void insertRelations() throws SQLException {
        JSONArray entities = DBService.retrieveAllEntities();
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

    public static void main(String[] args) throws ClassNotFoundException, SQLException, UnsupportedEncodingException, IOException {
        H2Manager h2 = new H2Manager();
        h2.init();
        //        ResultSet results = h2.fetchEntities();
//        while (results.next()) {
//            System.out.println(results.getString(2));
//        }
//        System.out.println(H2Service.retrieveAllNamedgraphs("jdbc:h2:~/evre", "sa", ""));
//      System.out.println(H2Service.retrieveAllEntityNames("jdbc:h2:~/evre", "sa", ""));
        String authorizationToken = "ca14d12c-280a-41ee-b189-5ea133c6d68d";
        String endpoint = "http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT";
        String namespace = "vre4eic";

        DBService.createRelationsTable(h2, authorizationToken, endpoint, namespace);
    }

    public Connection getConnection() {
        return this.connection;
    }

}
