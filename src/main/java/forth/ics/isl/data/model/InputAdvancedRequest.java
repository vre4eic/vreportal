package forth.ics.isl.data.model;

/**
 * Pojo (Plain Old Java Object) used for the structure of messages exchanged 
 * between angularJS sparQL Query submission and Spring
 * 
 * @author Vangelis Kritsotakis
 */

public class InputAdvancedRequest {

	int itemsPerPage;
	String QueryToExecute;
	
	public int getItemsPerPage() {
		return itemsPerPage;
	}
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
	public String getQueryToExecute() {
		return QueryToExecute;
	}
	public void setQueryToExecute(String queryToExecute) {
		QueryToExecute = queryToExecute;
	}
		
}
