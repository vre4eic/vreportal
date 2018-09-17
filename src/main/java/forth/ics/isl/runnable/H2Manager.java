/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package forth.ics.isl.runnable;

import forth.ics.isl.service.DBService;
import forth.ics.isl.triplestore.VirtuosoRestClient;
import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.ParseException;

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

    public H2Manager(Statement statement, Connection connection) {
        this.statement = statement;
        H2Manager.connection = connection;
    }

    public void init() throws SQLException {
        deleteTable("namedgraph_category");
        deleteTable("namedgraph");
        deleteTable("entity");
        deleteTable("relation");
        deleteTable("relations_material");
        deleteTable("user_favorites");

        createTableCategory();
        createTableNamedgraph();
        createTableEntity();
        createTableRelation();
        createTableRelationsMatUpdates();
        createTableUserFavorites();
        insertEntitiesBlazegraph();
        insertNamedgraphCategories();
        insertNamedgraphs();
        insertRelationsMatUpdates();

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

    public int insertEntity(String name, String uri, String thesaurus, String query, String geoQuery, String textGeoQuery, boolean geospatial, String selectionList, String selectionPattern, String keywordSearch, String geoSearch, String filterGeoSearch, String varName) throws SQLException {
        return statement.executeUpdate("insert into entity(`name`, `uri`, `thesaurus`, `query`, `geo_query`, `text_geo_query`, `geospatial`, `selection_list`, `selection_pattern`, `keyword_search`, `geo_search`, `filter_geo_search`, `var_name`)"
                + " values ('" + name + "','" + uri + "','" + thesaurus + "','" + query + "','" + geoQuery + "', '" + textGeoQuery + "', " + geospatial + ", '" + selectionList + "', '" + selectionPattern + "', '" + keywordSearch + "', '" + geoSearch + "', '" + filterGeoSearch + "', '" + varName + "')");
    }

    public boolean relationExists(String uri, String name, int sourceEntity, int destinationEntity, String graph) throws SQLException {
        ResultSet result = statement.executeQuery("SELECT * FROM RELATION where "
                + "URI='" + uri + "' AND "
                + "NAME = '" + name + "' AND "
                + "SOURCE_ENTITY = " + sourceEntity + " AND "
                + "DESTINATION_ENTITY = " + destinationEntity + " AND "
                + "GRAPH = '" + graph + "'");
        return result.next();
    }

    public int insertRelation(String uri, String name, int sourceEntity, int destinationEntity, String graph) throws SQLException {
        return statement.executeUpdate("insert into relation(`uri`, `name`, `source_entity`, `destination_entity`, `graph`)"
                + " values ('" + uri + "','" + name + "', " + sourceEntity + ", " + destinationEntity + ", '" + graph + "')");
    }

    public int insertRelationMatUpdate(String relatedEntities, String update) throws SQLException {
        return statement.executeUpdate("insert into relations_material(`related_entities`, `update`)"
                + " values ('" + relatedEntities + "', '" + update + "')");
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
                + "selection_list clob, \n"
                + "selection_pattern clob, \n"
                + "keyword_search clob,\n"
                + "geo_search clob,\n"
                + "filter_geo_search clob,\n"
                + "var_name varchar(10),"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public int createTableRelation() throws SQLException {
        return statement.executeUpdate("CREATE TABLE relation ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "uri clob, \n"
                + "name varchar(100), \n"
                + "source_entity int, \n"
                + "destination_entity int, \n"
                + "graph varchar(30),"
                + "PRIMARY KEY (`id`),\n"
                + "FOREIGN KEY (`source_entity`) REFERENCES `entity` (`id`) ON DELETE CASCADE,"
                + "FOREIGN KEY (`destination_entity`) REFERENCES `entity` (`id`) ON DELETE CASCADE,"
                + "FOREIGN KEY (`graph`) REFERENCES `namedgraph` (`uri`) ON DELETE CASCADE"
                + ");");
    }

    private int createTableRelationsMatUpdates() throws SQLException {
        return statement.executeUpdate("CREATE TABLE relations_material ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "related_entities varchar(100),\n"
                + "update clob,\n"
                + "PRIMARY KEY (`id`)\n"
                + ");");
    }

    public int createTableUserFavorites() throws SQLException {
        return statement.executeUpdate("CREATE TABLE user_favorites ( \n"
                + "id int NOT NULL AUTO_INCREMENT, \n"
                + "username varchar(250),\n"
                + "title varchar(250),\n"
                + "description clob,\n"
                + "query_model clob,\n"
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

    //to do: na elegxoume an tha einai matchAllTerms i oxi
    private void insertEntitiesBlazegraph() throws SQLException {
        insertEntity("Person",
                "http://eurocris.org/ontology/cerif#Person",
                "thesaurus/persons-firstAndLastNames.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?persName ?Service (?pers as ?uri) @#$%FROM%$#@\n"
                + "where {\n"
                + "?pers a cerif:Person.  \n"
                + "OPTIONAL {?pers <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.  \n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.  \n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser cerif:has_acronym ?Service.}\n"
                + "?pers rdfs:label ?persName. \n"
                + "?persName bds:search \"@#$%TERM%$#@\".  \n"
                + "?persName bds:relevance ?score. \n"
                + "}  ORDER BY desc(?score) ?pers \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Name as ?name) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "OPTIONAL {?@#$%VAR%$#@  <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.}\n"
                //                + "?@#$%VAR%$#@ a cerif:Person.  \n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name. \n",
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name. \n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "",
                "",
                "pers"
        );
        insertEntity("Project",
                "http://eurocris.org/ontology/cerif#Project",
                "thesaurus/project-acronyms.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?projectTitle as ?title) ?projectAcronym ?Service (?proj as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?proj a cerif:Project.\n"
                + "?proj cerif:has_title ?projectTitle.\n"
                + "OPTIONAL {?proj cerif:has_URI ?projURI.}\n"
                + "BIND(if(bound(?projURI),?projURI,?proj) as ?projectURI).\n"
                + "?proj cerif:has_acronym ?projectAcronym.\n"
                + "?proj rdfs:label ?projName.\n"
                + "?projName bds:search \"@#$%TERM%$#@\".\n"
                + "?projName bds:relevance ?score \n"
                + "} ORDER BY desc(?score) \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Title as ?title) (?@#$%VAR%$#@Name as ?name) (?@#$%VAR%$#@Acronym as ?acronym) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?@#$%VAR%$#@ a cerif:Project.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_URI> ?@#$%VAR%$#@URI.}\n"
                + "BIND(if(bound(?@#$%VAR%$#@URI),?@#$%VAR%$#@URI,?@#$%VAR%$#@) as ?@#$%VAR%$#@).\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.\n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name.\n",
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "",
                "",
                "proj"
        );
        insertEntity("Publication",
                "http://eurocris.org/ontology/cerif#Publication",
                "thesaurus/publications-titles.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?pubTitle as ?title) (?pubDate as ?publication_date) ?Service (?pub as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                + "?pub <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "?pub <http://eurocris.org/ontology/cerif#has_title> ?pubTitle.\n"
                + "?pub <http://eurocris.org/ontology/cerif#has_publicationDate> ?pubDate.\n"
                + "?pubTitle bds:search \"@#$%TERM%$#@\".\n"
                + "?pubTitle bds:relevance ?score.\n"
                + "}ORDER BY desc(?score) \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Title as ?title) (?@#$%VAR%$#@Date as ?publication_date) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?@#$%VAR%$#@ a cerif:Publication.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_publicationDate> ?@#$%VAR%$#@Date.\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "?@#$%VAR%$#@Title bds:search \"@#$%TERM%$#@\".",
                "",
                "",
                "pub"
        );
        insertEntity("OrganisationUnit",
                "http://eurocris.org/ontology/cerif#OrganisationUnit",
                "thesaurus/organizationUnits-acronyms.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org rdfs:label ?orgLabel.\n"
                + "OPTIONAL { \n ?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "}\n"
                + "OPTIONAL {?org cerif:has_acronym ?orgAcronym.}\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "?orgLabel bds:search \"@#$%TERM%$#@\".\n"
                + "?orgLabel bds:relevance ?score.\n"
                + "}ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "OPTIONAL { \n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "}\n"
                + "OPTIONAL {?org cerif:has_acronym ?orgAcronym. }\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select distinct (?orgName as ?name) (?orgAcronym as ?acronym) ?Service (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org rdfs:label ?orgLabel.\n"
                + "OPTIONAL { \n ?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "}\n"
                + "OPTIONAL {?org cerif:has_acronym ?orgAcronym.}\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///     
                //                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                //                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                //                + "}\n"
                ///
                + "?orgLabel bds:search \"@#$%TERM%$#@\".\n"
                + "?orgLabel bds:relevance ?score.\n"
                + "}ORDER BY desc(?score) \n",
                false,
                "distinct (?@#$%VAR%$#@Name as ?name) (?@#$%VAR%$#@Acronym as ?acronym) ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "OPTIONAL {"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "}\n"
                //                + "?@#$%VAR%$#@ a cerif:OrganisationUnit.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Label.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.}",
                //                "OPTIONAL {?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Label}. \n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name. \n"
                //                + "bind(if(bound(?@#$%VAR%$#@Label), ?@#$%VAR%$#@Label, ?@#$%VAR%$#@Name) as ?@#$%VAR%$#@FinalName). \n"
                //                + "?@#$%VAR%$#@FinalName bds:search \"@#$%TERM%$#@\"."
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Label. \n"
                + "?@#$%VAR%$#@Label bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                //                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)"
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "org"
        );
        insertEntity("Product",
                "http://eurocris.org/ontology/cerif#Product",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                ///
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                false,
                "distinct ?@#$%VAR%$#@Νame  ?Responsible ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Νame.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "Optional {\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Νame.\n"
                + "?@#$%VAR%$#@Νame bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                //                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "prod"
        );
        insertEntity("Equipment",
                "http://eurocris.org/ontology/cerif#Equipment",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                ///
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                ///
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                false,
                "distinct ?@#$%VAR%$#@Νame  ?Responsible ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "Optional {\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                //                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "eq"
        );
        insertEntity("Facility",
                "http://eurocris.org/ontology/cerif#Facility",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                ///
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?Service (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                ///
                + "?name bds:search \"@#$%TERM%$#@\".\n"
                + "?name bds:relevance ?score.\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ORDER BY desc(?score) \n",
                false,
                "distinct ?@#$%VAR%$#@Νame  ?Responsible ?Service (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "Optional {\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                //                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?@#$%VAR%$#@GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "fac"
        );
        insertEntity("Location",
                "http://eurocris.org/ontology/cerif#GeographicBoundingBox",
                "",
                "SELECT DISTINCT ?address ?Service (?addr as ?uri) ?east ?west ?north ?south  @#$%FROM%$#@ WHERE {\n"
                + "?addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                + "?addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"
                + "?addr rdfs:label ?address.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?loc.\n"
                + "?loc a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "##\n"
                + "?address bds:search \"@#$%TERM%$#@\".\n"
                + "?address bds:relevance ?score.\n"
                + "##\n"
                + "} ORDER BY desc(?score)",
                "SELECT DISTINCT ?address ?Service (?loc as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ WHERE {\n"
                + "?addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                + "?addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"
                + "?addr rdfs:label ?address.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?loc.\n"
                + "?loc a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "##\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?GBB <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                ///
                + "}",
                "SELECT DISTINCT ?address ?Service (?loc as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ WHERE {\n"
                + "?addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                + "?addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"
                + "?addr rdfs:label ?address.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?loc.\n"
                + "?loc a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "##\n"
                + "?address bds:search \"@#$%TERM%$#@\".\n"
                + "?address bds:relevance ?score.\n"
                + "##\n"
                //                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?loc <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n"
                ///
                + "} ORDER BY desc(?score)",
                false,
                "distinct (?@#$%VAR%$#@address as ?address) ?@#$%VAR%$#@east ?@#$%VAR%$#@west ?@#$%VAR%$#@north ?@#$%VAR%$#@south ?Service (?@#$%VAR%$#@ as ?uri)",
                ///
                "?@#$%VAR%$#@addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                + "?@#$%VAR%$#@addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"
                + "?@#$%VAR%$#@addr rdfs:label ?@#$%VAR%$#@Name.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@.\n"
                + "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.",
                ///
                "?@#$%VAR%$#@addr a <http://eurocris.org/ontology/cerif#PostalAddress>. \n"
                + "?@#$%VAR%$#@addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?@#$%TARGET%$#@].\n"
                + "?@#$%VAR%$#@addr rdfs:label ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bds:search \"@#$%TERM%$#@\".",
                ///
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                //                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                + "SERVICE <http://www.bigdata.com/rdf/geospatial#search> {\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#search> \"inRectangle\" .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-west> .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .    \n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/north-east> .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-east> .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#predicate> <http://location/south-west> .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#searchDatatype> <http://www.bigdata.com/rdf/geospatial/literals/v1#lat-lon> .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#spatialRectangleSouthWest> \"@#$%SOUTH%$#@#@#$%WEST%$#@\" .\n"
                + " ?@#$%VAR%$#@ <http://www.bigdata.com/rdf/geospatial#spatialRectangleNorthEast> \"@#$%NORTH%$#@#@#$%EAST%$#@\" .\n"
                + "}\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "loc"
        );
    }

    private void insertEntitiesVirtuoso() throws SQLException {
        insertEntity("Person",
                "http://eurocris.org/ontology/cerif#Person",
                "thesaurus/persons-firstAndLastNames.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?persName as ?name ?collection (?pers as ?uri) @#$%FROM%$#@\n"
                + "where {\n"
                + "?pers a cerif:Person.  \n"
                + "?pers <http://in_graph> ?collection. \n"
                //                + "OPTIONAL {?pers <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.  \n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.  \n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                //                + "?Ser cerif:has_acronym ?Service.\n}"
                + "?pers rdfs:label ?persName. \n"
                + "?persName bif:contains \"@#$%TERM%$#@\".  \n"
                + "} \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Name as ?name) ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "?pers <http://in_graph> ?collection. \n"
                //                + "OPTIONAL {?@#$%VAR%$#@  <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.  \n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service. }\n"
                //                + "?@#$%VAR%$#@ a cerif:Person.  \n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name. \n",
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name. \n"
                + "?@#$%VAR%$#@Name bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "pers"
        );
        insertEntity("Project",
                "http://eurocris.org/ontology/cerif#Project",
                "thesaurus/project-acronyms.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?projectTitle as ?title) ?projectAcronym ?collection (?proj as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                //                + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?proj <http://in_graph> ?collection. \n"
                + "?proj a cerif:Project.\n"
                + "?proj cerif:has_title ?projectTitle.\n"
                + "OPTIONAL {?proj cerif:has_URI ?projURI.}\n"
                + "BIND(if(bound(?projURI),?projURI,?proj) as ?projectURI).\n"
                + "?proj cerif:has_acronym ?projectAcronym.\n"
                + "?proj rdfs:label ?projName.\n"
                + "?projName bif:contains \"@#$%TERM%$#@\".\n"
                + "} \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Title as ?title) (?@#$%VAR%$#@Name as ?name) (?@#$%VAR%$#@Acronym as ?acronym) ?collection (?@#$%VAR%$#@ as ?uri)",
                //                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Project>.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_URI> ?@#$%VAR%$#@URI.}\n"
                + "BIND(if(bound(?@#$%VAR%$#@URI),?@#$%VAR%$#@URI,?@#$%VAR%$#@) as ?@#$%VAR%$#@).\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.\n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name.\n",
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@Name bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "proj"
        );
        insertEntity("Publication",
                "http://eurocris.org/ontology/cerif#Publication",
                "thesaurus/publications-titles.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct (?pubTitle as ?title) (?pubDate as ?publication_date) ?collection (?pub as ?uri) @#$%FROM%$#@ \n"
                + "where {\n"
                //                + "?pub <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"

                + "?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "?pub <http://in_graph> ?collection. \n"
                + "?pub <http://eurocris.org/ontology/cerif#has_title> ?pubTitle.\n"
                + "?pub <http://eurocris.org/ontology/cerif#has_publicationDate> ?pubDate.\n"
                + "?pubTitle bif:contains \"@#$%TERM%$#@\".\n"
                + "} \n",
                "",
                "",
                false,
                "distinct (?@#$%VAR%$#@Title as ?title) (?@#$%VAR%$#@Date as ?publication_date) ?collection (?@#$%VAR%$#@ as ?uri)",
                //                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?@#$%VAR%$#@ a cerif:Publication.\n"
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_publicationDate> ?@#$%VAR%$#@Date.\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_title> ?@#$%VAR%$#@Title.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "?@#$%VAR%$#@Title bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "pub"
        );
        insertEntity("Organisation",
                "http://eurocris.org/ontology/cerif#OrganisationUnit",
                "thesaurus/organizationUnits-acronyms.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct GROUP_CONCAT(distinct ?orgName ; separator=\", \") as ?name ?acronym ?collection (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org <http://in_graph> ?collection. \n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org <http://searchable_text> ?orgLabel. ?orgLabel bif:contains \"@#$%TERM%$#@\".\n "
                + "OPTIONAL { \n ?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                + "?Ser cerif:has_acronym ?Service. }\n"
                + "OPTIONAL {?org cerif:has_acronym ?orgAcronym.}\n"
                + "bind (if(bound(?orgAcronym), ?orgAcronym, \"-\") as ?acronym).\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "} \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select distinct GROUP_CONCAT(distinct ?orgName ; separator=\", \") as ?name ?acronym ?collection (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org a cerif:OrganisationUnit.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org <http://in_graph> ?collection. \n"
                //                + "OPTIONAL { \n"
                //                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                //                + "}\n"
                + "OPTIONAL {?org cerif:has_acronym ?orgAcronym. }\n"
                + "bind (if(bound(?orgAcronym), ?orgAcronym, \"-\") as ?acronym).\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#> \n"
                + "select distinct GROUP_CONCAT(distinct ?orgName ; separator=\", \") as ?name ?acronym ?collection (?org as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "where {\n"
                + "?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "?org cerif:has_name ?orgName.\n"
                + "?org <http://searchable_text> ?orgLabel. ?orgLabel bif:contains \"@#$%TERM%$#@\".\n "
                + "?org <http://in_graph> ?collection. \n"
                //                + "OPTIONAL { \n ?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                //                + "}\n"
                + "OPTIONAL {?org cerif:has_acronym ?orgAcronym.}\n"
                + "bind (if(bound(?orgAcronym), ?orgAcronym, \"-\") as ?acronym).\n"
                + "?org <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "} \n",
                false,
                "distinct GROUP_CONCAT(distinct ?@#$%VAR%$#@Name ; separator=\", \") as ?name ?acronym ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                //                + "OPTIONAL {"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service. }\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.\n"
                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.} \n"
                + "bind (if(bound(?@#$%VAR%$#@Acronym), ?@#$%VAR%$#@Acronym, \"-\") as ?acronym).\n",
                //                "OPTIONAL {?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Label}. \n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name. \n"
                //                + "bind(if(bound(?@#$%VAR%$#@Label), ?@#$%VAR%$#@Label, ?@#$%VAR%$#@Name) as ?@#$%VAR%$#@FinalName). \n"
                //                + "?@#$%VAR%$#@FinalName bds:search \"@#$%TERM%$#@\"."
                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label. \n"
                + "?@#$%VAR%$#@Label bif:contains \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "org"
        );
        insertEntity("Product",
                "http://eurocris.org/ontology/cerif#Product",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?collection (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://in_graph> ?collection. \n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "?name bif:contains \"@#$%TERM%$#@\".\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} \n",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  ?Responsible ?collection (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "}",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name ?Responsible ?collection (?object as ?uri) ?east ?west ?north ?south \n"
                + "@#$%FROM%$#@\n"
                + "WHERE {\n"
                + "?object a cerif:Product.\n"
                + "?object cerif:has_name ?name.\n"
                + "?object <http://in_graph> ?collection. \n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@)\n"
                ///
                + "?name bif:contains \"@#$%TERM%$#@\".\n"
                + "Optional {\n"
                + "?object cerif:is_destination_of ?FLE3.\n"
                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} \n",
                false,
                "distinct ?@#$%VAR%$#@Νame  ?Responsible ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Νame.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "Optional {\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                + "optional {\n"
                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                + "optional{\n"
                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                + "}\n"
                + "}\n"
                + "}\n"
                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Νame.\n"
                + "?@#$%VAR%$#@Νame bif:contains \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                + "?@#$%VAR%$#@FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "prod"
        );

        insertEntity("Dataset",
                "http://eurocris.org/ontology/cerif#Dataset",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?datasetKeyw ; separator=\", \") as ?dataset_Params ?collection (?object as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Dataset.\n"
                + "OPTIONAL {?object cerif:has_name ?name.}\n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?datasetKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "#FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                + "?object <http://searchable_text> ?text. \n"
                + "?text bif:contains \"@#$%TERM%$#@\".\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?datasetKeyw ; separator=\", \") as ?dataset_Params ?collection (?object as ?uri) ?east ?west ?north ?south  @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Dataset.\n"
                + "OPTIONAL {?object cerif:has_name ?name.} \n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?datasetKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?datasetKeyw ; separator=\", \") as ?dataset_Params ?Service (?object as ?uri) ?east ?west ?north ?south  @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Dataset.\n"
                + "OPTIONAL {?object cerif:has_name ?name.}\n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?datasetKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                + "?object <http://searchable_text> ?text. \n"
                + "?text bif:contains \"@#$%TERM%$#@\".\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                false,
                "distinct ?@#$%VAR%$#@Name as ?name GROUP_CONCAT(distinct ?@#$%VAR%$#@Keyw ; separator=\", \") as ?@#$%VAR%$#@_Params ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords> ?@#$%VAR%$#@Keyw.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.} \n"
                //                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr). \n"
                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n" //                  "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n" //                + "Optional {\n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                //                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                ,
                //                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                //                + 
                "?@#$%VAR%$#@Text bif:contains \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "# ?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "# ?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "#FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "# ?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "# ?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "# FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "dataset"
        );

        insertEntity("Equipment",
                "http://eurocris.org/ontology/cerif#Equipment",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?equipKeyw ; separator=\", \") as ?equipment_keywords ?collection (?object as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "OPTIONAL {?object cerif:has_name ?name.}\n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?equipKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "#FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                + "?object <http://searchable_text> ?text. \n"
                + "?text bif:contains \"@#$%TERM%$#@\".\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?equipKeyw ; separator=\", \") as ?equipment_keywords ?collection (?object as ?uri) ?east ?west ?north ?south  @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "OPTIONAL {?object cerif:has_name ?name.} \n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?equipKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?equipKeyw ; separator=\", \") as ?equipment_keywords ?collection (?object as ?uri) ?east ?west ?north ?south  @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Equipment.\n"
                + "OPTIONAL {?object cerif:has_name ?name.}\n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?equipKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                + "?object <http://searchable_text> ?text. \n"
                + "?text bif:contains \"@#$%TERM%$#@\".\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                false,
                "distinct ?name GROUP_CONCAT(distinct ?@#$%VAR%$#@Keyw ; separator=\", \") as ?keywords "
                //                        + "?@#$%VAR%$#@Acronym as ?acronym ?Responsible "
                + "?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords> ?@#$%VAR%$#@Keywords.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.} \n"
                //                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr). \n"
                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "BIND(if(bound(?@#$%VAR%$#@Keywords),?@#$%VAR%$#@Keywords,\"-\") as ?@#$%VAR%$#@Keyw).\n"
                + "BIND(if(bound(?@#$%VAR%$#@Name),?@#$%VAR%$#@Name,\"-\") as ?name).\n" //                + "Optional {\n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                //                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                ,
                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                + "?@#$%VAR%$#@Text bif:contains \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "#FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "# FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "eq"
        );

        insertEntity("Facility",
                "http://eurocris.org/ontology/cerif#Facility",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?facilKeyw ; separator=\", \") as ?facility_keywords ?collection (?object as ?uri) ?east ?west ?north ?south @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "OPTIONAL {?object cerif:has_name ?name.}\n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?facilKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "#FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                + "?object <http://searchable_text> ?text. \n"
                + "?text bif:contains \"@#$%TERM%$#@\".\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?facilKeyw ; separator=\", \") as ?facility_keywords ?collection (?object as ?uri) ?east ?west ?north ?south  @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "OPTIONAL {?object cerif:has_name ?name.} \n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?facilKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name  GROUP_CONCAT(distinct ?facilKeyw ; separator=\", \") as ?facility_keywords ?collection (?object as ?uri) ?east ?west ?north ?south  @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Facility.\n"
                + "OPTIONAL {?object cerif:has_name ?name.}\n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?facilKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@).\n"
                + "?object <http://searchable_text> ?text. \n"
                + "?text bif:contains \"@#$%TERM%$#@\".\n"
                //                + "Optional {\n"
                //                + "?object cerif:is_destination_of ?FLE3.\n"
                //                + "?FLE3 cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 cerif:has_source ?OUorP. ?OUorP cerif:has_name ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU cerif:has_name ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n"
                + "} ",
                false,
                "distinct ?name GROUP_CONCAT(distinct ?@#$%VAR%$#@Keyw ; separator=\", \") as ?keywords "
                //                        + "?@#$%VAR%$#@Acronym as ?acronym ?Responsible "
                + "?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords> ?@#$%VAR%$#@Keywords.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.} \n"
                //                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr). \n"
                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "BIND(if(bound(?@#$%VAR%$#@Keywords),?@#$%VAR%$#@Keywords,\"-\") as ?@#$%VAR%$#@Keyw).\n"
                + "BIND(if(bound(?@#$%VAR%$#@Name),?@#$%VAR%$#@Name,\"-\") as ?name).\n" //                + "Optional {\n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_destination_of> ?FLE3.\n"
                //                + "?FLE3 <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.Responsible>.\n"
                //                + "optional {\n"
                //                + "?FLE3 <http://eurocris.org/ontology/cerif#has_source> ?OUorP. ?OUorP <http://eurocris.org/ontology/cerif#has_name> ?nameOUorP.\n"
                //                + "optional{\n"
                //                + "?OUorP <http://eurocris.org/ontology/cerif#is_source_of> ?FLE4.?FLE4 <http://eurocris.org/ontology/cerif#has_destination> ?OU. ?OU <http://eurocris.org/ontology/cerif#has_name> ?nameOU.\n"
                //                + "}\n"
                //                + "}\n"
                //                + "}\n"
                //                + "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible).\n",
                ,
                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                + "?@#$%VAR%$#@Text bif:contains \"@#$%TERM%$#@\".",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "#FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@GBB.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "# FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "fac"
        );

        insertEntity("Software",
                "http://eurocris.org/ontology/cerif#Software",
                "",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "SELECT DISTINCT ?name GROUP_CONCAT(distinct ?softKeyw ; separator=\", \") as ?software_keywords ?collection (?object as ?uri) @#$%FROM%$#@ \n"
                + "WHERE {\n"
                + "?object a cerif:Software.\n"
                + "OPTIONAL {?object cerif:has_name ?name.}\n"
                + "OPTIONAL {?object <http://eurocris.org/ontology/cerif#has_keywords> ?softKeyw.} \n"
                //                + "OPTIONAL {?object cerif:has_description ?description.}\n"
                //                + "OPTIONAL {?object cerif:has_acronym ?acronym.}\n"
                //                + "?object <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser cerif:has_acronym ?Service.\n"
                + "?object <http://in_graph> ?collection. \n"
                + "?object <http://searchable_text> ?text. \n"
                + "?text bif:contains \"@#$%TERM%$#@\".\n"
                + "} ",
                "",
                "",
                false,
                "distinct ?name GROUP_CONCAT(distinct ?@#$%VAR%$#@Keyw ; separator=\", \") as ?keywords "
                //                        + "?@#$%VAR%$#@Acronym as ?acronym ?Responsible "
                + "?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name> ?@#$%VAR%$#@Name.}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords> ?@#$%VAR%$#@Keywords.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description.} \n"
                //                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_acronym> ?@#$%VAR%$#@Acronym.} \n"
                //                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr). \n"
                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                //                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "BIND(if(bound(?@#$%VAR%$#@Keywords),?@#$%VAR%$#@Keywords,\"-\") as ?@#$%VAR%$#@Keyw).\n"
                + "BIND(if(bound(?@#$%VAR%$#@Name),?@#$%VAR%$#@Name,\"-\") as ?name).\n",
                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Text.\n"
                + "?@#$%VAR%$#@Text bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "software"
        );
        insertEntity("Location",
                "http://eurocris.org/ontology/cerif#GeographicBoundingBox",
                "",
                "SELECT DISTINCT (?loc as ?uri) ?location ?east ?west ?north ?south  @#$%FROM%$#@ WHERE {\n"
                //                + "?addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                //                + "?addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"
                //                + "?addr rdfs:label ?address.\n"
                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                //                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?loc.\n"
                + "?loc a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "##\n"
                + "?loc rdfs:label ?location. ?location bif:contains \"@#$%TERM%$#@\".\n"
                + "##\n"
                + "} ",
                "SELECT DISTINCT (?loc as ?uri) ?location ?east ?west ?north ?south  @#$%FROM%$#@ WHERE {\n"
                //                + "?addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                //                + "?addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"

                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                //                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?loc.\n"
                + "?loc a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?loc rdfs:label ?location. \n"
                + "?loc <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "##\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@). \n"
                + "}",
                "SELECT DISTINCT (?loc as ?uri) ?location ?east ?west ?north ?south  @#$%FROM%$#@ WHERE {\n"
                //                + "?addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                //                + "?addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"
                //                + "?addr rdfs:label ?address.\n"
                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                //                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?loc.\n"
                + "?loc a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "?loc <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "##\n"
                + "?loc rdfs:label ?location. ?address bif:contains \"@#$%TERM%$#@\".\n"
                + "##\n"
                + "FILTER(xsd:float(?east) <= @#$%EAST%$#@ && xsd:float(?west) >= @#$%WEST%$#@ && xsd:float(?north) <= @#$%NORTH%$#@ && xsd:float(?south) >= @#$%SOUTH%$#@). \n"
                ///
                + "}",
                false,
                "distinct (?@#$%VAR%$#@address as ?address) ?@#$%VAR%$#@east ?@#$%VAR%$#@west ?@#$%VAR%$#@north ?@#$%VAR%$#@south ?Service (?@#$%VAR%$#@ as ?uri)",
                ///
                //                "?@#$%VAR%$#@addr a <http://eurocris.org/ontology/cerif#PostalAddress>.\n"
                //                + "?@#$%VAR%$#@addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?entity].\n"
                //                + "?@#$%VAR%$#@addr rdfs:label ?@#$%VAR%$#@Name.\n"
                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?FLES.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_destination> ?Ser.\n"
                //                + "?FLES <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
                //                + "?Ser <http://eurocris.org/ontology/cerif#has_acronym> ?Service.\n"
                //                + "?entity <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE1.\n"
                //                + "?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@PA.\n"
                //                + "?@#$%VAR%$#@PA <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@FLE2.\n"
                //                + "?@#$%VAR%$#@FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@.\n"
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#GeographicBoundingBox>.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.",
                ///
                //                "?@#$%VAR%$#@addr a <http://eurocris.org/ontology/cerif#PostalAddress>. \n"
                //                + "?@#$%VAR%$#@addr <http://eurocris.org/ontology/cerif#is_destination_of> [<http://eurocris.org/ontology/cerif#has_source> ?@#$%TARGET%$#@].\n"
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Location. ?@#$%VAR%$#@?@#$%VAR%$#@Location bif:contains \"@#$%TERM%$#@\". \n",
                ///
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@).",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?@#$%VAR%$#@east.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?@#$%VAR%$#@west.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?@#$%VAR%$#@north.\n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?@#$%VAR%$#@south.\n"
                + "FILTER(xsd:float(?@#$%VAR%$#@east) <= @#$%EAST%$#@ && xsd:float(?@#$%VAR%$#@west) >= @#$%WEST%$#@ && xsd:float(?@#$%VAR%$#@north) <= @#$%NORTH%$#@ && xsd:float(?@#$%VAR%$#@south) >= @#$%SOUTH%$#@)",
                "loc"
        );
        insertEntity("Service",
                "http://eurocris.org/ontology/cerif#Service",
                "thesaurus/persons-firstAndLastNames.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?servName as ?name ?servUri ?servDescription ?collection (?serv as ?uri) @#$%FROM%$#@\n"
                + "where {\n"
                + "?serv a cerif:Service. \n"
                + "?serv <http://in_graph> ?collection. \n"
                //                + "?serv cerif:is_source_of ?sle. \n"
                //                + "?sle cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.WebService>.\n"
                + "OPTIONAL {?service_uri cerif:has_URI         ?servUri .}\n"
                + "OPTIONAL {?service_uri cerif:has_description ?servDescription .}\n"
                + "#OPTIONAL {?service_uri cerif:has_keywords    ?servKeywords .}\n"
                + "OPTIONAL {?service_uri cerif:has_name        ?servName .}\n"
                + "?serv rdfs:label ?servLabel. \n"
                + "?servLabel bif:contains \"@#$%TERM%$#@\". \n"
                + "} ",
                "",
                "",
                false,
                "distinct ?@#$%VAR%$#@Name as ?name ?@#$%VAR%$#@Medium as ?wadl_file ?@#$%VAR%$#@Uri as ?service_uri ?@#$%VAR%$#@Descr as ?description GROUP_CONCAT(distinct ?@#$%VAR%$#@Keyw ; separator=\", \") as ?@#$%VAR%$#@_Params ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@sle.\n"
                + "?@#$%VAR%$#@sle <http://eurocris.org/ontology/cerif#has_classification> <http://139.91.183.70:8090/vre4eic/Classification.WebService>.\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_URI>         ?@#$%VAR%$#@Uri .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords>    ?@#$%VAR%$#@Keywords .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name>        ?@#$%VAR%$#@Name .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@mediumLE .\n"
                + "?@#$%VAR%$#@mediumLE  <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@Medium.\n"
                + "?@#$%VAR%$#@Medium a <http://eurocris.org/ontology/cerif#Medium>. }\n"
                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr).\n"
                + "BIND(if(bound(?@#$%VAR%$#@Keywords),?@#$%VAR%$#@Keywords,\"-\") as ?@#$%VAR%$#@Keyw).\n"
                + "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Label. \n",
                "?@#$%VAR%$#@ rdfs:label ?@#$%VAR%$#@Label. \n"
                + "?@#$%VAR%$#@Label bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "serv"
        );

        insertEntity("WebService",
                "http://eurocris.org/ontology/cerif#WebService",
                "thesaurus/persons-firstAndLastNames.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?servName as ?name ?servUri GROUP_CONCAT(distinct ?servKeywords ; separator=\", \") as ?params ?collection (?serv as ?uri) @#$%FROM%$#@\n"
                + "where {\n"
                + "?serv a cerif:WebService. \n"
                + "?serv <http://in_graph> ?collection. \n"
                + "OPTIONAL {?service_uri cerif:has_URI         ?servUri .}\n"
                //                + "OPTIONAL {?service_uri cerif:has_description ?servDescription .}\n"
                + "#OPTIONAL {?service_uri cerif:has_keywords    ?servKeywords .}\n"
                + "OPTIONAL {?service_uri cerif:has_name        ?servName .}\n"
                + "?serv <http://searchable_text> ?servLabel. \n"
                + "?servLabel bif:contains \"@#$%TERM%$#@\". \n"
                + "} ",
                "",
                "",
                false,
                "distinct ?@#$%VAR%$#@Name as ?name ?wadl_file ?@#$%VAR%$#@Uri as ?webservice_uri GROUP_CONCAT(distinct ?@#$%VAR%$#@Keyw ; separator=\", \") as ?@#$%VAR%$#@_Params ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_URI>         ?@#$%VAR%$#@Uri .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords>    ?@#$%VAR%$#@Keywords .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name>        ?@#$%VAR%$#@Name .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@mediumLE .\n"
                + "?@#$%VAR%$#@mediumLE  <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@Medium.\n"
                + "?@#$%VAR%$#@Medium a <http://eurocris.org/ontology/cerif#Medium>. }\n"
                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr).\n"
                + "BIND(if(bound(?@#$%VAR%$#@Keywords),?@#$%VAR%$#@Keywords,\"-\") as ?@#$%VAR%$#@Keyw).\n"
                + "BIND(if(bound(?@#$%VAR%$#@Medium),?@#$%VAR%$#@Medium,\"-\") as ?wadl_file).\n"
                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label. \n",
                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label. \n"
                + "?@#$%VAR%$#@Label bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "webserv"
        );

        insertEntity("Workflow",
                "http://eurocris.org/ontology/cerif#Workflow",
                "thesaurus/persons-firstAndLastNames.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?name ?wflowUri ?wflowDescription ?collection (?serv as ?uri) @#$%FROM%$#@\n"
                + "where {\n"
                + "?serv a cerif:Workflow. \n"
                + "?serv <http://in_graph> ?collection. \n"
                + "OPTIONAL {?serv cerif:has_URI         ?wflowUri .}\n"
                + "OPTIONAL {?serv cerif:has_description ?wflowDescription .}\n"
                + "#OPTIONAL {?serv cerif:has_keywords    ?wflowKeywords .}\n"
                + "OPTIONAL {?serv cerif:has_name        ?name .}\n"
                + "?serv <http://searchable_text> ?wflowLabel. \n"
                + "?wflowLabel bif:contains \"@#$%TERM%$#@\". \n"
                + "} ",
                "",
                "",
                false,
                "distinct ?@#$%VAR%$#@Name as ?name ?@#$%VAR%$#@Uri as ?workflow_uri ?@#$%VAR%$#@Descr as ?description ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Workflow>.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_URI>         ?@#$%VAR%$#@Uri .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description .}\n"
                + "#OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords>    ?@#$%VAR%$#@Keywords .}\n"
                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name>        ?@#$%VAR%$#@Name .}\n"
                + "#OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@mediumLE .\n"
                + "#?@#$%VAR%$#@mediumLE  <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@Medium.\n"
                + "#?@#$%VAR%$#@Medium a <http://eurocris.org/ontology/cerif#Medium>. }\n"
                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr).\n"
                + "#BIND(if(bound(?@#$%VAR%$#@Keywords),?@#$%VAR%$#@Keywords,\"-\") as ?@#$%VAR%$#@Keyw).\n"
                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label. \n",
                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label. \n"
                + "?@#$%VAR%$#@Label bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "workfl"
        );

        insertEntity("Classification",
                "http://eurocris.org/ontology/cerif#Classification",
                "thesaurus/classification_terms.json",
                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
                + "select distinct ?name ?collection (?c as ?uri) @#$%FROM%$#@\n"
                + "where {\n"
                + "?c a cerif:Classification. \n"
                + "?c <http://eurocris.org/ontology/cerif#has_term> ?name . \n"
                + "?c <http://in_graph> ?collection.\n"
                + "?name bif:contains \"@#$%TERM%$#@\". \n"
                + "} ",
                "",
                "",
                false,
                "distinct ?@#$%VAR%$#@Name as ?name ?@#$%VAR%$#@Uri as ?classif_uri ?collection (?@#$%VAR%$#@ as ?uri)",
                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Classification>.\n"
                + "?@#$%VAR%$#@ <http://in_graph> ?collection. \n"
                + "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_term> ?@#$%VAR%$#@Name. \n",
                "?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_term> ?@#$%VAR%$#@Name .\n"
                + "?@#$%VAR%$#@Name bif:contains \"@#$%TERM%$#@\".",
                "",
                "",
                "classif"
        );
    }

    private void insertRelationsMatUpdates() throws SQLException {
        insertRelationMatUpdate("insert_classifications", "insert into @#$%FROM%$#@ {\n"
                + "?s ?p ?o.\n"
                + "} where \n"
                + "{\n"
                + "select * from <http://vre/classifications>  where {\n"
                + "?s ?p ?o.\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_organisation", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(GROUP_CONCAT(distinct ?name ; separator=\", \"),\" \", ?acronym) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_acronym>  ?acronym .}\n"
                + "?serv <http://eurocris.org/ontology/cerif#has_name>     ?name .\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_dataset", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");
        insertRelationMatUpdate("searchable_text_software", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?abstr,\" \", ?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_abstract>        ?servAbstract .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "BIND(if(bound(?servDescription),?servAbstract,\"-\") as ?abstr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_equipment", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_facility", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_webservice", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("has_source",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#has_source> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#is_source_of> ?s.\n"
                + "}");
        insertRelationMatUpdate("is_source_of",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#is_source_of> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#has_source> ?s.\n"
                + "}");
        insertRelationMatUpdate("has_destination",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#has_destination> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#is_destination_of> ?s.\n"
                + "}");
        insertRelationMatUpdate("is_destination_of",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#is_destination_of> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#has_destination> ?s.\n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Publication",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?project_pub ?pub. \n"
                + "  ?pub ?pub_project ?org. \n"
                + "} WHERE { \n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>. \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>. \n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?op. \n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?op. \n"
                + "  ?op <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Publication/\",encode_for_uri(?role) )) as ?orgunit_pub ). \n"
                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#Publication-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?pub_orgunit ). \n"
                + "}");
        insertRelationMatUpdate("Person-OrganisationUnit",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?person_orgunit ?org. \n"
                + "  ?org ?orgunit_person ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>. \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>. \n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-OrganisationUnit/\",encode_for_uri(?role) )) as ?person_orgunit ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Person/\",encode_for_uri(?role_opposite) )) as ?orgunit_person ) \n"
                + "}");
        insertRelationMatUpdate("Person-Person", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_pers1 ?pers1. \n"
                + "  ?pers1 ?pers_pers2 ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?pers1 a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?pers1 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Person/\",encode_for_uri(?role) )) as ?pers_pers1 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Person/\",encode_for_uri(?role_opposite) )) as ?pers_pers2). \n"
                + "}");
        insertRelationMatUpdate("Person-Project",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?person_project ?proj. \n"
                + "  ?proj ?project_person ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>. \n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>. \n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp. \n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Project/\",encode_for_uri(?role) )) as ?person_project ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Project-Person/\",encode_for_uri(?role_opposite) )) as ?project_person ). \n"
                + "}");
        insertRelationMatUpdate("Person-Publication",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?person_publication ?pub. \n"
                + "  ?pub ?publication_person ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>. \n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>. \n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pp. \n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Publication/\",encode_for_uri(?role) )) as ?person_publication ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Publication-Person/\",encode_for_uri(?role_opposite) )) as ?publication_person ). \n"
                + "}");
        insertRelationMatUpdate("Project-Publication",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?proj ?project_pub ?pub. \n"
                + "  ?pub ?pub_project ?proj. \n"
                + "} WHERE { \n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>. \n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>. \n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp. \n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Project-Publication/\",encode_for_uri(?role) )) as ?project_pub ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Publication-Project/\",encode_for_uri(?role_opposite) )) as ?pub_project ). \n"
                + "}");
        insertRelationMatUpdate("Project-OrganisationUnit",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?proj ?project_org ?org. \n"
                + "  ?org ?org_project ?proj. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>. \n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>. \n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pou. \n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.    \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role_opposite. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Project-OrganisationUnit/\",encode_for_uri(?role) )) as ?project_org ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Project/\",encode_for_uri(?role_opposite) )) as ?org_project ). \n"
                + "}");
        insertRelationMatUpdate("Person-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Project-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Project>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Project-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Publication-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Publication-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Product-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Product-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Dataset-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Dataset-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Equipment-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Equipment-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Facility-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Facility-Location/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Product", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_product ?prod. \n"
                + "  ?prod ?product_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?prod a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?prod <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role_opposite.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Product/\",encode_for_uri(?role) )) as ?org_product ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Product-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?product_org ). \n"
                + "}");
        insertRelationMatUpdate("Person-Product", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_product ?prod. \n"
                + "  ?prod ?product_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?prod a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?prod <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role_opposite.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Product/\",encode_for_uri(?role) )) as ?pers_product ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Product-Person/\",encode_for_uri(?role_opposite) )) as ?product_pers ). \n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Facility", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_fac ?fac. \n"
                + "  ?fac ?fac_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?fac a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?fac <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Facility/\",encode_for_uri(?role) )) as ?org_fac ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Facility-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?fac_org ). \n"
                + "}");
        insertRelationMatUpdate("Person-Facility", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_fac ?fac. \n"
                + "  ?fac ?fac_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?fac a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?fac <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Facility/\",encode_for_uri(?role) )) as ?pers_fac ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Facility-Person/\",encode_for_uri(?role_opposite) )) as ?fac_pers ). \n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Equipment", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_eq ?eq. \n"
                + "  ?eq ?eq_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?eq a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?eq <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Equipment/\",encode_for_uri(?role) )) as ?org_eq ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Equipment-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?eq_org ). \n"
                + "}");
        insertRelationMatUpdate("Person-Equipment", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_eq ?eq. \n"
                + "  ?eq ?eq_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?eq a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?eq <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Equipment/\",encode_for_uri(?role) )) as ?pers_eq ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Equipment-Person/\",encode_for_uri(?role_opposite) )) as ?eq_pers ). \n"
                + "}");

        insertRelationMatUpdate("Person-Service", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_ser ?ser. \n"
                + "  ?ser ?ser_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Service/\",encode_for_uri(?role) )) as ?pers_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Service-Person/\",encode_for_uri(?role_opposite) )) as ?ser_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-Service", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_ser ?ser. \n"
                + "  ?ser ?ser_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Service/\",encode_for_uri(?role) )) as ?org_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Service-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
                + "}");

        insertRelationMatUpdate("Service-Service", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?ser1 ?ser1_ser2 ?ser2. \n"
                + "  ?ser2 ?ser2_ser1 ?pers1. \n"
                + "} WHERE { \n"
                + "  ?ser1 a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "  ?ser2 a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "\n"
                + "   ?ser1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Service-Service/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Service-Service/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");
        ///
        insertRelationMatUpdate("Person-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_ser ?ser. \n"
                + "  ?ser ?ser_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-WebService/\",encode_for_uri(?role) )) as ?pers_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-Person/\",encode_for_uri(?role_opposite) )) as ?ser_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_ser ?ser. \n"
                + "  ?ser ?ser_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-WebService/\",encode_for_uri(?role) )) as ?org_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
                + "}");

        insertRelationMatUpdate("WebService-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?ser1 ?ser1_ser2 ?ser2. \n"
                + "  ?ser2 ?ser2_ser1 ?pers1. \n"
                + "} WHERE { \n"
                + "  ?ser1 a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "  ?ser2 a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?ser1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-WebService/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-WebService/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Person-Software", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_soft ?soft. \n"
                + "  ?soft ?soft_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?soft a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?soft <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Software/\",encode_for_uri(?role) )) as ?pers_soft ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Software-Person/\",encode_for_uri(?role_opposite) )) as ?soft_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-Software", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_soft ?soft. \n"
                + "  ?soft ?soft_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?soft a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?soft <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Software/\",encode_for_uri(?role) )) as ?org_soft ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Software-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?soft_org ). \n"
                + "}");

        insertRelationMatUpdate("Person-Dataset", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_data ?data. \n"
                + "  ?data ?data_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Dataset/\",encode_for_uri(?role) )) as ?pers_data ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Dataset-Person/\",encode_for_uri(?role_opposite) )) as ?data_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-Dataset", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_ser ?data. \n"
                + "  ?data ?ser_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Dataset/\",encode_for_uri(?role) )) as ?org_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Dataset-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
                + "}");

        insertRelationMatUpdate("Software-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?soft ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?soft. \n"
                + "} WHERE { \n"
                + "  ?soft a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Software-WebService/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-Software/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Dataset-Dataset", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data1 ?ser1_ser2 ?data2. \n"
                + "  ?data2 ?ser2_ser1 ?data1. \n"
                + "} WHERE { \n"
                + "  ?data1 a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "  ?data2 a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "\n"
                + "   ?data1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?data2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Dataset-Dataset/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Dataset-Dataset/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Dataset-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?data. \n"
                + "} WHERE { \n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Dataset-WebService/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-Dataset/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Equipment-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?data. \n"
                + "} WHERE { \n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Equipment-WebService/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-Equipment/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Facility-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?data. \n"
                + "} WHERE { \n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Facility-WebService/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#WebService-Facility/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");
        ////
        insertRelationMatUpdate("Person-Workflow", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_ser ?ser. \n"
                + "  ?ser ?ser_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#Workflow>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Workflow/\",encode_for_uri(?role) )) as ?pers_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Workflow-Person/\",encode_for_uri(?role_opposite) )) as ?ser_pers ). \n"
                + "}");

        insertRelationMatUpdate("Person-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Person", "Person"));
        insertRelationMatUpdate("OrganisationUnit-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#OrganisationUnit", "OrganisationUnit"));
        insertRelationMatUpdate("Project-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Project", "Project"));
        insertRelationMatUpdate("Publication-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Publication", "Publication"));
        insertRelationMatUpdate("Product-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Product", "Product"));
        insertRelationMatUpdate("Dataset-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Dataset", "Dataset"));
        insertRelationMatUpdate("Equipment-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Equipment", "Equipment"));
        insertRelationMatUpdate("Facility-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Facility", "Facility"));
        insertRelationMatUpdate("Software-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Software", "Software"));
        insertRelationMatUpdate("Service-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Service", "Service"));
        insertRelationMatUpdate("WebService-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#WebService", "WebService"));
        insertRelationMatUpdate("Workflow-Classification", insertEntityClassficationMaterial("http://eurocris.org/ontology/cerif#Workflow", "Workflow"));

//        insertRelationMatUpdate("OrganisationUnit-Workflow", "WITH @#$%FROM%$#@ \n"
//                + "INSERT { \n"
//                + "  ?org ?org_ser ?ser. \n"
//                + "  ?ser ?ser_org ?org. \n"
//                + "} WHERE { \n"
//                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
//                + "  ?ser a <http://eurocris.org/ontology/cerif#Workflow>.\n"
//                + "\n"
//                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
//                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
//                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
//                + "\n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Workflow/\",encode_for_uri(?role) )) as ?org_ser ). \n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Workflow-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
//                + "}");
//
//        insertRelationMatUpdate("Service-Workflow", "WITH @#$%FROM%$#@ \n"
//                + "INSERT { \n"
//                + "  ?ser1 ?ser1_ser2 ?ser2. \n"
//                + "  ?ser2 ?ser2_ser1 ?pers1. \n"
//                + "} WHERE { \n"
//                + "  ?ser1 a <http://eurocris.org/ontology/cerif#Service>.\n"
//                + "  ?ser2 a <http://eurocris.org/ontology/cerif#Workflow>.\n"
//                + "\n"
//                + "   ?ser1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
//                + "   ?ser2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
//                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
//                + "\n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Service-Workflow/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Workflow-Service/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
//                + "}");
    }

    private String insertEntityClassficationMaterial(String entityUri, String entity) {
        return "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?ent ?ent_class ?classif. \n"
                + "  ?classif ?class_ent ?ent. \n"
                + "} WHERE { \n"
                + "  ?ent a <" + entityUri + ">.\n"
                + "  ?classif a <http://eurocris.org/ontology/cerif#Classification>.\n"
                + "\n"
                + "   ?ent <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?pou a <http://eurocris.org/ontology/cerif#SimpleLinkEntity>.\n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#" + entity + "-Classification/\",encode_for_uri(?role) )) as ?ent_class ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Classification-" + entity + "/\",encode_for_uri(?role_opposite) )) as ?class_ent ). \n"
                + "}";
    }

    private void insertRelationsMatUpdatesNew() throws SQLException {
        insertRelationMatUpdate("searchable_text_organisation", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(GROUP_CONCAT(distinct ?name ; separator=\", \"),\" \", ?acronym) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_acronym>  ?acronym .}\n"
                + "?serv <http://eurocris.org/ontology/cerif#has_name>     ?name .\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_dataset", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");
        insertRelationMatUpdate("searchable_text_software", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?abstr,\" \", ?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_abstract>        ?servAbstract .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "BIND(if(bound(?servDescription),?servAbstract,\"-\") as ?abstr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_equipment", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_facility", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("searchable_text_webservice", "insert into @#$%FROM%$#@ {\n"
                + "?uri <http://searchable_text> ?text.\n"
                + "} where \n"
                + "{\n"
                + "select distinct (?serv as ?uri) concat(?servName,\" \", ?descr, \" \", GROUP_CONCAT(distinct ?servKeywords ; separator=\", \")) as ?text from @#$%FROM%$#@  where {\n"
                + "?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_description> ?servDescription .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_keywords>    ?servKeywords .}\n"
                + "OPTIONAL {?serv <http://eurocris.org/ontology/cerif#has_name>        ?servName .}\n"
                + "BIND(if(bound(?servDescription),?servDescription,\"-\") as ?descr).\n"
                + "}\n"
                + "}");

        insertRelationMatUpdate("has_source",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#has_source> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#is_source_of> ?s.\n"
                + "}");
        insertRelationMatUpdate("is_source_of",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#is_source_of> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#has_source> ?s.\n"
                + "}");
        insertRelationMatUpdate("has_destination",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#has_destination> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#is_destination_of> ?s.\n"
                + "}");
        insertRelationMatUpdate("is_destination_of",
                "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?s <http://eurocris.org/ontology/cerif#is_destination_of> ?o.  \n"
                + "} WHERE {\n"
                + "  ?o <http://eurocris.org/ontology/cerif#has_destination> ?s.\n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Publication",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?project_pub ?pub. \n"
                + "  ?pub ?pub_project ?org. \n"
                + "} WHERE { \n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>. \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>. \n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?op. \n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?op. \n"
                + "  ?op <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?orgunit_pub ). \n"
                + "  Bind( IRI( concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?pub_orgunit ). \n"
                + "}");
        insertRelationMatUpdate("Person-OrganisationUnit",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?person_orgunit ?org. \n"
                + "  ?org ?orgunit_person ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>. \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>. \n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?person_orgunit ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?orgunit_person ) \n"
                + "}");
        insertRelationMatUpdate("Person-Person", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_pers1 ?pers1. \n"
                + "  ?pers1 ?pers_pers2 ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?pers1 a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?pers1 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_pers1 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?pers_pers2). \n"
                + "}");
        insertRelationMatUpdate("Person-Project",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?person_project ?proj. \n"
                + "  ?proj ?project_person ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>. \n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>. \n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp. \n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?person_project ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?project_person ). \n"
                + "}");
        insertRelationMatUpdate("Person-Publication",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?person_publication ?pub. \n"
                + "  ?pub ?publication_person ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>. \n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>. \n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pp. \n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?person_publication ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?publication_person ). \n"
                + "}");
        insertRelationMatUpdate("Project-Publication",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?proj ?project_pub ?pub. \n"
                + "  ?pub ?pub_project ?proj. \n"
                + "} WHERE { \n"
                + "  ?pub a <http://eurocris.org/ontology/cerif#Publication>. \n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>. \n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp. \n"
                + "  ?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp. \n"
                + "  ?pp <http://eurocris.org/ontology/cerif#has_classification> ?classif. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite. \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?project_pub ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?pub_project ). \n"
                + "}");
        insertRelationMatUpdate("Project-OrganisationUnit",
                "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?proj ?project_org ?org. \n"
                + "  ?org ?org_project ?proj. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>. \n"
                + "  ?proj a <http://eurocris.org/ontology/cerif#Project>. \n"
                + "\n"
                + "  ?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pou. \n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.    \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role_opposite. \n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role. \n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?project_org ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?org_project ). \n"
                + "}");
        insertRelationMatUpdate("Person-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Project-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Project>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Publication-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Publication>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE1.\n"
                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Product-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Dataset-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Equipment-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("Facility-Location", "WITH @#$%FROM%$#@\n"
                + "INSERT {\n"
                + "  ?obj ?locProp ?GBB.\n"
                + "} WHERE {\n"
                + "  ?obj a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "  ?obj <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                //                + "  ?FLE1 <http://eurocris.org/ontology/cerif#has_destination> ?PA.\n"
                //                + "  ?PA <http://eurocris.org/ontology/cerif#is_source_of> ?FLE2.\n"
                + "  ?FLE2 <http://eurocris.org/ontology/cerif#has_destination> ?GBB.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_eastBoundaryLongitude> ?east.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_westBoundaryLongitude> ?west.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_northBoundaryLatitude> ?north.\n"
                + "  ?GBB <http://eurocris.org/ontology/cerif#has_southBoundaryLatitude> ?south.\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(\"located at\"))) as ?locProp ).\n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Product", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_product ?prod. \n"
                + "  ?prod ?product_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?prod a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?prod <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role_opposite.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?org_product ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?product_org ). \n"
                + "}");
        insertRelationMatUpdate("Person-Product", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_product ?prod. \n"
                + "  ?prod ?product_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?prod a <http://eurocris.org/ontology/cerif#Product>.\n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?prod <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role_opposite.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_product ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?product_pers ). \n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Facility", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_fac ?fac. \n"
                + "  ?fac ?fac_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?fac a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?fac <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?org_fac ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?fac_org ). \n"
                + "}");
        insertRelationMatUpdate("Person-Facility", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_fac ?fac. \n"
                + "  ?fac ?fac_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?fac a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "\n"
                + "  ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?fac <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_fac ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?fac_pers ). \n"
                + "}");
        insertRelationMatUpdate("OrganisationUnit-Equipment", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_eq ?eq. \n"
                + "  ?eq ?eq_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?eq a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "\n"
                + "  ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "  ?eq <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "  ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "  ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?org_eq ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?eq_org ). \n"
                + "}");
        insertRelationMatUpdate("Person-Equipment", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_eq ?eq. \n"
                + "  ?eq ?eq_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?eq a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?eq <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_eq ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?eq_pers ). \n"
                + "}");

        insertRelationMatUpdate("Person-Service", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_ser ?ser. \n"
                + "  ?ser ?ser_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-Service", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_ser ?ser. \n"
                + "  ?ser ?ser_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?org_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
                + "}");

        insertRelationMatUpdate("Service-Service", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?ser1 ?ser1_ser2 ?ser2. \n"
                + "  ?ser2 ?ser2_ser1 ?pers1. \n"
                + "} WHERE { \n"
                + "  ?ser1 a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "  ?ser2 a <http://eurocris.org/ontology/cerif#Service>.\n"
                + "\n"
                + "   ?ser1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");
        ///
        insertRelationMatUpdate("Person-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_ser ?ser. \n"
                + "  ?ser ?ser_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_ser ?ser. \n"
                + "  ?ser ?ser_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?org_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
                + "}");

        insertRelationMatUpdate("WebService-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?ser1 ?ser1_ser2 ?ser2. \n"
                + "  ?ser2 ?ser2_ser1 ?pers1. \n"
                + "} WHERE { \n"
                + "  ?ser1 a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "  ?ser2 a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?ser1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Person-Software", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_soft ?soft. \n"
                + "  ?soft ?soft_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?soft a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?soft <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_soft ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?soft_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-Software", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_soft ?soft. \n"
                + "  ?soft ?soft_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?soft a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?soft <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?org_soft ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?soft_org ). \n"
                + "}");

        insertRelationMatUpdate("Person-Dataset", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_data ?data. \n"
                + "  ?data ?data_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_data ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?data_pers ). \n"
                + "}");

        insertRelationMatUpdate("OrganisationUnit-Dataset", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?org ?org_ser ?data. \n"
                + "  ?data ?ser_org ?org. \n"
                + "} WHERE { \n"
                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "\n"
                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?org_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
                + "}");

        insertRelationMatUpdate("Software-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?soft ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?soft. \n"
                + "} WHERE { \n"
                + "  ?soft a <http://eurocris.org/ontology/cerif#Software>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Dataset-Dataset", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data1 ?ser1_ser2 ?data2. \n"
                + "  ?data2 ?ser2_ser1 ?data1. \n"
                + "} WHERE { \n"
                + "  ?data1 a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "  ?data2 a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "\n"
                + "   ?data1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?data2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Dataset-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?data. \n"
                + "} WHERE { \n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Dataset>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Equipment-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?data. \n"
                + "} WHERE { \n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Equipment>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");

        insertRelationMatUpdate("Facility-WebService", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?data ?ser1_ser2 ?serv. \n"
                + "  ?serv ?ser2_ser1 ?data. \n"
                + "} WHERE { \n"
                + "  ?data a <http://eurocris.org/ontology/cerif#Facility>.\n"
                + "  ?serv a <http://eurocris.org/ontology/cerif#WebService>.\n"
                + "\n"
                + "   ?data <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?serv <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
                + "}");
        ////
        insertRelationMatUpdate("Person-Workflow", "WITH @#$%FROM%$#@ \n"
                + "INSERT { \n"
                + "  ?pers ?pers_ser ?ser. \n"
                + "  ?ser ?ser_pers ?pers. \n"
                + "} WHERE { \n"
                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
                + "  ?ser a <http://eurocris.org/ontology/cerif#Workflow>.\n"
                + "\n"
                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
                + "\n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role) )) as ?pers_ser ). \n"
                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#/\",encode_for_uri(?role_opposite) )) as ?ser_pers ). \n"
                + "}");

//        insertRelationMatUpdate("OrganisationUnit-Workflow", "WITH @#$%FROM%$#@ \n"
//                + "INSERT { \n"
//                + "  ?org ?org_ser ?ser. \n"
//                + "  ?ser ?ser_org ?org. \n"
//                + "} WHERE { \n"
//                + "  ?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
//                + "  ?ser a <http://eurocris.org/ontology/cerif#Workflow>.\n"
//                + "\n"
//                + "   ?org <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
//                + "   ?ser <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
//                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
//                + "\n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#OrganisationUnit-Workflow/\",encode_for_uri(?role) )) as ?org_ser ). \n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Workflow-OrganisationUnit/\",encode_for_uri(?role_opposite) )) as ?ser_org ). \n"
//                + "}");
//
//        insertRelationMatUpdate("Service-Workflow", "WITH @#$%FROM%$#@ \n"
//                + "INSERT { \n"
//                + "  ?ser1 ?ser1_ser2 ?ser2. \n"
//                + "  ?ser2 ?ser2_ser1 ?pers1. \n"
//                + "} WHERE { \n"
//                + "  ?ser1 a <http://eurocris.org/ontology/cerif#Service>.\n"
//                + "  ?ser2 a <http://eurocris.org/ontology/cerif#Workflow>.\n"
//                + "\n"
//                + "   ?ser1 <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
//                + "   ?ser2 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
//                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
//                + "\n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Service-Workflow/\",encode_for_uri(?role) )) as ?ser1_ser2 ). \n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Workflow-Service/\",encode_for_uri(?role_opposite) )) as ?ser2_ser1 ). \n"
//                + "}");
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

    public boolean namedGraphExists(String graphName) throws SQLException {
        ResultSet result = statement.executeQuery("SELECT * FROM NAMEDGRAPH where NAME='" + graphName + "'");
        return result.next();
    }

    public static void main(String[] args) throws ClassNotFoundException, SQLException, UnsupportedEncodingException, IOException, ParseException, Exception {
        H2Manager h2 = new H2Manager();
//        h2.init();
//
        h2.deleteTable("entity");
        h2.createTableEntity();
//        h2.insertEntitiesBlazegraph();
        h2.insertEntitiesVirtuoso();
//        h2.deleteTable("RELATION");
//        h2.createTableRelation();

        h2.deleteTable("RELATIONS_MATERIAL");
        h2.createTableRelationsMatUpdates();
        h2.insertRelationsMatUpdates();
//        h2.insertEntity("Workflow",
//                "http://eurocris.org/ontology/cerif#Workflow",
//                "thesaurus/persons-firstAndLastNames.json",
//                "PREFIX cerif:<http://eurocris.org/ontology/cerif#>\n"
//                + "select distinct ?name ?wflowUri ?wflowDescription (?serv as ?uri) @#$%FROM%$#@\n"
//                + "where {\n"
//                + "?serv a cerif:Workflow. \n"
//                + "OPTIONAL {?serv cerif:has_URI         ?wflowUri .}\n"
//                + "OPTIONAL {?serv cerif:has_description ?wflowDescription .}\n"
//                + "#OPTIONAL {?serv cerif:has_keywords    ?wflowKeywords .}\n"
//                + "OPTIONAL {?serv cerif:has_name        ?name .}\n"
//                + "?serv <http://searchable_text> ?wflowLabel. \n"
//                + "?wflowLabel bif:contains \"@#$%TERM%$#@\". \n"
//                + "} ",
//                "",
//                "",
//                false,
//                "distinct ?@#$%VAR%$#@Name as ?name ?@#$%VAR%$#@Uri as ?workflow_uri ?@#$%VAR%$#@Descr as ?description (?@#$%VAR%$#@ as ?uri)",
//                "?@#$%VAR%$#@ a <http://eurocris.org/ontology/cerif#Workflow>.\n"
//                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_URI>         ?@#$%VAR%$#@Uri .}\n"
//                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_description> ?@#$%VAR%$#@Description .}\n"
//                + "#OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_keywords>    ?@#$%VAR%$#@Keywords .}\n"
//                + "OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#has_name>        ?@#$%VAR%$#@Name .}\n"
//                + "#OPTIONAL {?@#$%VAR%$#@ <http://eurocris.org/ontology/cerif#is_source_of> ?@#$%VAR%$#@mediumLE .\n"
//                + "#?@#$%VAR%$#@mediumLE  <http://eurocris.org/ontology/cerif#has_destination> ?@#$%VAR%$#@Medium.\n"
//                + "#?@#$%VAR%$#@Medium a <http://eurocris.org/ontology/cerif#Medium>. }\n"
//                + "BIND(if(bound(?@#$%VAR%$#@Description),?@#$%VAR%$#@Description,\"-\") as ?@#$%VAR%$#@Descr).\n"
//                + "#BIND(if(bound(?@#$%VAR%$#@Keywords),?@#$%VAR%$#@Keywords,\"-\") as ?@#$%VAR%$#@Keyw).\n"
//                + "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label. \n",
//                "?@#$%VAR%$#@ <http://searchable_text> ?@#$%VAR%$#@Label. \n"
//                + "?@#$%VAR%$#@Label bif:contains \"@#$%TERM%$#@\".",
//                "",
//                "",
//                "workfl"
//        );
//
//        h2.insertRelationMatUpdate("Person-Person", "WITH @#$%FROM%$#@ \n"
//                + "INSERT { \n"
//                + "  ?pers ?pers_pers1 ?pers1. \n"
//                + "  ?pers1 ?pers_pers2 ?pers. \n"
//                + "} WHERE { \n"
//                + "  ?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
//                + "  ?pers1 a <http://eurocris.org/ontology/cerif#Person>.\n"
//                + "\n"
//                + "   ?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
//                + "   ?pers1 <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n"
//                + "   ?pou <http://eurocris.org/ontology/cerif#has_classification> ?classif.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpression> ?role.\n"
//                + "   ?classif <http://eurocris.org/ontology/cerif#has_roleExpressionOpposite> ?role_opposite.\n"
//                + "\n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Person/\",encode_for_uri(?role) )) as ?pers_pers1 ). \n"
//                + "  Bind( IRI(concat(\"http://eurocris.org/ontology/cerif#Person-Person/\",encode_for_uri(?role_opposite) )) as ?pers_pers2). \n"
//                + "}");

//        h2.createTableUserFavorites();
//        h2.insertNamedGraph("http://vre/workflows", "Workflows", "", 2);
        h2.terminate();
        /////////////////        
        String authorizationToken = "eyJ0eXAiOiJKV1QiLCJhbGciOiJIUzI1NiJ9.eyJpc3MiOiJOb2RlU2VydmljZSIsInVzZXJJZCI6Im1hdGgifQ.JK2TzOSTAX9M-90mBOBgN_AGoashunSXnhaLTPwJZnA";
        String endpoint = "http://139.91.183.97:8181/EVREMetadataServices-2.0-SNAPSHOT";
        String graphUri = "http://graph/1536156823420";
//        graphUri = "http://vre/workflows";
        graphUri = "http://fris-data/dev";
        /////
////        List<String> uris = DBService.retrieveAllNamedgraphUris();
////        for (String graphURI : uris) {
//        Set<String> matRelationEntities = new HashSet<>();
//        matRelationEntities.add("Person");
//        matRelationEntities.add("Workflow");
//        matRelationEntities.add("Organisation");
//        matRelationEntities.add("Equipment");
//        matRelationEntities.add("Facility");
//        matRelationEntities.add("WebService");
//        matRelationEntities.add("Product");
//        matRelationEntities.add("Location");
//        matRelationEntities.add("Publication");
//        matRelationEntities.add("Project");
//        matRelationEntities.add("Publication");
//        matRelationEntities.add("Dataset");
//        matRelationEntities.add("Software");
//        
//        DBService.executeRelationsMatQueries(endpoint, "", authorizationToken, graphUri);
//        enrichMatRelationsTable(endpoint, authorizationToken, graphUri, matRelationEntities);
//        }

//        Map<String, String> namedgraphs = DBService.retrieveAllNamedgraphUrisLabels();
//        for (String uri : namedgraphs.keySet()) {
//            String label = namedgraphs.get(uri);
//            DBService.executeBelongsInQueries(endpoint, authorizationToken, uri, label);
//        }
    }

//    this table contains information about the entities and the materialized relations which connect them 
//    for each namedgraph. It helps in the performance when we select a target entity from the GUI and we want 
//    its related entities along with the relations. 
    public static void enrichMatRelationsTable(String endpoint, String authorizationToken, String graphUri, Set<String> matRelEntityNames) throws SQLException, UnsupportedEncodingException, ClassNotFoundException, IOException {
        if (matRelEntityNames.isEmpty()) {
            return;
        }
        JSONArray matRelEntities = DBService.retrieveAllEntities(false);
        Connection conn = DBService.initConnection();
        H2Manager h2 = new H2Manager(conn.createStatement(), conn);
//        RestClient client = new RestClient(endpoint, namespace, authorizationToken);
        VirtuosoRestClient virtClient = new VirtuosoRestClient(endpoint, authorizationToken);
        ////////
        for (int i = 0; i < matRelEntities.size(); i++) {
            JSONObject targetEntity = (JSONObject) matRelEntities.get(i);
            String targetEntityURI = (String) targetEntity.get("uri");
            int targetEntityID = (int) targetEntity.get("id");
            String targetEntityName = (String) targetEntity.get("name");
            if (!matRelEntityNames.contains(targetEntityName)) {
                continue;
            }
            int cnt = 0;
            for (int j = 0; j < matRelEntities.size(); j++) {
                StringBuilder sparqlQuery = new StringBuilder();
                JSONObject relatedEntity = (JSONObject) matRelEntities.get(j);
                String relatedEntityURI = (String) relatedEntity.get("uri");
                int relatedEntityID = (int) relatedEntity.get("id");
                String relatedEntityName = (String) relatedEntity.get("name");
//                if (j == i) {
//                    continue;
//                }
                if (!matRelEntityNames.contains(relatedEntityName)) {
                    continue;
                }
                sparqlQuery.append("select distinct ?relation from <" + graphUri + "> where {\n").
                        append("?target_inst a <" + targetEntityURI + ">.\n").
                        append("?target_inst ?relation [a <" + relatedEntityURI + ">].\n").
                        append("}");
//                String response = client.executeSparqlQuery(sparqlQuery.toString(), "text/csv", 0).readEntity(String.class);
                String response = virtClient.executeSparqlQuery(sparqlQuery.toString(), "text/csv", 0).readEntity(String.class);
                String[] data = response.split("\\n");
                for (int k = 1; k < data.length; k++) {
                    String relationUri = data[k].replaceAll("\\\"", "");
                    String relationName = URLDecoder.decode(relationUri, "UTF-8").substring(relationUri.lastIndexOf("/") + 1);
                    if (!h2.relationExists(relationUri.trim(), relationName.trim(), targetEntityID, relatedEntityID, graphUri)) {
                        h2.insertRelation(relationUri.trim(), relationName.trim(), targetEntityID, relatedEntityID, graphUri);
                    }
                }
            }
        }
        h2.terminate();
    }

    public Connection getConnection() {
        return this.connection;
    }

}
