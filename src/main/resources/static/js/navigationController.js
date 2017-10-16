/**
 * The main angularJS controllers (handling tabs, the query submission and serverside paginator and the file importing
 * 
 * @author Vangelis Kritsotakis
 */
app.controller("navigationCtrl", ['$state', '$scope', '$timeout', '$parse', '$sessionStorage', 'authenticationService', 'modalService', 'queryService', '$mdSidenav', 'ivhTreeviewMgr', '$http', '$log', '$mdDialog', 
                                  function($state, $scope, $timeout, $parse, $sessionStorage, authenticationService, modalService, queryService, $mdSidenav, ivhTreeviewMgr, $http, $log, $mdDialog) {
	
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
    
    // Old method
    /*
    $scope.showRelatedResultsDialog = function(ev, rowModel) {
    	
    	// Trying with promise - Start
    	var modalInstance = modalService.showModal(modalDefaults, modalOptions);
    	
    	// The search text to feed the query
    	var querySearchText = '';
    	
    	angular.forEach(rowModel.relatedChips, function(value, key) {
    		querySearchText = querySearchText + ' ' + value.name;
    	});
    	
    	if(rowModel.relatedEntitySearchText != null && rowModel.relatedEntitySearchText != '') {
    		querySearchText = querySearchText + ' ' + rowModel.relatedEntitySearchText;
    	}
    	
    	// Feeding the query with the respective search text
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
	*/
	
    
    
    
    
    
    
    
    
    $scope.showRelatedResultsDialog = function(ev, rowModel) {
    	
    	// Trying with promise - Start
    	var modalInstance = modalService.showModal(modalDefaults, modalOptions);
    	
    	// The search text to feed the query
    	var querySearchText = '';
    	
    	angular.forEach(rowModel.relatedChips, function(value, key) {
    		querySearchText = querySearchText + ' ' + value.name;
    	});
    	
    	if(rowModel.relatedEntitySearchText != null && rowModel.relatedEntitySearchText != '') {
    		querySearchText = querySearchText + ' ' + rowModel.relatedEntitySearchText;
    	}
    	
    	// Feeding the query with the respective search text
    	
    	var searchEntityModel = {
    		searchText: querySearchText,
    		fromSearch: 'from <http://ekt-data> from <http://rcuk-data> from <http://fris-data> from <http://epos-data> from <http://envri-data>'
    	}
    	
    	// Executing Query
    	queryService.searchEntityResults($scope.serviceModel, searchEntityModel, $scope.credentials.token)
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
		
		// Delay
		// This is nice but has issues, thus in comment
		// I am applying ng-scope = animateToRemove
		// So if I drop this completely then remove it from the html as well
		/*
        $timeout( function() {
        	//item.animateToRemove = 'removed-item';
        	rowModel.selectedRelatedInstanceList[itemIndex].animateToRemove = 'removed-item';
        });
        */
		
        //rowModel.selectedRelatedInstanceList[itemIndex].animateToRemove = 'removed-item';
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
	

	
		
	
		
		
		
		
		
		
		
		
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
		
	$scope.showMapForRelatedResultsDialog = function(ev, rowModel) {
		
		// Used for capturing the current row and thus knowing where to put selected items
    	$scope.currRowModel = rowModel;
    	$mdDialog.show({
    		//scope: $scope,
    		templateUrl: 'views/dialog/selectFromMap.tmpl.html', 
    		parent: angular.element(document.body),
    		targetEvent: ev,
    		clickOutsideToClose:true,
    		//onComplete: loadMapForRelatedEntity(),
    		onComplete:function(){
    				loadMapForRelatedEntity();
    		},
    		fullscreen: true,
    		preserveScope: true,
    		fullscreen: false // Only for -xs, -sm breakpoints.
    	})
    	.then(function(answer) {
    		console.log("OK");
    	}, function() {
    		$scope.status = 'You cancelled the dialog.';
    	}).finally(function() {
    		console.log("OK");
    	});
	}

	function loadMapForRelatedEntity() {
		
		// Starting with map in related entity
					
		$scope.pins = [{ 
		    	type:"Product", 
		    	selectedImg: "../images/Map-Marker-Marker-Inside-Pink-icon.png", 
		    	unselectedImg: "../images/Map-Marker-Marker-Outside-Pink-icon.png"
			},{ 
		    	type:"Equipment", 
		    	selectedImg: "../images/Map-Marker-Marker-Inside-Chartreuse-icon.png", 
		    	unselectedImg: "../images/Map-Marker-Marker-Outside-Chartreuse-icon.png"
			},{ 
		    	type:"Facility", 
		    	selectedImg: "../images/Map-Marker-Marker-Inside-Azure-icon.png", 
		    	unselectedImg: "../images/Map-Marker-Marker-Outside-Azure-icon.png"
			}]
		
		
		var mousePositionControl = new ol.control.MousePosition({
	        coordinateFormat: ol.coordinate.createStringXY(4),
	        projection: 'EPSG:4326',// 3857 //4326
	        // comment the following two lines to have the mouse position
	        // be placed within the map.
	        className: 'custom-mouse-position',
	        target: document.getElementById('mouse-position'),
	        undefinedHTML: '&nbsp;'
		});
		
		
		var logoElement = document.createElement('a');
		logoElement.href = 'https://www.vre4eic.eu/';
		logoElement.target = '_blank';
	    
	    var logoImageElement = document.createElement('img');
	    logoImageElement.src = 'https://www.vre4eic.eu/images/logo-vre4eic-modern.svg';
	    logoImageElement.style.fontSize = '200%';
	    
	    logoElement.appendChild(logoImageElement);
	    
	    var explainPinsElement = document.createElement('span');
	    
	    // Content of attribution (displayed along with the logo)
	    angular.forEach($scope.pins, function (pin) {
	    	var pinLabelElement = document.createElement('span');
	    	pinLabelElement.style.fontSize = '200%';
	    	pinLabelElement.innerHTML = pin.type + ':';
	        var pinImgElement = document.createElement('img');
	        pinImgElement.src = pin.unselectedImg;
	        pinImgElement.style.fontSize = '200%';
	        
	        explainPinsElement.appendChild(pinLabelElement);
	        explainPinsElement.appendChild(pinImgElement);
	       
	    });
	    
	    
		
	    var attribution = new ol.Attribution({
	        html: explainPinsElement.innerHTML
	      });
	        
	    $scope.map = new ol.Map({
			controls: ol.control.defaults({
				attributionOptions: /** @type {olx.control.AttributionOptions} */ ({
		            collapsible: true,
		            tipLabel: 'Information regarding the pins on the map'
				})
			})
		    .extend([mousePositionControl]),
		    //.extend([new ol.control.FullScreen()]),
	        layers: [
	          new ol.layer.Tile({
	            source: new ol.source.OSM({attributions: [attribution]})
	          })//,
	          //vectorlayer
	        ],
	        target: 'map',
	        view: new ol.View({
	        	center: [1000000, 4912205], // Default is [0, 0]
	        	zoom: 5, //Default is 2
	        	minZoom: 2,
	        	maxZoom: 19
	        }),					// To hide logo use:
	        logo: logoElement 	//document.createElement('span')
		});      
	      
		// a DragBox interaction used to select features by drawing boxes
	    $scope.dragBox = new ol.interaction.DragBox({
			condition: ol.events.condition.platformModifierKeyOnly
		});
	
		$scope.map.addInteraction($scope.dragBox);
	      
		$scope.coordinates = [];
		$scope.coordinatesRegion = {};
	      
		$scope.infoBox = document.getElementById('info');
	
		$scope.dragBox.on('boxend', function() {
			// Holding all 5 coordinates in a string and transform them to the appropriate projection
			var coordinateStr = $scope.dragBox.getGeometry().transform('EPSG:3857', 'EPSG:4326').getCoordinates();
			//alert (coordinateStr);
			convertCoordinatesToJson(coordinateStr);
	    	  
			// required for immediate update 
			$scope.$apply();
		});
	    
		// Coordinates are shown as (latitude, longitude) pairs and not the opposite 
		// which is the usual way of presenting them.
		//
		// Coordinates are delivered circular like shown below. The first pair is the 
		// same as the last (fifth) pair due to polygon and not rectangular.
		//	
		// [[[0,1],[2,3],[4,5],[6,7],[8,9]]]
		//
		// (8,9)
		// (0,1)		(6,7)
		//		---------
		//		|		|
		//		|		|
		//		---------
		// (2,3)		(4,5)
		//	
	      
		function convertCoordinatesToJson(coordinateStr) {
			var thebox = coordinateStr.toString().split(",");
			// Using parseFloat in order to convert the strings to floats
			// and been able to apply comparisons
			var latitude1 = parseFloat(thebox[0]);
			var longitude1 = parseFloat(thebox[1]);
			var latitude2 = parseFloat(thebox[2]);
			var longitude2 = parseFloat(thebox[3]);
			var latitude3 = parseFloat(thebox[4]);
			var longitude3 = parseFloat(thebox[5]);
			var latitude4 = parseFloat(thebox[6]);
			var longitude4 = parseFloat(thebox[7]);
			var latitude5 = parseFloat(thebox[8]);	// This is the same as latitude1
			var longitude5 = parseFloat(thebox[9]); // This is the same as longitude1
	    	    
			$scope.coordinates = [
			    {longitude1: longitude1, latitude1: latitude1},
	    	    {longitude2: longitude2, latitude2: latitude2},
	    	    {longitude3: longitude3, latitude3: latitude3},
	        	{longitude4: longitude4, latitude4: latitude4},
	        	{longitude5: longitude5, latitude5: latitude5},
	    	];
	    	
			// Because there are many ways (4 different ways) of drawing the rectangle 
			// (i.e. top-left to bottom-right or bottom-left to top-right), we have to 
			// determine nort-south and west-east according to which one is greatest or smallest
			
			// Determining north and south
			var north;
			var south;
			
			if(longitude1 > longitude2) {
				north = longitude1;
				south = longitude2;
			}
			else {
				north = longitude2;
				south = longitude1;
			}
			
			// Determining west and east
			var west;
			var east;
			
			if(latitude1 < latitude4) {
				west = latitude1;
				east = latitude4;
			}
			else {
				west = latitude4;
				east = latitude1;
			}
			
			//$scope.coordinatesRegion = {north: latitude1, south: latitude2, west: longitude1, east: longitude4}
			$scope.coordinatesRegion = {north: north, south: south, west: west, east: east}
			console.log(longitude1 + ", " + latitude1 + ", " + longitude2 + ", " + latitude2 + ", " + longitude3 + ", " + latitude3 + ", " + longitude4 + ", " + latitude4 + ", " + longitude5 + ", " + latitude5);
			console.log("north: " + north + ", south: " + south + ", west: " + west + ", east: " + east);
			
			// Calling the service
			$scope.retrieveGeoData();
			
	      }
	      
	      // clear selection when drawing a new box and when clicking on the map
	      $scope.dragBox.on('boxstart', function() {
	    	  $scope.infoBox.innerHTML = '&nbsp;';
	      });
	      
	      $scope.map.on('click', function() {
	        $scope.infoBox.innerHTML = '&nbsp;';
	      });
	
	      //$scope.vectorLayers = [];
	      
	      // Displaying on map
	      function handleGeoResultsForMap(geoResults) {
	    	  
	    	  if($scope.map.getLayers().getLength() > 1) {
	    		  
				  // Removing with inverse order all layers apart from that one with index 0 (this is the map) 
	    		  for (i = $scope.map.getLayers().getLength(); i > 0; i--) {
	    			  $scope.map.removeLayer($scope.map.getLayers().item(i));
	    		  }
			  }
	    	  
	    	  // Unselected Pink Icon
	    	  $scope.iconStylePinkUnselected = new ol.style.Style({
	    		  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
	    	          anchor: [0.3, 1],
	    	          offset: [22, 0],
	    	          size: [128, 128],
	    	          src: '../images/Map-Marker-Marker-Outside-Pink-icon.png',
	    	          scale: 0.3
	    		  }))
	    	  });
	    	  
	    	  // Selected Pink Icon
	    	  $scope.iconStylePinkSelected = new ol.style.Style({
	    		  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
	    	          anchor: [0.3, 1],
	    	          offset: [22, 0],
	    	          size: [128, 128],
	    	          src: '../images/Map-Marker-Marker-Inside-Pink-icon.png',
	    	          scale: 0.3
	    		  }))
	    	  });
	    	  
	    	// Unselected Green Icon
	    	  $scope.iconStyleGreenUnselected = new ol.style.Style({
	    		  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
	    	          anchor: [0.3, 1],
	    	          offset: [22, 0],
	    	          size: [128, 128],
	    	          src: '../images/Map-Marker-Marker-Outside-Chartreuse-icon.png',
	    	          scale: 0.3
	    		  }))
	    	  });
	    	  
	    	  // Selected Green Icon
	    	  $scope.iconStyleGreenSelected = new ol.style.Style({
	    		  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
	    	          anchor: [0.3, 1],
	    	          offset: [22, 0],
	    	          size: [128, 128],
	    	          src: '../images/Map-Marker-Marker-Inside-Chartreuse-icon.png',
	    	          scale: 0.3
	    		  }))
	    	  });
	    	  
	    	  // Unselected Blue Icon
	    	  $scope.iconStyleBlueUnselected = new ol.style.Style({
	    		  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
	    	          anchor: [0.3, 1],
	    	          offset: [22, 0],
	    	          size: [128, 128],
	    	          src: '../images/Map-Marker-Marker-Outside-Azure-icon.png',
	    	          scale: 0.3
	    		  }))
	    	  });
	    	  
	    	  // Selected Blue Icon
	    	  $scope.iconStyleBlueSelected = new ol.style.Style({
	    		  image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
	    	          anchor: [0.3, 1],
	    	          offset: [22, 0],
	    	          size: [128, 128],
	    	          src: '../images/Map-Marker-Marker-Inside-Azure-icon.png',
	    	          scale: 0.3
	    		  }))
	    	  });
	    	  
	    	  var polyFeatures = [];	// Array to hold the polygons
	    	  var pointFeatures = [];	// Array to hold the points
	    	  
	    	  // Style for the polygons to be used for presenting the region
	    	  
	    	  var areaStyle = new ol.style.Style({
		          stroke: new ol.style.Stroke({
		              color: 'blue',
		              width: 1
		          }),
		          fill: new ol.style.Fill({
		        	  color: 'rgba(0, 0, 255, 0.1)'
		          })
		      });
	    	  
	    	  for (i = 0; i < geoResults.length; i++) {
	    		  
	    		  // Polygon Feature (rectangular)
	    		  var polyFeature = new ol.Feature({
	        		  geometry: new ol.geom.Polygon([
	        		  	[
	        		       [parseFloat(geoResults[i].west.value), parseFloat(geoResults[i].north.value)],
	        		       [parseFloat(geoResults[i].west.value), parseFloat(geoResults[i].south.value)],
	        		       [parseFloat(geoResults[i].east.value), parseFloat(geoResults[i].south.value)],
	        		       [parseFloat(geoResults[i].east.value), parseFloat(geoResults[i].north.value)],
	        		       [parseFloat(geoResults[i].west.value), parseFloat(geoResults[i].north.value)]
	        		    ]
	    		      ])
	        	  });
	    		  
	    		  
	    		  polyFeature.setStyle(areaStyle);
	    		  polyFeatures.push(polyFeature); // Adding into Array
	    		  
	    		  // Handling nulls in pointFeature property by using logic and wrappers
	    		  var responsibleWrapper = '';
	    		  if(geoResults[i].Responsible == null) {
	    			  responsibleWrapper = "--";
	    		  }
	    		  else {
	    			  if(geoResults[i].Responsible.value == null) {
	    				  responsibleWrapper = "--";
	        		  }
	    			  else {
	    				  responsibleWrapper = geoResults[i].Responsible.value;
	    			  }
	    		  }
	    		  
	    		  var serviceWrapper = '';
	    		  if(geoResults[i].Service == null) {
	    			  serviceWrapper = "--";
	    		  }
	    		  else {
	    			  if(geoResults[i].Service.value == null) {
	    				  serviceWrapper = "--";
	        		  }
	    			  else {
	    				  serviceWrapper = geoResults[i].Service.value;
	    			  }
	    		  }
	    		  
	    		  var nameWrapper = '';
	    		  var uriWrapper = '';
	    		  if(geoResults[i].resource == null) {
	    			  nameWrapper = "--";
	    			  uriWrapper = "--";
	    		  }
	    		  else {
	    			  if(geoResults[i].resource.value == null) {
	        			  nameWrapper = "--";
	        			  uriWrapper = "--";
	        		  }
	    			  else {
	    				  uriWrapper = geoResults[i].resource.value.split(splitStr)[0];
	    				  nameWrapper = geoResults[i].resource.value.split(splitStr)[1];
	    			  }
	    		  }
	    		  
	    		  var typeWrapper = '';
	    		  if(geoResults[i].type == null) {
	    			  typeWrapper = "--";
	    		  }
	    		  else {
	    			  if(geoResults[i].type.value == null) {
	    				  typeWrapper = "--";
	        		  }
	    			  else {
	    				  typeWrapper = geoResults[i].type.value;
	    			  }
	    		  }
	    		  
	    		  // Point Feature (with marker icon)
	    		  // Since we have rectangular this point is the center of the polygon
	        	  var pointFeature = new ol.Feature({
	        	      geometry: polyFeature.getGeometry().getInteriorPoint(),
	        	      featureType: 'marker',
	        	      name: '<a href="' + uriWrapper + '" target="_blank">' + nameWrapper + '</a>',
	        	      type: typeWrapper,
	        	      //description: descrWrapper,
	        	      responsible: responsibleWrapper,
	        	      service: serviceWrapper
	        	  });
	        	  
	        	  // Change  coordinate systems to display on the map
	        	  polyFeature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
	        	  pointFeature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
	        	  
	        	  if(geoResults[i].type == null) {
	        		  pointFeature.setStyle($scope.iconStylePinkUnselected);
	    		  }
	    		  else {
	    			  if(geoResults[i].type.value == null) {
	    				  pointFeature.setStyle($scope.iconStylePinkUnselected);
	        		  }
	    			  else if(geoResults[i].type.value == 'Product') {
	    				  pointFeature.setStyle($scope.iconStylePinkUnselected);
	    			  }
	    			  else if(geoResults[i].type.value == 'Equipment') {
	    				  pointFeature.setStyle($scope.iconStyleGreenUnselected);
	    			  }
	    			  else if(geoResults[i].type.value == 'Facility') {
	    				  pointFeature.setStyle($scope.iconStyleBlueUnselected);
	    			  }
	    		  }
	        	          	  
	        	  pointFeatures.push(pointFeature); // Adding into Array
	        	          	          	  
	    	  }
	    	  
	    	  //A vector layer to hold the features
	    	  var polyVectorLayer = new ol.layer.Vector({
	    		  name: 'polyVectorLayer',
	    	      source: new ol.source.Vector({
	    	          features: polyFeatures
	    	      })
	    	  });
	    	  
	    	  $scope.map.addLayer(polyVectorLayer);	// Adding Layer with all the polygons
	    	  
	    	  //A vector layer to hold the features
	    	  var pointVectorLayer = new ol.layer.Vector({
	    		  name: 'pointVectorLayer',
	    	      source: new ol.source.Vector({
	    	          features: pointFeatures
	    	      })
	    	  });
	    	  
	    	  $scope.map.addLayer(pointVectorLayer);	// Adding Layer with all the points
	    	  
	    	  // Trying with animation but...
	    	  /*
	    	  pointVectorLayer.animateFeature (pointFeatures[0], [	
	  				new ol.featureAnimation["Drop"]({
	  					speed: 0.8,
	  					duration: 760,
	  					side: false
	  				}),
	  				new ol.featureAnimation["Bounce"]({
	  					speed: 0.8,
	  					duration: 760,
	  					side: false
	  				})
	  		  ]);
	    	  */
	    	  
	    	  /*
	    	  for (i = 0; i < vectorLayers.getLength(); i++) {
	    		  map.addLayer(vectorLayers[i]);
	    	  }
	    	  */    	 
	      }
	      
	      var popoverElement = document.getElementById('popup');
	
	      var popup = new ol.Overlay({
	          element: popoverElement,
	          positioning: 'bottom-center',
	          stopEvent: true, // If false popover's click and wheel events won't work 
	          offset: [0, -50]
	        });
	      
	      $scope.map.addOverlay(popup);
	      
	      // Handling on click of point-marker
	      $scope.map.on('click', function(evt) {
	    	  
	    	  $scope.map.getLayers().forEach(function(layer) {
	    		  // Only for points
	    		  if (layer.get('name') == 'pointVectorLayer') {
	    			  layer.getSource().getFeatures().forEach(function(feature) {
	    				  
	    	    		  if(feature.get('type') == 'Product') {
	    	    			  feature.setStyle($scope.iconStylePinkUnselected);
		    			  }
		    			  else if(feature.get('type') == 'Equipment') {
		    				  feature.setStyle($scope.iconStyleGreenUnselected);
		    			  }
		    			  else if(feature.get('type') == 'Facility') {
		    				  feature.setStyle($scope.iconStyleBlueUnselected);
		    			  }
		    			  else {//if(feature.get('type') == '--') {
	    					  feature.setStyle($scope.iconStylePinkUnselected);
	    	    		  }
	    	    		  
	    			  });
	    		  }
	    		  
	    		  //$scope.$apply();
			  });
	    	  
	    	  // Displaying popup
	    	  var element = popup.getElement();
	
	    	  var feature = $scope.map.forEachFeatureAtPixel(evt.pixel,
	    	      function(feature) {
	    	        return feature;
	    	      });
	    	  if(feature != undefined) {
	    	  
	    		  if (feature.get('featureType') == 'marker') {
	    		      		  
		    		  // Setting new marker-icon (that looks different) for the clicked marker
					  if(feature.get('type') == 'Product') {
						  feature.setStyle($scope.iconStylePinkSelected);
					  }
					  else if(feature.get('type') == 'Equipment') {
						  feature.setStyle($scope.iconStyleGreenSelected);
					  }
					  else if(feature.get('type') == 'Facility') {
						  feature.setStyle($scope.iconStyleBlueSelected);
					  }
					  else {//if(feature.get('type') == '--') {
						  feature.setStyle($scope.iconStylePinkSelected);
		    		  }
	    		  
		    	      $(element).popover('destroy');
		    	      var coordinates = feature.getGeometry().getCoordinates();
		    	      popup.setPosition(coordinates);
		    	      // the keys are quoted to prevent renaming in ADVANCED mode.
		    	      $(element).popover({
		    	          'placement': 'top',
		    	          'animation': false,
		    	          'html': true,
		    	          'title': '<b>' + feature.get('type') + '</b>',
		    	          'content': feature.get('name') + " by " + feature.get('service') + "</br></br><span style=\"text-decoration: underline;\">Responsible:</span> <i>" + feature.get('responsible') + "</i>"
		    	      });
		    	      $(element).popover('show');
		    	  } else {
		    		  $(element).popover('destroy');
		    	      popup.setPosition(undefined);
		    	  }
	    	  
	    	  } else {
	    		  $(element).popover('destroy');
	    		  popup.setPosition(undefined);
	    	  }
	      });
	      
	      // change mouse cursor when over marker
	      var target = $scope.map.getTarget();
	      var jTarget = typeof target === "string" ? $("#" + target) : $(target);
	      // change mouse cursor when over marker
	      $($scope.map.getViewport()).on('mousemove', function (e) {
	          var pixel = $scope.map.getEventPixel(e.originalEvent);
	          var hit = $scope.map.forEachFeatureAtPixel(pixel, function (feature, layer) {
	        	  // Only for points
	        	  if (layer.get('name') == 'pointVectorLayer') {
	        		  return true;
	        	  }
	          });
	          if (hit) {
	              jTarget.css("cursor", "pointer");
	          } else {
	              jTarget.css("cursor", "");
	          }
	      });
	      
	      $scope.retrieveGeoData = function() {
	  		var queryModel = {
	              north : $scope.coordinatesRegion.north,
	              south : $scope.coordinatesRegion.south,
	              west : $scope.coordinatesRegion.west,
	              east : $scope.coordinatesRegion.east,
	              itemsPerPage : $scope.itemsPerPage
	  		};
	  		
			// Modal
			var modalInstance = modalService.showModal(modalDefaults, modalOptions);
			 
			queryService.getGeoQueryResults(queryModel, $scope.credentials.token)
			.then(function (response){
				
				$scope.statusRequestInfo = response.statusRequestInfo;
				
				// Close alerts
				$scope.alerts.splice(0);
				
				// Checking the response from blazegraph
				if(response.statusRequestCode == '200') {
					//console.log("queryModel: " + queryModel.north + ", " + queryModel.south + ", " + queryModel.west + ", " + queryModel.east);
					//console.log("200 - getGeoQueryResults: " + response.result.results);
					$scope.lastEndPointForm = response;
					$scope.endpointResult = response.result;
					$scope.totalItems = response.totalItems;
					$scope.currentPage = 1;
					$scope.alerts.push({type: 'success-funky', msg: 'Searching completed successfully'});
					
					// handling results
					handleGeoResultsForMap(response.result.results);
				}
				else if(response.statusRequestCode == '400') {
					$scope.alerts.push({type: 'danger-funky', msg: response.statusRequestInfo});
				}
				else if(response.statusRequestCode == '401') {				
					$scope.showLogoutAlert();
					authenticationService.clearCredentials();
				}
				else {
					$scope.alerts.push({type: 'danger-funky', msg: response.statusRequestInfo});
				}
				
				modalInstance.close();
			},function (error){
				$scope.message = 'There was a network error. Try again later.';
				alert("failure message: " + $scope.message + "\n" + JSON.stringify({
					data : error
				}));
				modalInstance.close();
			});
	  			
	  		// Submit stuff Ends here
	  		
	  	}
		
	
	}
	
	
	/*
	angular.forEach(list, function(value, key) {
		//if()
		//exists = 
		$log.info('value: ' + value);
	});
	*/
	
	
}]);