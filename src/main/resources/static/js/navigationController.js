/**
 * The main angularJS controllers (handling tabs, the query submission and serverside paginator and the file importing
 * 
 * @author Vangelis Kritsotakis
 */
app.controller("navigationCtrl", ['$state', '$scope', '$parse', '$sessionStorage', 'authenticationService', 'modalService', 'queryService', '$mdSidenav', 'ivhTreeviewMgr', '$http', '$log', '$mdDialog', 
                                  function($state, $scope, $parse, $sessionStorage, authenticationService, modalService, queryService, $mdSidenav, ivhTreeviewMgr, $http, $log, $mdDialog) {
	
	$scope.headingTitle = "Metadata Search";
	
	// Calling service to get the user's credentials (token, userId)
	function initCredentials() {
		$scope.credentials = authenticationService.getCredentials();
		if($scope.credentials == undefined) {
			$state.go('login', {});
		}
	}
	
	initCredentials();
	
	function checkAuthorization() {
		
	}
	checkAuthorization();
	
	var modalOptions = {
		headerText: 'Loading Please Wait...',
		bodyText: 'Your query is under process...'
	};
	
	var modalDefaults = {
		backdrop: true,
		keyboard: true,
		modalFade: true,
		templateUrl: '/views/loadingModal.html'
	};
	
	// Used to inform user that his token is no longer valid and will be logged out
	$scope.showLogoutAlert = function() {
	    // Appending dialog to document.body to cover sidenav in docs app
	    // Modal dialogs should fully cover application
	    // to prevent interaction outside of dialog
	    $mdDialog.show(
	      $mdDialog.alert()
	        .parent(angular.element(document.querySelector('#popupContainer')))
	        .clickOutsideToClose(true)
	        .title('Attention Please')
	        .textContent('Either your session has been expired or you are no longer authorized to continue.')
	        .ariaLabel('Alert Dialog Demo')
	        .ok('OK')
	    ).finally(function() { 
	    	$state.go('login', {});
	    });
	};
	
	// Used to inform user that error has occured
	$scope.showErrorAlert = function(title, msg) {
		$mdDialog.show(
			$mdDialog.alert()
				.parent(angular.element(document.querySelector('#popupContainer')))
				.clickOutsideToClose(true)
				.title(title)
				.textContent(msg)
				.ariaLabel('Alert Dialog')
				.ok('OK')
		)
	};
	
	
	
		    
	// Toggles SidePanel
	$scope.toggleInfo = buildToggler('rightInfo');
	$scope.toggleTreeMenu = buildToggler('treeMenu');
	
	function buildToggler(componentId) {
      return function() {
        $mdSidenav(componentId).toggle();
      };
    }
	
	$scope.hiddenTreeMenuTogglerButton = false;
	
	$scope.hideTreeMenuTogglerButton = function(boolean) {
		$scope.hiddenTreeMenuTogglerButton = boolean;
	}
	
	$scope.stuff = [{
		id: 'vre',
		label: 'VREs',
		children: [
			{label: 'ekt-data', value: 'ekt-data'},
			{label: 'rcuk-data', value: 'rcuk-data'},
			{label: 'epos-data', value: 'epos-data'}
		]
	},{
		id: 'ri',
		label: 'RIs',
		children: [
			{label: 'fris-data', value: 'fris-data'},
			{label: 'envri-data', value: 'envri-data'}
		]
	}]
	/*
	$scope.stuff = [{
			id: 'hats',
			label: 'Hats',
			children: [
				{label: 'Flat cap'},
				{label: 'Top hat'},
				{label: 'Gatsby'}
			]
		},{
			id: 'pens',
			label: 'Pens',
			selected: true,
			children: [
		    	{label: 'Fountain'},
		    	{label: 'Gel ink'},
		    	{label: 'Fedora'},
		    	{label: 'Baseball', selected: true},
		    	{label: 'Roller ball'},
		    	{label: 'Fiber tip'},
		    	{label: 'Ballpoint'}
		    ]
		},{
			id: 'whiskey',
			label: 'Whiskey',
			children: [
		    	{label: 'Irish'},
		    	{label: 'Scotch'},
		    	{label: 'Rye'},
		    	{label: 'Tennessee'},
		    	{label: 'Bourbon'}
		    ]
		}];
	*/
	
	$scope.awesomeCallback = function(node, tree) {
		// Do something with node or tree
		//alert("awesomeCallback");
	};

	$scope.otherAwesomeCallback = function(node, isSelected, tree) {
		// Do soemthing with node or tree based on isSelected
		//alert("otherAwesomeCallback: \nlabel: " + node.label + "\nselected: " + isSelected);
		console.log("otherAwesomeCallback: \nlabel: " + node.label + "\nselected: " + isSelected);
		console.log(angular.toJson($scope.stuff));
		
		//console.log("otherAwesomeCallback: \nlabel: " + node.label + "\nselected: " + node.selected);
	}
	
	$scope.selectHats = function() {
	    // Selecting by node id
	    ivhTreeviewMgr.select($scope.stuff, 'hats');
	    //scope.$apply();
	};
	
	$scope.deselectGel = function() {
	    // deselect by node reference
	    ivhTreeviewMgr.deselect($scope.stuff, $scope.stuff[1].children[1]);
	};
	
	$scope.testBreadcrumb = function(str) {
	    alert(str);
	};
	
	$scope.breadcrumbItems = 
		[{
			id: '1',
			label: 'Frank Norson',
			icon: 'fa fa-user'
		},{
			id: '2',
			label: 'Semantic Web Paper',
			icon: 'fa fa-file-text'
		},{
			id: '3',
			label: 'Institute of Everything',
			icon: 'fa fa-building'
		},{
			id: '4',
			label: 'Happy Life Project',
			icon: 'fa fa-users'
		},{
			id: '5',
			label: 'Bloody Jurnal',
			icon: 'fa fa-file-text'
		},{
			id: '6',
			label: 'University of Students',
			icon: 'fa fa-university'
		},{
			id: '7',
			label: 'East Laos',
			icon: 'fa fa-globe'
		}];
	
	$scope.goToHomeView = function() {
		$state.go('welcome', {});
	}

	// To be used for constructing the simple related entity query dynamically
    $scope.relatedEntityQuerySearchText = '';
	
	// All entities used in the row model
    /*
	$scope.allEntities = [{
		name: 'Persons', 
			thesaurus: 'thesaurus/persons-firstAndLastNames.json', 
        	queryModel: {
        		format: 'application/json',
    			query: 'PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n' +
    				   'select distinct ?persName ?Service @#$%FROM%$#@\n' + 
    				   'where {' +
    					  '?pers a <http://eurocris.org/ontology/cerif#Person>. ' + 
    					  '?pers rdfs:label ?persName. ' + 
    					  '?pers cerif:is_source_of ?FLES. ' + 
    					  '?FLES cerif:has_destination ?Ser. ' + 
    					  '?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>. ' + 
    					  '?Ser cerif:has_acronym ?Service.?pers a <http://eurocris.org/ontology/cerif#Person>. ' + 
    					  '?pers rdfs:label ?persName. ' + 
    					  '?persName bds:search \'' + '@#$%TERM%$#@' + '\'. ' + 
    					  '?persName bds:matchAllTerms \'true\'. ' + 
    					  '?persName bds:relevance ?score. ' + 
    					'} ' + 
    					'ORDER BY desc(?score) ?pers ' + 
    					'limit 100'
        		}
		}, {
			name: 'Projects', 
			thesaurus: 'thesaurus/project-acronyms.json'
		}, {
			name: 'Publications', 
			thesaurus: 'thesaurus/publications-titles.json'
		}, {
			name: 'Organization Units', 
			thesaurus: 'thesaurus/organizationUnits-acronyms.json'
		}, {
			name: 'Resources', 
			thesaurus: 'thesaurus/resources.json'
		}];
	*/
    
	// Relations used in the row model
	$scope.relations = [{
			name: 'is related'
		},{
			name: 'has author'
		},{
			name: 'has editor'
		},{
			name: 'is member of'
		},{
			name: 'produced by'
		},{
			name: 'located at'
		}
	];
	
	$scope.allEntities = [];
	
	// Target entity
	$scope.selectedTargetEntity = {name: '', thesaurus: ''};
	$scope.targetEntities = $scope.allEntities;//angular.copy($scope.allEntities);
	$scope.searchTargetKeywords = '';
	$scope.targetThesaurus = {};
	$scope.selectedTargetRecomentation = null;
	$scope.targetChips = [];
	
	var autoincrementedRowModelId = 0;
	
	// Initial empty row model
	$scope.initEmptyRowModel = {
		id: autoincrementedRowModelId,
		outerSelectedFilterExpression: '',
		selectedRelation: '',
		relations: angular.copy($scope.relations),
		rangeOfDates: {
			from: null,//new Date(),
			fromInputName: '',
			until: null,
			untilInputName: ''
		},
		selectedRelatedEntity: null,//{name: '', thesaurus: '', queryModel: ''},
		relatedEntities: $scope.allEntities,//angular.copy($scope.allEntities),
		searchRelatedKeywords: '',
		allRelatedSearchResultsIsSelected: false,
		allRelatedEntitiesSelectedList: [{name: 'All Instances Selected'}],
		selectedRelatedInstanceList: [],
		shownEntitySearchResults: false,
		selectedRecomentation: null,
		relatedChips: [],
		relatedEntitySearchText: '',
		rowModelList: [],
		activeRelatedSearchResultsStyle: 'enabled-style'//,
		//activeStyle: 'disabled-style'
	}
	
	$scope.thesaurus = {};
	
	// Uncheck All-Related-Search-Results option (when removing the single chip)
	$scope.unselectAllRelatedSearchResults = function(rowModel) {
		rowModel.allRelatedSearchResultsIsSelected = false;
		$scope.handleSelectAllRelatedSearchResults(rowModel);
	}
	
	// Initializing All available entities
	function initAllEntities() {
		queryService.getAllEntities().then(function (response) {
			//console.log(angular.toJson(response));
			$scope.allEntities = response.data;
			$scope.targetEntities = response.data;
			$scope.initEmptyRowModel.relatedEntities = response.data;
			$scope.rowModelList[0].relatedEntities = response.data;
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
		}).finally(function(){
			//$scope.$apply();
		});
	}
	
	initAllEntities();
	
	// Initializing empty row model (new instance)
	function initRowModels() {
		$scope.emptyRowModel = angular.copy($scope.initEmptyRowModel);
		//alert($scope.emptyRowModel.relatedChips.length===0);
	}
	
	initRowModels();
	
	$scope.rowModelList = [$scope.emptyRowModel];
		
	$scope.addNewEmptyRowModel = function(parentRowModel, str) {
		
		autoincrementedRowModelId++;
		
		if(parentRowModel == null) {
			$scope.rowModelList.push(angular.copy($scope.initEmptyRowModel));
			$scope.rowModelList[$scope.rowModelList.length-1].outerSelectedFilterExpression = str;
			$scope.rowModelList[$scope.rowModelList.length-1].id = autoincrementedRowModelId;
		}
		else { //if(parentRowModel != null) {
			parentRowModel.rowModelList.push(angular.copy($scope.initEmptyRowModel));
			parentRowModel.rowModelList[parentRowModel.rowModelList.length-1].outerSelectedFilterExpression = str;
			parentRowModel.rowModelList[parentRowModel.rowModelList.length-1].id =  autoincrementedRowModelId;
			//rowModel.outerSelectedFilterExpression = str;
		}
		//rowModel.activeStyle = 'enabledStyle';
		
		//$log.info(JSON.stringify($scope.rowModelList));
	}
	
	$scope.removeRowModel = function(outerIndex, rowModel) {
		// Removes 1 item at position outerIndex
		//$scope.rowModelList.splice(outerIndex, 1)
		if(rowModel == null) {
			$scope.rowModelList.splice(outerIndex, 1)
		}
		else { //if(rowModel != null) {
			rowModel.rowModelList.splice(outerIndex, 1)
		}
		
		//$log.info(JSON.stringify($scope.rowModelList));
	}
	
	$scope.changeLogicalExpressionOfRowModel = function(rowModel) {
		if(rowModel.outerSelectedFilterExpression=='OR') {
			rowModel.outerSelectedFilterExpression = 'AND';
		}
		else { //if(rowModel.outerSelectedFilterExpression=='AND')
			rowModel.outerSelectedFilterExpression = 'OR';
		}
	}
	
	// Adding filter on related entity
	$scope.addFilterOnRelated = function(rowModel) {
		autoincrementedRowModelId++;
		rowModel.rowModelList.push(angular.copy($scope.initEmptyRowModel));
		//rowModel.rowModelList[outerIndex].activeStyle = 'enabledStyle';
		rowModel.rowModelList[rowModel.rowModelList.length-1].id = autoincrementedRowModelId;
		$log.info(JSON.stringify($scope.rowModelList));
	}
	
	// SpeedDialModes
	$scope.targetSpeedDialMode = 'md-scale'; //'md-scale'
	$scope.relatedSpeedDialMode = 'md-scale';
	
	
	$scope.selectRelatedEntityFromResults = function(rowModel, index) {
		//////rowModel.selectedRelatedEntityResult = rowModel.relatedEntityResults[index];
		rowModel.selectedRelatedInstanceList = [{name: rowModel.relatedEntityResults[index].name}];
	};
	
	$scope.pressEnterOnEntitySearchResults = function(outerIndex, keyEvent) {
		if (keyEvent.which === 13)
			$scope.showEntitySearchResults(outerIndex, true);
	}
	
	// Adding list of results to the respective instance of the rowModel
	$scope.showEntitySearchResults = function(rowModel, boolean) {
		rowModel.shownEntitySearchResults = boolean;
	}
	
	// some animate example
	//http://plnkr.co/edit/oCLzLHbDLtNT8G8rKFJS?p=preview
	
	// Thesaurus	
	
	$scope.loadThesaurus = function(entity, outerIndex) {
		return $http.get(entity.thesaurus, {cache: true}).then(function(response) {
        	//$log.info('$scope.targetThesaurus ' + JSON.stringify($scope.targetThesaurus));
        	// Case Target
        	if(outerIndex == -1) {
        		$scope.targetThesaurus = response.data;
        	}
        	// Case Related Entity
        	else { //if(entityCase == 'related')
        		$scope.thesaurus = response.data;
        	}
        });
    };
	
    $scope.querySearch = function(query, outerIndex) {
    	var results = null;
    	if(outerIndex == -1) {
    		results = query ? $scope.targetThesaurus.filter( createFilterFor(query) ) : $scope.targetThesaurus;
    	}
    	else { //if(entityCase == 'related')
    		results = query ? $scope.thesaurus.filter( createFilterFor(query) ) : $scope.thesaurus;
    	}
        return results;
    }
    
    function createFilterFor(query) {
    	var lowercaseQuery = angular.lowercase(query);
    	return function filterFn(thesaurusItem) {//alert("thesaurus: " + thesaurus);
    		return (angular.lowercase(thesaurusItem.name).indexOf(lowercaseQuery) === 0);
    	};
    }
        
    $scope.selectedItemChange = function(item, outerIndex) {
    	$log.info('Item in outer-index: ' + outerIndex+ ' changed to ' + JSON.stringify(item));
    	$log.info($scope.emptyRowModel.relatedChips.length===0);
    }
    
    $scope.currRowModel = null;
    
    $scope.serviceModel = { 
		url: 'http://139.91.183.70:8080/EVREMetadataServices-1.0-SNAPSHOT',
		namespace: 'vre4eic'
	}
	
    /*
	$scope.queryModel = {
		format: 'application/json',
		query: 'PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n' +
			   'select distinct ?persName ?Service from <http://ekt-data>' + 
			   'where {' +
				  '?pers a <http://eurocris.org/ontology/cerif#Person>. ' + 
				  '?pers rdfs:label ?persName. ' + 
				  '?pers cerif:is_source_of ?FLES. ' + 
				  '?FLES cerif:has_destination ?Ser. ' + 
				  '?FLES cerif:has_classification <http://139.91.183.70:8090/vre4eic/Classification.provenance>. ' + 
				  '?Ser cerif:has_acronym ?Service.?pers a <http://eurocris.org/ontology/cerif#Person>. ' + 
				  '?pers rdfs:label ?persName. ' + 
				  '?persName bds:search \'' + relatedEntityQuerySearchText + '\'. ' + 
				  '?persName bds:matchAllTerms \'true\'. ' + 
				  '?persName bds:relevance ?score. ' + 
				'} ' + 
				'ORDER BY desc(?score) ?pers ' + 
				'limit 100'
	}
    */
    /*
    function updateRelatedEntityQueryModel(queryModel, searchText) {
    	var model = $parse(queryModel.query.relatedEntityQuerySearchText);
    	model.assign($scope, searchText);
    	return queryModel;
	}
    */ //Remove $parse from controller
    
    $scope.showRelatedResultsDialog = function(ev, rowModel) {
    	
    	// Trying with promise - Start
    	var modalInstance = modalService.showModal(modalDefaults, modalOptions);
    	
    	/*
    	relatedEntitySearchText
		allRelatedSearchResultsIsSelected
		relatedChips
		selectedRelatedInstanceList
    	*/
    	
    	// Determine what searchText is before executing query
    	
    	//relatedChips
    	
    	var querySearchText = '';
    	
    	angular.forEach(rowModel.relatedChips, function(value, key) {
    		querySearchText = querySearchText + ' ' + value.name;
    	});
    	
    	if($scope.relatedEntitySearchText != null && rowModel.relatedEntitySearchText != '') {
    		querySearchText = querySearchText + ' ' + rowModel.relatedEntitySearchText;
    	}
    	
    	var updatedQuery = angular.copy(rowModel.selectedRelatedEntity.queryModel.query).replace('@#$%TERM%$#@', querySearchText);
    	updatedQuery = updatedQuery.replace('@#$%FROM%$#@','from <http://rcuk-data> from <http://fris-data>');
    	var updatedQueryModel = angular.copy(rowModel.selectedRelatedEntity.queryModel)
    	updatedQueryModel.query = updatedQuery;
    	
    	// Executing Query
    	queryService.getEntityQueryResults($scope.serviceModel, updatedQueryModel, $scope.credentials.token)
		.then(function (response) {
			
			if(response.status == -1) {
				$scope.message = 'There was a network error. Try again later.';
				$scope.showErrorAlert('Error', $scope.message);
				modalInstance.close();
			}
			
			else {
				// Checking the response from blazegraph
				if(response.status == '200') {
					
					$scope.relatedEntityResults = response.data;
					
					for(var i=0; i<response.data.results.bindings.length; i++) { // Iterating response that doesn't have 'isChecked' element
						if(containedInList($scope.relatedEntityResults.results.bindings[i], rowModel.selectedRelatedInstanceList, true).contained) {
							$scope.relatedEntityResults.results.bindings[i].isChecked = true;
						}
			    	}
					
					if($scope.relatedEntityResults.results != undefined)
		    			$scope.totalItems = $scope.relatedEntityResults.results.bindings.length;
					
					modalInstance.close();
					
					// Trying with dummy data directly from here - To be changed after promise is functional
			    	// Before showing the dialog, the results must me copied in the model
			    	// In the final version this list must pre-check items that match with the list of selected
			    	//rowModel.relatedEntityResults = angular.copy($scope.relatedEntityResults1);
					
					// Used for capturing the current row and thus knowing where to put selected items
			    	$scope.currRowModel = rowModel;
			    	$mdDialog.show({
			    		scope: $scope,
			    		templateUrl: 'views/dialog/selectFromResults.tmpl.html', 
			    		parent: angular.element(document.body),
			    		targetEvent: ev,
			    		//clickOutsideToClose:true,
			    		preserveScope: true,
			    		fullscreen: false // Only for -xs, -sm breakpoints.
			    	})
			    	.then(function(answer) {
			    		$scope.status = 'You are OK';
			    	}, function() {
			    		$scope.status = 'You cancelled the dialog.';
			    	});
				}
				else if(response.status == '400') {
					$log.info(response.status);
					modalInstance.close();
				}
				else if(response.status == '401') {
					$log.info(response.status);
					modalInstance.close();
					$scope.showLogoutAlert();
					authenticationService.clearCredentials();
				}
				else {
					$log.info(response.status);
					modalInstance.close();
				}
			
			} // else close
			
		
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
			modalInstance.close();
		});
    	// Tring with promise - End
    	
    	
	};
	
	$scope.closeRelatedEntitySearchResults = function(rowModel) {
		// Hide dialog
		$mdDialog.cancel();
		// Show related entity results panel on respective rowModel
		if(rowModel.shownEntitySearchResults == false && rowModel.selectedRelatedInstanceList.length > 0) {
			rowModel.shownEntitySearchResults = true;
		}
		else if (rowModel.shownEntitySearchResults == true && rowModel.selectedRelatedInstanceList.length < 1) {
			rowModel.shownEntitySearchResults = false;
		}
	}
	
	$scope.maxSize = 5;
	$scope.currentPage = 1;
	$scope.itemsPerPage = 10;
	
	$scope.pageChanged = function() {
		//getData();
	};
    
	// adding or removing selected items from the searched results of the related entity
	// to the list of selected related items
	$scope.changeSelectedRelatedItem = function(item) {
		$log.info('currRowModel: ' + $scope.currRowModel.id );
		
		if(item.isChecked==true) {
			// Returned item from the function that checks whether the item is contained in the list
			var containedElement = containedInList(item, $scope.currRowModel.selectedRelatedInstanceList, false);
			if(!containedElement.contained) {
				$scope.currRowModel.selectedRelatedInstanceList.push(item);
			}
		}
		else { //if(item.isChecked==false)
			// Returned item from the function that checks whether the item is contained in the list
			var containedElement = containedInList(item, $scope.currRowModel.selectedRelatedInstanceList, true);
			if(containedElement.contained) {
				$scope.currRowModel.selectedRelatedInstanceList.splice(containedElement.index, 1);
			}
		}
		//$log.info('$scope.relatedEntityResults.results: ' + angular.toJson($scope.relatedEntityResults.results.bindings) );
	}
	
	$scope.removeSelectedRelatedItem = function(rowModel, itemIndex) {
		$log.info('itemIndex: ' + itemIndex);
		rowModel.selectedRelatedInstanceList.splice(itemIndex, 1);
		// Hide related entity search results' panel if there are no items to show
		if(rowModel.selectedRelatedInstanceList.length < 1) {
			rowModel.shownEntitySearchResults = false;
		}
	}

	$scope.handleSelectAllRelatedSearchResults = function(rowModel) {
		
		if(rowModel.allRelatedSearchResultsIsSelected) {
			// Disabled look & feel for the data table
			rowModel.activeRelatedSearchResultsStyle = 'disabled-style';
			// The list of selected becomes empty
			rowModel.selectedRelatedInstanceList = [];
			// All checked boxes becomes un-checked
			angular.forEach($scope.relatedEntityResults.results.bindings, function(value, key) {
				if(value.isChecked === true) {
					value.isChecked = false;
				}
			});
			rowModel.allRelatedEntitiesSelectedList = [{name: 'All Instances Selected'}];
		}
		
		else { //(selected==false)
			// Enabled look & feel for the data table
			rowModel.activeRelatedSearchResultsStyle = 'enabled-style';
		}
	}
	
	// Determines if an item is contained into a list
	function containedInList(item, list, ignoreIsCheckedProperty) {
		
		var containedElement = {'contained': false, 'index': -1};
		
    	//var contained = false;
    	//var index = -1;
    	
		// Ignoring the 'isChecked property'
    	if (ignoreIsCheckedProperty == true) {
			var tempListItem = null; 				// Used in order to delete the isChecked property and compare
			var tempItem = angular.copy(item); 	// Used in order to delete the isChecked property and compare
			for(var i=0; i<list.length; i++) {
				
				tempListItem = angular.copy(list[i]);
				if (tempListItem.hasOwnProperty('isChecked')) {
					delete tempListItem.isChecked;
				}
				
				if (tempItem.hasOwnProperty('isChecked')) {
					delete tempItem.isChecked;
				}
				
			    if(angular.toJson(tempListItem) === angular.toJson(tempItem)) {
			    	containedElement.contained = true;
			    	containedElement.index = i;
			    }
			}
    	}
    	
    	// Pure compare
    	else {// Pure check
        	for(var i=0; i<list.length; i++){
        	    if(angular.toJson(list[i]) === angular.toJson(item)) {
        	    	containedElement.contained = true;
        	    	containedElement.index = i;
        	    }
        	}
    	}
    		
    	return containedElement;
    }
	
	$scope.test = function() {
/*
		queryService.getAllEntities().then(function (response) {
			console.log(angular.toJson(response));
			}, function (error) {
				$scope.message = 'There was a network error. Try again later.';
				alert("failure message: " + $scope.message + "\n" + JSON.stringify({
					data : error
				}));
			});
			*/
	}
	
	
	/*
	angular.forEach(list, function(value, key) {
		//if()
		//exists = 
		$log.info('value: ' + value);
	});
	*/
	
	
}]);