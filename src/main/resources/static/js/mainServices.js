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

/**
 * The main angularJS service to hold the factories
 * 
 * @author Vangelis Kritsotakis
 */

angular.module('app.mainServices', [])

.factory('queryService', function($http, $timeout, $q) {
	
	var splitStr = '#@#';
	
	return {
		getQueryResults : function(queryModel, token) {

			return $http({
				'url' : '/executequery_json',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data' : queryModel
			}).then(function (success){
				if(success.data.result != null) {
					combineValuesToCreateLinks(success.data.result.results, splitStr);
				}
				return success.data;
			},function (error) {
				return error;
			});

		},
		
		getPageResults : function(pageParams) {
			return $http({
				'url' : '/paginator_json',
				'method' : 'GET',
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : pageParams
			}).then(function (success){
				if(success.data.result != null) {
					combineValuesToCreateLinks(success.data.result.results, splitStr);
				}
				return success.data;
			},function (error) {
				//error code
			});
		},
		
		getNameGraphsResults : function(token, additionalParams) {
			return $http({
				'url' : '/retrieveNameGraphs',
				'method' : 'GET',
				'headers' : {
					'Content-Type' : 'application/json',
					'Authorization': token
				},
				'params' : additionalParams
			}).then(function (success){
				return success.data;
			},function (error) {
				//error code
			});

		},
		
		checkAuthorization : function(token) {
			return $http({
				'url' : '/checkAuthorization',
				'method' : 'GET',
				'headers' : {
					'Content-Type' : 'application/json',
					'Authorization': token
				}
			}).then(function (success){
				return success.data;
			},function (error) {
				//error code
			});

		},
		
		getGeoQueryResults : function(queryModel, token) {

			return $http({
				'url' : '/executegeoquery_json',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data' : queryModel
			}).then(function (success) {
				if(success.data.result != null) {
					combineValuesToCreateLinks(success.data.result.results, splitStr);
				}
				return success.data;
			},function (error) {
				//error code
			});

		},
		
		getAdvancedQueryResults : function(queryModel, token) {

			return $http({
				'url' : '/executeadvancedquery_json',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data' : queryModel
			}).then(function (success){
				if(success.data.result != null) {
					combineValuesToCreateLinks(success.data.result.results, splitStr);
				}
				return success.data;
			},function (error) {
				return error;
			});

		},
	}
	
	// Takes a json array (jsonResults) of json objects and parses it to discover values 
	// that have a specific substring (splitStr). This is used to split that values into 
	// uri and name in order to create links with labels.
	function combineValuesToCreateLinks(jsonResults, splitStr) {
		// Creating links by splitting concated fields
		angular.forEach(jsonResults, function (row) {
			angular.forEach(row, function (value, key) {
				var name = '';
				var uri = '';
				// If the field is concut
	            if(value.value.indexOf(splitStr) !== -1) {
	            	uri = value.value.split(splitStr)[0];
	            	name = value.value.split(splitStr)[1];
	            	value.link = true;
	            	value.name = name;
	            	value.uri = uri;
	            }
			});
        });  
	}
	
})

.factory('importService', function($http, $timeout, $q) {
	
	
	// TODO: Define importModel in the controller
	return {
		importRdf : function(importModel, token) {

			return $http({
				'url' : '/impor_rdf',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json',
					'Authorization': token
				},
				'data' : importModel
			}).then(function (success){
				return success.data;
			},function (error) {
				//error code
			});

		}
	}
});