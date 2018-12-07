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

import java.util.List;

/**
 * Pojo (Plain Old Java Object) used for the structure of messages exchanged 
 * between angularJS sparQL Query submission and Spring
 * 
 * @author Vangelis Kritsotakis
 */

public class InputGeoRequest {
	
	int itemsPerPage;
	String north;
	String south;
	String west;
	String east;
	
	public int getItemsPerPage() {
		return itemsPerPage;
	}
	public void setItemsPerPage(int itemsPerPage) {
		this.itemsPerPage = itemsPerPage;
	}
	public String getNorth() {
		return north;
	}
	public void setNorth(String north) {
		this.north = north;
	}
	public String getSouth() {
		return south;
	}
	public void setSouth(String south) {
		this.south = south;
	}
	public String getWest() {
		return west;
	}
	public void setWest(String west) {
		this.west = west;
	}
	public String getEast() {
		return east;
	}
	public void setEast(String east) {
		this.east = east;
	}

	
}
