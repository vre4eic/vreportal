/*******************************************************************************
 * Copyright (c) 2018 VRE4EIC Consortium
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *     http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 *******************************************************************************/

package forth.ics.isl.data.model;

import com.fasterxml.jackson.databind.JsonNode;

/**
 * Pojo (Plain Old Java Object) used for the structure of messages exchanged 
 * between angularJS sparQL Query submission and Spring
 * 
 * @author Vangelis Kritsotakis
 */

public class EndPointForm {

	private String query;
	private JsonNode result;
	private int totalItems;
	private int itemsPerPage;
	private int statusRequestCode;
	private String statusRequestInfo;
	private String authorizationToken;
	private boolean supportedCase;

	public String getQuery() {
		return query;
	}
	public void setQuery(String query) {
		this.query = query;
	}
	public JsonNode getResult() {
		return result;
	}
	public void setResult(JsonNode result) {
		this.result = result;
	}
	public int getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}
	public int getItemsPerPage() {
		return itemsPerPage;
	}
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
	public int getStatusRequestCode() {
		return statusRequestCode;
	}
	public void setStatusRequestCode(int statusRequestCode) {
		this.statusRequestCode = statusRequestCode;
	}
	public String getStatusRequestInfo() {
		return statusRequestInfo;
	}
	public void setStatusRequestInfo(String statusRequestInfo) {
		this.statusRequestInfo = statusRequestInfo;
	}
	public String getAuthorizationToken() {
		return authorizationToken;
	}
	public void setAuthorizationToken(String authorizationToken) {
		this.authorizationToken = authorizationToken;
	}
	public boolean isSupportedCase() {
		return supportedCase;
	}
	public void setSupportedCase(boolean supportedCase) {
		this.supportedCase = supportedCase;
	}
	
	
}
