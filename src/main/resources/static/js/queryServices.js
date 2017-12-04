/**
 * The main angularJS service to hold the factories
 * 
 * @author Vangelis Kritsotakis
 */

angular.module('app.mainServices', [])

.factory('queryService', function($http, $timeout, $q) {
	
	var splitStr = '#@#';
	
	return {
		computeRelatedEntityQuery : function(searchEntityModel, token) {
			return $http({
				'url' : '/related_entity_query',
				'method' : 'POST',
				'headers': {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data' : searchEntityModel
			}).then(function (success){
				return success;
			},function (error) {
				//error code
			});
		},
		
		getEntityQueryResultsCount : function(serviceModel, queryModel, token) {

			return $http({
				'url': serviceModel.url + '/query/count/namespace/' + serviceModel.namespace,
				'method': 'POST',
				'headers': {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data': queryModel
			}).then(function (success){
				return success;
			},function (error) {
				return error;
			});

		},
		
		getEntityQueryResults : function(serviceModel, queryModel, token) {
			return $http({
				'url': serviceModel.url + '/query/namespace/' + serviceModel.namespace,
				'method': 'POST',
				'headers': {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data': queryModel
			}).then(function (success){
				return success;
			},function (error) {
				return error;
			});
		},
		
		getEntities : function(queryFrom, token) {
			return $http({
				'url': '/get_entities',
				'method': 'POST',
				'headers': {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data': {fromSearch: queryFrom}
			}).then(function (success){
				return success;
			},function (error) {
				return error;
			});
		},
		/*
		getAllEntities : function() {
			return $http({
				'url' : '/get_all_entities',
				'method' : 'GET',
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : ''
			}).then(function (success){
				return success;
			},function (error) {
				//error code
			});
		},
		*/
		getAllNamegraphs : function() {
			return $http({
				'url' : '/get_all_namedgraphs',
				'method' : 'GET',
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : ''
			}).then(function (success){
				return success;
			},function (error) {
				//error code
			});
		},
		
		getRelationsAndRelatedEntitiesByTarget : function(paramModel, token) {
			return $http({
				'url': '/get_relations_related_entities',
				'method': 'POST',
				'headers': {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data': paramModel
			}).then(function (success){
				return success;
			},function (error) {
				return error;
			});
		},
		
		getRelationsByTargetAndRelatedEntity : function(paramModel, token) {
			return $http({
				'url': '/get_relations',
				'method': 'POST',
				'headers': {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data': paramModel
			}).then(function (success){
				return success;
			},function (error) {
				return error;
			});
		},
		
		computeFinalSearchQuery : function(paramModel, token) {
			return $http({
				'url': '/final_search_query',
				'method': 'POST',
				'headers': {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data': paramModel
			}).then(function (success){
				return success;
			},function (error) {
				return error;
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
		
		saveIntoFavorites : function(model, token) {

			return $http({
				'url' : '/save_into_favorites',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data' : model
			}).then(function (success) {
				return success;
			},function (error) {
				//error code
			});

		},
		
		removeFromFavoritesById : function(model, token) {

			return $http({
				'url' : '/remove_from_favorites_by_id',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data' : model
			}).then(function (success) {
				return success;
			},function (error) {
				//error code
			});

		},
		
		retrieveFavoriteQueryModelsByUsername : function(model, token) {

			return $http({
				'url' : '/retrieve_favorite_query_models_by_user',
				'method' : 'POST',
				'headers' : {
					'Content-Type' : 'application/json',
				    'Authorization': token
				},
				'data' : model
			}).then(function (success) {
				return success;
			},function (error) {
				//error code
			});

		}//,
		
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
				return success;
			},function (error) {
				return error;
			});

		}
	}
});