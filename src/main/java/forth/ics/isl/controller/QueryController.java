package forth.ics.isl.controller;

import org.springframework.context.annotation.ScopedProxyMode;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.StringTokenizer;

import javax.annotation.PostConstruct;
import javax.ws.rs.core.Response;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.JsonNodeFactory;
import com.fasterxml.jackson.databind.node.ObjectNode;

import forth.ics.isl.data.model.EndPointDataPage;
import forth.ics.isl.data.model.EndPointForm;
import forth.ics.isl.data.model.InputAdvancedRequest;
import forth.ics.isl.data.model.InputGeoRequest;
import forth.ics.isl.data.model.InputTagRequest;
import forth.ics.isl.data.model.NgTag;
import forth.ics.isl.triplestore.RestClient;

/**
 * The back-end controller for the query service
 * 
 * @author Vangelis Kritsotakis
 */

@Scope(scopeName="session", proxyMode=ScopedProxyMode.TARGET_CLASS)
@Controller
public class QueryController {
	
	@Value("${service.url}")
	private String serviceUrl;
	@Value("${triplestore.namespace}")
	private String namespace;
	private JsonNode currQueryResult;
	private RestClient restClient;
	@Value("${maps.namegraph}")
	private String mapNamegraphs;
	
	@PostConstruct
    public void init() throws IOException {
		restClient = new RestClient(serviceUrl, namespace);
        currQueryResult = new ObjectNode(JsonNodeFactory.instance);
    }
	
	@RequestMapping(value="/",method = RequestMethod.GET)
    public String homepage() {
        return "index";
    }
	
	@JsonProperty("geonames")
	private List<NgTag> ngTagList; 
	
	@RequestMapping(value = "/executequery_json", method = RequestMethod.POST, produces={"application/json"})
	public @ResponseBody EndPointForm callQuery(@RequestHeader(value="Authorization") String authorizationToken, @RequestBody InputTagRequest inputTagRequest) {
		
		System.out.println("running executequery_json...");
		System.out.println("inputTagRequest:" + inputTagRequest.getTerms());
		System.out.println("selectedEntity:" + inputTagRequest.getSelectedEntity());
		System.out.println("selectedProjection:" + inputTagRequest.getSelectedProjection());
		System.out.println("selectedNamegraphs:" + inputTagRequest.getSelectedNamegraphs());
		System.out.println("authorizationToken: " + authorizationToken);
		
		
		System.out.println("tags:");
		for(NgTag tag : inputTagRequest.getTerms()) {
			System.out.println(tag.getName());
		}
		
		int itemsPerPage = new Integer(inputTagRequest.getItemsPerPage());
		
		// String to hold user's input tags separated by space
		String tagString = "";
		int i = 0;
		String optionalSpaceStr = " ";
		for(NgTag tag : inputTagRequest.getTerms()) {
			if(i == 0)
				optionalSpaceStr = "";
			else
				optionalSpaceStr = " ";
			
			tagString = tagString + optionalSpaceStr + tag.getName();
			i++;
		}
		
		// Strings to hold the query to execute
		String prefixStr= "";
		String selectStr = "";
		String queryStr = "";
		String specialCaseStr = "";
		
		boolean supportedCase = true;
		
		// Entity should not be null
		if(inputTagRequest.getSelectedEntity() != null) {
			
			
			// Prefix
			prefixStr= "PREFIX bds: <http://www.bigdata.com/rdf/search#>\n";
			
			// Case: Projection Persons
			if(inputTagRequest.getSelectedProjection().equals("Persons")) {
				
				// Select String
				selectStr = "select distinct (concat(str(?pers), '#@#', str(?persName)) as ?person_name) ?Service ";
				
				for(String namegraph : inputTagRequest.getSelectedNamegraphs()) {
					queryStr = queryStr + "from <" + namegraph + "> \n";
				}
				
				queryStr = queryStr + "where {\n";
				
				queryStr = queryStr 
						 + "?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
						 + "?pers rdfs:label ?persName.\n"
						 + "?pers cerif:is_source_of ?FLES.\n"
						 + "?FLES cerif:has_destination ?Ser.\n"
						 + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
						 + "?Ser cerif:has_acronym ?Service.";
				
				if(inputTagRequest.getSelectedEntity().equals("Projects")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?pers <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
							 + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n";
				}
				
				else if(inputTagRequest.getSelectedEntity().equals("Publications")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
							 + "?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n";
				}
				
				else if (inputTagRequest.getSelectedEntity().equals("Organization Units")) {
					queryStr = queryStr 
							 + "?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
							 + "?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou.\n";
					
				}
				else if (inputTagRequest.getSelectedEntity().equals("Resources")) {
					// This case doesn't exist
					supportedCase = false;
					
				}
				
			}
			
			// Case: Projection Projects
			else if(inputTagRequest.getSelectedProjection().equals("Projects")) {
				
				// Select String
				selectStr = "select distinct (concat(str(?proj), '#@#', str(?projectTitle)) as ?project_title) ?projectURI ?projectAcronym ?Service ";
				
				for(String namegraph : inputTagRequest.getSelectedNamegraphs()) {
					queryStr = queryStr + "from <" + namegraph + "> \n";
				}
				queryStr = queryStr + "where {\n";
				
				queryStr = queryStr + "?proj a <http://eurocris.org/ontology/cerif#Project>.\n"
						 + "?proj <http://eurocris.org/ontology/cerif#has_title> ?projectTitle.\n"
						 + "OPTIONAL {?proj <http://eurocris.org/ontology/cerif#has_URI> ?projURI.}\n"
						 + "BIND(if(bound(?projURI),?projURI,?proj) as ?projectURI).\n"
						 + "?proj <http://eurocris.org/ontology/cerif#has_acronym> ?projectAcronym."
						 + "?proj cerif:is_source_of ?FLES.\n"
						 + "?FLES cerif:has_destination ?Ser.\n"
						 + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
						 + "?Ser cerif:has_acronym ?Service.\n";
						
				if(inputTagRequest.getSelectedEntity().equals("Persons")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?pers <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
							 + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n";
				}
				
				else if(inputTagRequest.getSelectedEntity().equals("Publications")) {
					
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
							 + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n";
				}
				
				else if (inputTagRequest.getSelectedEntity().equals("Organization Units")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
							 + "?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou. \n";
					
				}
				
				else if (inputTagRequest.getSelectedEntity().equals("Resources")) {
					// This case doesn't exist
					supportedCase = false;
					
				}
				
			}
			
			// Case: Projection Publications
			else if(inputTagRequest.getSelectedProjection().equals("Publications")) {
				
				// Select String
				selectStr = "select distinct (concat(str(?pub), '#@#', str(?pubTitle)) as ?publication_title) ?pubDate ?Service ";
				
				//queryStr = "PREFIX bds: <http://www.bigdata.com/rdf/search#>\n"
				//		 + "select ?pub ?pubTitle ?pubDate ?pubAbstract\n";
				
				for(String namegraph : inputTagRequest.getSelectedNamegraphs()) {
					queryStr = queryStr + "from <" + namegraph + "> \n";
				}
				
				queryStr = queryStr + "where {\n";
				
				queryStr = queryStr + "?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
						 + "?pub <http://eurocris.org/ontology/cerif#has_title> ?pubTitle.\n"
						 + "?pub <http://eurocris.org/ontology/cerif#has_publicationDate> ?pubDate.\n"
						 + "?pub cerif:is_source_of ?FLES.\n"
						 + "?FLES cerif:has_destination ?Ser.\n"
						 + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
						 + "?Ser cerif:has_acronym ?Service.";
														
				if(inputTagRequest.getSelectedEntity().equals("Persons")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
							 + "?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n";
					
				}
				
				else if(inputTagRequest.getSelectedEntity().equals("Projects")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?pub <http://eurocris.org/ontology/cerif#is_destination_of> ?pp.\n"
							 + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pp.\n";
				}
				
				else if (inputTagRequest.getSelectedEntity().equals("Organization Units")) {
					// This case doesn't exist
					supportedCase = false;
				}
				
				else if (inputTagRequest.getSelectedEntity().equals("Resources")) {
					// This case doesn't exist
					supportedCase = false;
					
				}
				
			}
			
			// Case: Organization Units
			else if (inputTagRequest.getSelectedProjection().equals("Organization Units")) {
				
				// Select String
				selectStr = "select distinct (concat(str(?org), '#@#', str(?orgName)) as ?organization_name) ?orgAcronym ?Service ";
				
				for(String namegraph : inputTagRequest.getSelectedNamegraphs()) {
					queryStr = queryStr + "from <" + namegraph + "> \n";
				}
								
				queryStr = queryStr + "where {\n";
				
				if(inputTagRequest.getSelectedEntity().equals("Persons")) {
					queryStr = queryStr + "{\n";
				}
				
				queryStr = queryStr +  "?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
						 + "?org <http://eurocris.org/ontology/cerif#has_name> ?orgName.\n"
						 + "?org <http://eurocris.org/ontology/cerif#has_acronym> ?orgAcronym.\n"
						 + "?org cerif:is_source_of ?FLES.\n"
						 + "?FLES cerif:has_destination ?Ser.\n"
						 + "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
						 + "?Ser cerif:has_acronym ?Service.";
															
				if(inputTagRequest.getSelectedEntity().equals("Persons")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?pers <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
							 + "?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou.\n";
				}
				
				else if(inputTagRequest.getSelectedEntity().equals("Projects")) {
					// Handling Projection and Entity Connection
					queryStr = queryStr 
							 + "?proj <http://eurocris.org/ontology/cerif#is_source_of> ?pou.\n"
							 + "?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou.\n";
				}
				
				else if(inputTagRequest.getSelectedEntity().equals("Publications")) {
					// This case doesn't exist
					supportedCase = false;
				}
				
				else if (inputTagRequest.getSelectedEntity().equals("Resources")) {
					// This case doesn't exist
					supportedCase = false;
					
				}
				
			}
			
			// Handling Entities
			if(inputTagRequest.getSelectedEntity().equals("Persons")) {
				
				// Select String handling entity
				//selectStr = selectStr + "?pers ?persName\n";
				if(!inputTagRequest.getSelectedProjection().equals("Persons"))
					selectStr = selectStr + "(concat(str(?pers), '#@#', str(?persName)) as ?person_name)\n";
				
				// Handling Entity
				queryStr = queryStr 
						 + "?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
						 + "?pers rdfs:label ?persName.\n"
						 + "?persName bds:search '" + tagString + "'.\n"
						 + "?persName bds:matchAllTerms 'true'.\n"
						 + "?persName bds:relevance ?score.";
			}
			
			else if(inputTagRequest.getSelectedEntity().equals("Projects")) {
				
				// Select String handling entity
				//selectStr = selectStr + "?proj ?projectTitle ?projectURI ?projectAcronym\n";
				if(!inputTagRequest.getSelectedProjection().equals("Projects"))
					selectStr = selectStr + "(concat(str(?proj), '#@#', str(?projectTitle)) as ?project_title) ?projectAcronym\n";
				
				// Handling Entity
				queryStr = queryStr 
						 + "?proj a <http://eurocris.org/ontology/cerif#Project>.\n"
						 + "?proj <http://eurocris.org/ontology/cerif#has_title> ?projectTitle.\n"
						 + "OPTIONAL {?proj <http://eurocris.org/ontology/cerif#has_URI> ?projURI.}\n"
						 + "BIND(if(bound(?projURI),?projURI,?proj) as ?projectURI).\n"
						 + "?proj <http://eurocris.org/ontology/cerif#has_acronym> ?projectAcronym.\n"
						 + "?proj rdfs:label ?projName.\n"							 
						 + "?projName bds:search '" + tagString + "'.\n"
						 + "?projName bds:matchAllTerms 'true'.\n"
						 + "?projName bds:relevance ?score \n";
			}
			
			else if(inputTagRequest.getSelectedEntity().equals("Publications")) {
				
				// Select String handling entity
				//selectStr = selectStr + "?pub ?pubTitle\n";
				if(!inputTagRequest.getSelectedProjection().equals("Publications"))
					selectStr = selectStr + "(concat(str(?pub), '#@#', str(?pubTitle)) as ?publication_title)\n";
				
				// Handling Entity
				queryStr = queryStr 
						 + "?pub a <http://eurocris.org/ontology/cerif#Publication>.\n"
						 + "?pub <http://eurocris.org/ontology/cerif#has_title> ?pubTitle.\n"
						 + "?pub <http://eurocris.org/ontology/cerif#has_publicationDate> ?pubDate.\n"
						 + "?pubTitle bds:search '" + tagString + "'.\n"
						 + "?pubTitle bds:matchAllTerms 'true'.\n"
						 + "?pubTitle bds:relevance ?score.\n";
			}
			
			else if(inputTagRequest.getSelectedEntity().equals("Organization Units")) {
				
				// Select String handling entity
				//selectStr = selectStr + "?org ?orgName ?orgAcronym\n";
				if(!inputTagRequest.getSelectedProjection().equals("Organization Units"))
					selectStr = selectStr + "(concat(str(?org), '#@#', str(?orgName)) as ?org_name) ?orgAcronym\n";
				
				// Handling Entity
				queryStr = queryStr 
						 + "?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
						 + "?org rdfs:label ?orgName.\n"
						 + "?org <http://eurocris.org/ontology/cerif#has_acronym> ?orgAcronym.\n"
						 + "?orgName bds:search '" + tagString + "'.\n"
						 + "?orgName bds:matchAllTerms 'true'.\n"
						 + "?orgName bds:relevance ?score.\n";
			}
			
			if(inputTagRequest.getSelectedProjection().equals("Organization Units") && inputTagRequest.getSelectedEntity().equals("Persons")) {
				queryStr = queryStr + "}\n"
						+ "UNION\n"
						+ "{\n"
							+ "?org a <http://eurocris.org/ontology/cerif#OrganisationUnit>.\n"
							+ "?org <http://eurocris.org/ontology/cerif#has_name> ?orgName.\n"
							+ "?org <http://eurocris.org/ontology/cerif#has_acronym> ?orgAcronym.\n"
							+ "?org cerif:is_source_of ?FLES.\n"
							+ "?FLES cerif:has_destination ?Ser.\n"
							+ "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
							+ "?Ser cerif:has_acronym ?Service.\n"
							+ "?org <http://eurocris.org/ontology/cerif#is_destination_of> ?pou.\n"
							+ "?pers a <http://eurocris.org/ontology/cerif#Person>.\n"
							+ "?pers rdfs:label ?persName.\n"
							+ "?persName bds:search '" + tagString + "'.\n"
							+ "?persName bds:matchAllTerms 'true'.\n"
							+ "?persName bds:relevance ?score.\n"
							+ "?linkentity <http://eurocris.org/ontology/cerif#has_source> ?pers.\n"
							+ "?org <http://eurocris.org/ontology/cerif#is_destination_of> ?linkentity."
						+ "}";
			}
			queryStr = queryStr + "}";
		}
		
		// Case Resources (Epos Data)
		
		// Prefix
		prefixStr= "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n";
		
		// Case: Projection Resources
		if(inputTagRequest.getSelectedProjection().equals("Resources")) {
			// Entity Resources
			if(inputTagRequest.getSelectedEntity().equals("Resources")) {
				// Select String
				//queryStr = "SELECT DISTINCT ?object ?name  ?type ?nameX\n";
				queryStr = "SELECT DISTINCT (concat(str(?object), '#@#', str(?name)) as ?resource)  ?type ?Responsible ?Service\n";
				
				for(String namegraph : inputTagRequest.getSelectedNamegraphs()) {
					queryStr = queryStr + "FROM <" + namegraph + "> \n";
				}
				
				queryStr = queryStr
						+ "WHERE {\n?object a ?typeentity.\n"
							+ "?typeentity rdfs:label ?type.\n"
							+ "?object cerif:has_name ?name;\n"
							+ "rdfs:label ?label.\n"
							+ "?object cerif:is_source_of ?FLES.\n"
							+ "?FLES cerif:has_destination ?Ser.\n"
							+ "?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>.\n"
							+ "?Ser cerif:has_acronym ?Service.\n"
							+ "?object cerif:is_source_of ?FLE1.\n"
							+ "?FLE1 cerif:has_destination ?PA.\n"
							+ "?PA cerif:is_source_of ?FLE2.\n"
							+ "?FLE2 cerif:has_destination ?GBB.\n"
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
							+ "bind(coalesce(?nameOU, ?nameOUorP) as ?Responsible). {\n"
								+ "?label bds:search '" + tagString + "'.\n"
								+ "?label bds:matchAllTerms 'true'.\n"
								+ "?label bds:relevance ?score.\n"
							+ "}\n"
							+ "UNION {\n"
								+ "?object <http://eurocris.org/ontology/cerif#has_keywords> ?keyword.\n"
								+ "?keyword bds:search '" + tagString + "'.\n"
								+ "?keyword bds:matchAllTerms 'true'.\n"
								+ "?keyword bds:relevance ?score.\n"
							+ "}\n"
						+ "}\n";
			
			}
			
			// Entity Persons
			else if (inputTagRequest.getSelectedEntity().equals("Persons")) {
				// This case doesn't exist
				supportedCase = false;
			}
			
			// Entity Projects
			else if (inputTagRequest.getSelectedEntity().equals("Projects")) {
				// This case doesn't exist
				supportedCase = false;
			}
			
			// Entity Publications
			else if (inputTagRequest.getSelectedEntity().equals("Publications")) {
				// This case doesn't exist
				supportedCase = false;
			}
			
			// Entity Organization Units
			else if (inputTagRequest.getSelectedEntity().equals("Organization Units")) {
				// This case doesn't exist
				supportedCase = false;
			}
			
		}
						
		queryStr = prefixStr + selectStr + queryStr + "ORDER BY desc(?score)";
		
		EndPointForm endPointForm = new EndPointForm();
		endPointForm.setSupportedCase(supportedCase);
		
		endPointForm.setAuthorizationToken(authorizationToken);
		
		// Run only supported queries
		if(supportedCase == true) {
			
			System.out.println("queryStr: " + queryStr);
			
			endPointForm.setItemsPerPage(itemsPerPage);
			endPointForm.setQuery(queryStr);
			
			// Retrieving items based on query and holding them in EndPointForm pojo
			endPointForm = retrieveBasedOnQuery(endPointForm);
	    	
	    	// Checking saved status
	    	if (endPointForm.getStatusRequestCode() == 200) {
	    	
		    	// Holding globaly the whole results, which can be a lot.
		    	currQueryResult = endPointForm.getResult();
		    	
		    	// Holding the first page results in a separate JsonNode
				JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, endPointForm.getItemsPerPage());
				
				// Re-setting results (overwriting the old ones) for the response, 
				// such that it holds only those appearing at the first page
				endPointForm.setResult(firstPageQueryResult);
				
	    	}
		}
		
		else {
			
			endPointForm.setItemsPerPage(itemsPerPage);
			endPointForm.setQuery("");
			
			try {
				Thread.sleep(1000);
			} catch (InterruptedException e) {
				// TODO Auto-generated catch block
				e.printStackTrace();
			}
		}
    	
		return endPointForm;
		
	}

	@RequestMapping(value = "/executegeoquery_json", method = RequestMethod.POST, produces={"application/json"})
	public @ResponseBody EndPointForm callGeoQuery(@RequestHeader(value="Authorization") String authorizationToken, @RequestBody InputGeoRequest inputGeoRequest) {
		
		System.out.println("running executegeoquery_json...");
		System.out.println("inputTagRequest North:" + inputGeoRequest.getNorth());
		System.out.println("inputTagRequest South:" + inputGeoRequest.getSouth());
		System.out.println("inputTagRequest West:" + inputGeoRequest.getWest());
		System.out.println("inputTagRequest East:" + inputGeoRequest.getEast());
		System.out.println("authorizationToken: " + authorizationToken);
		
		int itemsPerPage = new Integer(inputGeoRequest.getItemsPerPage());
		
		ArrayList<String> mapNamegraphsArray = new ArrayList<String>();
		StringTokenizer tokenizer = new StringTokenizer(mapNamegraphs, ",");
		while (tokenizer.hasMoreElements()) {
			mapNamegraphsArray.add(tokenizer.nextElement().toString());
		}
		
		// String to hold the query to execute
		String queryStr = "";
		
		queryStr = "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n"
				 + "SELECT DISTINCT (concat(str(?object), '#@#', str(?name)) as ?resource)  ?type ?Responsible ?Service "
				 + "?east ?west ?north ?south\n";
			
		for(String namegraph : mapNamegraphsArray) {
			queryStr = queryStr + "FROM <http://" + namegraph + "> \n";
		}
		queryStr = queryStr + "WHERE {\n"
					 + "?object a ?typeentity.\n"
					 + "?typeentity rdfs:label ?type.\n"
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
					 + "FILTER(xsd:float(?east) <= " + inputGeoRequest.getEast() 
				     + " && xsd:float(?west) >= " + inputGeoRequest.getWest()
				     + " && xsd:float(?north) <= " + inputGeoRequest.getNorth() 
				     + " && xsd:float(?south) >= " + inputGeoRequest.getSouth() + ")\n"
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
				+ "}";

		
		System.out.println("queryStr: " + queryStr);
		
		EndPointForm endPointForm = new EndPointForm();
		endPointForm.setItemsPerPage(itemsPerPage);
		endPointForm.setQuery(queryStr);
		endPointForm.setAuthorizationToken(authorizationToken);
		
		
		// Retrieving items based on query an holding them in EndPointForm pojo
		endPointForm = retrieveBasedOnQuery(endPointForm);
    	
    	// Checking saved status
    	if (endPointForm.getStatusRequestCode() == 200) {
    	
	    	// Holding globaly the whole results, which can be a lot.
	    	currQueryResult = endPointForm.getResult();
	    	//System.out.println("currQueryResult: " + currQueryResult);
	    	
	    	// Holding the first page results in a separate JsonNode
			JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, endPointForm.getItemsPerPage());
			
			// Re-setting results (overwriting the old ones) for the response, 
			// such that it holds only those appearing at the first page
			endPointForm.setResult(firstPageQueryResult);
			
    	}
    	
		return endPointForm;
		
	}
	
	// Advanced Queries
	
	@RequestMapping(value = "/executeadvancedquery_json", method = RequestMethod.POST, produces={"application/json"})
	public @ResponseBody EndPointForm callAdvancedQuery(@RequestHeader(value="Authorization") String authorizationToken, @RequestBody InputAdvancedRequest inputAdvancedRequest) {
		
		System.out.println("running executeadvancedquery_json...");
		
		int itemsPerPage = new Integer(inputAdvancedRequest.getItemsPerPage());
		String queryToExecute = inputAdvancedRequest.getQueryToExecute();
		
		System.out.println("queryStr: " + queryToExecute);
		
		EndPointForm endPointForm = new EndPointForm();
		endPointForm.setItemsPerPage(itemsPerPage);
		endPointForm.setQuery(queryToExecute);
		endPointForm.setAuthorizationToken(authorizationToken);
		
		
		// Retrieving items based on query an holding them in EndPointForm pojo
		endPointForm = retrieveBasedOnQuery(endPointForm);
    	
    	// Checking saved status
    	if (endPointForm.getStatusRequestCode() == 200) {
    	
	    	// Holding globaly the whole results, which can be a lot.
	    	currQueryResult = endPointForm.getResult();
	    	//System.out.println("currQueryResult: " + currQueryResult);
	    	
	    	// Holding the first page results in a separate JsonNode
			JsonNode firstPageQueryResult = getDataOfPageForCurrentEndPointForm(1, endPointForm.getItemsPerPage());
			
			// Re-setting results (overwriting the old ones) for the response, 
			// such that it holds only those appearing at the first page
			endPointForm.setResult(firstPageQueryResult);
			
    	}
    	
		return endPointForm;
		
	}

	/**
     * Request get for retrieving a number of items that correspond to the passed page
     * and returning them in the form of EndPointDataPage object.
     *
     * @param requestParams 	A map that holds all the request parameters
     * @return 					An EndPointDataPage object that holds the items of the passed page.
     */
    @RequestMapping(value = "/paginator_json", method = RequestMethod.GET, produces={"application/json"})
	public @ResponseBody EndPointDataPage loadDataForPage(@RequestParam Map<String,String> requestParams) {//, Model model) {
    	
    	int page = new Integer(requestParams.get("page")).intValue();
    	int itemsPerPage = new Integer(requestParams.get("itemsPerPage")).intValue();

    	// The EndPointForm for the page
    	EndPointDataPage endPointDataPage = new EndPointDataPage();
    	endPointDataPage.setPage(page);
    	endPointDataPage.setTotalItems(currQueryResult.get("results").get("bindings").size());
    	endPointDataPage.setResult(getDataOfPageForCurrentEndPointForm(page, itemsPerPage));
    	    	
		return endPointDataPage;
	}
	
	/**
     * Method used many times that retrieves data based on an EndPointForm POJO (from which 
     * it mainly uses the query variable)
     *
     * @param endPointForm 	An EndPointForm object some of the variables of which are filled
     * 						in and the rest will be filled after the data retrieval is completed.
     * @return 				A processed EndPointForm object, all the fields of which are 
     * 						filled in. However the field "Result" will be reset soon.
     */
    private EndPointForm retrieveBasedOnQuery(EndPointForm endPointForm) {
    	
    	System.out.println("endPointForm: " + endPointForm);
    	
    	try {
    		Response serviceResponce = restClient.executeSparqlQuery(endPointForm.getQuery(), namespace, "application/json", endPointForm.getAuthorizationToken());
    		System.out.println("serviceResponce.getStatus(): "  + serviceResponce.getStatusInfo());
    		
    		// Setting Response status to POJO
    		endPointForm.setStatusRequestCode(serviceResponce.getStatus());
			endPointForm.setStatusRequestInfo(serviceResponce.getStatusInfo().toString());
    		
			// In case of OK status handle the response
    		if (serviceResponce.getStatus() == 200) {
    			
	    		// Serializing in pojo
	    		ObjectMapper mapper = new ObjectMapper();
	    		// Holding JSON in jsonNode globally
	    		JsonNode queryResult = mapper.readValue(serviceResponce.readEntity(String.class), JsonNode.class);
	    		//System.out.println("queryResult: " + queryResult);
				// Setting results for the response (for now we set them all and 
	    		// later we will replace them with those at the first page)
				endPointForm.setResult(queryResult);
				// Setting total items for the response
				endPointForm.setTotalItems(queryResult.get("results").get("bindings").size());
				
    		}
    		
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}
    	
		return endPointForm;
    }
    
    /**
     * Constructs an ObjectNode for one page only, based on the passed page and the whole data
     *
     * @param page	 		The page number
     * @param itemsPerPage 	The number of items per page
     * @return 				the constructed ObjectNode
     */
    private ObjectNode getDataOfPageForCurrentEndPointForm(int page, int itemsPerPage) {
    	    	    	    	
    	//{"head":{"vars":["s","p","o"]},"results":{"bindings":[{"s":{... "p":{...
    	JsonNodeFactory factory = JsonNodeFactory.instance;
    	
    	// vars
		ObjectNode varsObjectNode = new ObjectNode(factory);
		varsObjectNode.set("vars", currQueryResult.get("head").get("vars"));
		
		// bindings
		ArrayNode bindingsArrayNode = new ArrayNode(factory);
		
    	for (int i=0; i<itemsPerPage; i++) {
	    	if(currQueryResult.get("results").get("bindings").hasNonNull(i+(page-1)*itemsPerPage))
	    		bindingsArrayNode.add(currQueryResult.get("results").get("bindings").get(i+(page-1)*itemsPerPage));
    	}
    	  	
    	// Final ResultObject
    	ObjectNode resultObjectNode = new ObjectNode(factory);
    	resultObjectNode.set("head", varsObjectNode);
    	resultObjectNode.set("results", bindingsArrayNode);
    	
    	return resultObjectNode;
    	
    }
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    
    @RequestMapping(value = "/checkAuthorization", method = RequestMethod.GET, produces={"application/json"})
	public @ResponseBody EndPointForm loadDataForPage(@RequestHeader(value="Authorization", required=false) String authorizationToken, @RequestParam Map<String,String> requestParams) {
		
		String queryStr = "select * where {?s ?p ?o} limit 0";
		//System.out.println("authorizationToken: " + authorizationToken);
		EndPointForm endPointForm = new EndPointForm();
		endPointForm.setQuery(queryStr);
		endPointForm.setAuthorizationToken(authorizationToken);
		
		try {
    		Response serviceResponse = restClient.executeSparqlQuery(queryStr, namespace, "application/json", endPointForm.getAuthorizationToken());
    		System.out.println("serviceResponce.getStatus(): "  + serviceResponse.getStatusInfo());
    		
    		// Setting Response status to POJO
    		endPointForm.setStatusRequestCode(serviceResponse.getStatus());
			endPointForm.setStatusRequestInfo(serviceResponse.getStatusInfo().toString());
    		
			// In case of OK status handle the response
    		if (serviceResponse.getStatus() == 200) {
    			
	    		// Serializing in pojo
	    		ObjectMapper mapper = new ObjectMapper();
	    		// Holding JSON in jsonNode globally
	    		JsonNode queryResult = mapper.readValue(serviceResponse.readEntity(String.class), JsonNode.class);
				// Setting results for the response (for now we set them all and 
	    		// later we will replace them with those at the first page)
				endPointForm.setResult(queryResult);
				// Setting total items for the response
				endPointForm.setTotalItems(queryResult.get("results").get("bindings").size());
				
    		}
    		
		} 
    	catch (IOException e) {
			e.printStackTrace();
		}

		return endPointForm;
		
	}
    
}