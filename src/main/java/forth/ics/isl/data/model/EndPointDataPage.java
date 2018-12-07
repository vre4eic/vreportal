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

import com.fasterxml.jackson.databind.node.ObjectNode;

/**
 * Pojo (Plain Old Java Object) used for the structure of messages exchanged 
 * between angularJS the pagination and Spring
 * 
 * @author Vangelis Kritsotakis
 */

public class EndPointDataPage {
	private ObjectNode result;
	private int page;
	private int totalItems;
	
	public ObjectNode getResult() {
		return result;
	}
	public void setResult(ObjectNode result) {
		this.result = result;
	}
	public int getPage() {
		return page;
	}
	public void setPage(int page) {
		this.page = page;
	}
	public int getTotalItems() {
		return totalItems;
	}
	public void setTotalItems(int totalItems) {
		this.totalItems = totalItems;
	}
	
}
