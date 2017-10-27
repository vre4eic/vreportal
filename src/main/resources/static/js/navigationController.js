/**
 * The main angularJS controllers (handling tabs, the query submission and serverside paginator and the file importing
 * 
 * @author Vangelis Kritsotakis
 */
app.controller("navigationCtrl", ['$state', '$scope', '$timeout', '$parse', '$sessionStorage', 'authenticationService', 'modalService', 'queryService', '$mdSidenav', 'ivhTreeviewMgr', '$http', '$log', '$mdDialog', '$mdToast', 
                                  function($state, $scope, $timeout, $parse, $sessionStorage, authenticationService, modalService, queryService, $mdSidenav, ivhTreeviewMgr, $http, $log, $mdDialog, $mdToast) {
	
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
	
	var modalDefaultOptions = {
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
	        .ariaLabel('Logout Message')
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
				.ariaLabel('Error Message')
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
	
	$scope.namegraphs = [{
		id: 'vre',
		label: 'VREs',
		selected: true,
		children: [
			{label: 'ekt-data', value: 'ekt-data', selected: true},
			{label: 'rcuk-data', value: 'rcuk-data', selected: true},
			{label: 'epos-data', value: 'epos-data', selected: true}
		]
	},{
		id: 'ri',
		label: 'RIs',
		selected: true,
		children: [
			{label: 'fris-data', value: 'fris-data', selected: true},
			{label: 'envri-data', value: 'envri-data', selected: true}
		]
	}];
	
	$scope.queryFrom = '';
	
	function constructQueryForm(namegraphs) {
		$scope.queryFrom = '';
		angular.forEach(namegraphs, function(parentValue, parentKey) {
			angular.forEach(parentValue.children, function(childValue, childKey) {
				if(childValue.selected) {
					$scope.queryFrom = $scope.queryFrom + 'from <' + childValue.value + '> ';
				}
			})
		});
		
		// In case of none selected, use some fictional namegraph,
		// such that searching in the whole namespace is avoided
		if($scope.queryFrom == '') {
			$scope.queryFrom = $scope.queryFrom + 'from <http://none> ';
		}
	}
	
	function makeAllNamegraphsSelected(namegraphs) {
		angular.forEach(namegraphs, function(parentValue, parentKey) {
			parentValue.selected = true;
			angular.forEach(parentValue.children, function(childValue, childKey) {
				childValue.selected = true;
			})
		});
		return namegraphs;
	}
	
	// Initializing All available entities
	function initAllNamegraphs() {
		queryService.getAllNamegraphs().then(function (response) {
			//console.log(angular.toJson(response));
			
			$scope.namegraphs = makeAllNamegraphsSelected(response.data);
			// Initialising the queryFrom string
			constructQueryForm(response.data);
			console.log('$scope.queryFrom: ' + $scope.queryFrom);
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
		}).finally(function(){
			//$scope.$apply();
		});
	}
	
	initAllNamegraphs();
	
	
	
	/*
	$scope.namegraphs = [{
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
		
	$scope.namegraphTreeCallback = function(ev, node, isSelected, tree) {
		//console.log("namegraphTreeCallback: \nlabel: " + node.label + "\nselected: " + isSelected);
		//console.log(angular.toJson($scope.namegraphs));
		
		//console.log("namegraphTreeCallback: \nlabel: " + node.label + "\nselected: " + node.selected);
		
		constructQueryForm($scope.namegraphs);
		$log.info('$scope.queryFrom: ' + $scope.queryFrom);
		
		updateAvailableEntities(ev, $scope.queryFrom, node);
		
		
	}
	
	$scope.selectHats = function() {
	    // Selecting by node id
	    ivhTreeviewMgr.select($scope.namegraphs, 'hats');
	};
	
	$scope.deselectGel = function() {
	    // deselect by node reference
	    ivhTreeviewMgr.deselect($scope.namegraphs, $scope.namegraphs[1].children[1]);
	};
	
	$scope.testBreadcrumb = function(str) {
	    alert(str);
	};
	
	// Retrieving all available entities
	function updateAvailableEntities(ev, queryFrom, checkboxNode) {
		var modalOptions = {
			headerText: 'Loading Please Wait...',
			bodyText: 'Updating available options...'
		};
		
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
		
		queryService.getEntities(queryFrom, $scope.credentials.token)
		.then(function (response) {
			console.log('getEntities:');
			console.log(response.data);
			
			if(response.status == -1) {
				$scope.message = 'There was a network error. Try again later.';
				$scope.showErrorAlert('Error', $scope.message);
				modalInstance.close();
			}
			
			else {
				// Checking the response status
				if(response.status == '200') {
					
					var report = checkForUnavailableEntities(response.data);
					
					console.log("report:");
					console.log(angular.toJson(report));
					
					console.log("$scope.unavailableCurrentlySelectedRelatedEntities:");
					console.log($scope.unavailableCurrentlySelectedRelatedEntities);
					
					// Check from report then call dialog which will do the following
					
					modalInstance.close();
					
					// Conflicts
					if(!report.targetAvailability.available || !report.relatedAvailability.available) {
						
						var msg = 'There are currently selected entities that are no longer available.';
						
						// Case - Both target and related entities are unavailable
						if(!report.targetAvailability.available && !report.relatedAvailability.available) {
							// Target
							msg = 'The selected <code class="blueCodeStyle">target</code> entity <code><i>\'' + 
							  $scope.selectedTargetEntity.name + 
							  '\'</i></code> is no longer avaialbe and shoold be re-selected.'
							
							// Related
							// More than one related entities are unavailable
							if(report.relatedAvailability.unavailableEntityList.length > 1) {
								msg = msg + '<br/>Furthermore, the folowing selected <code class="blueCodeStyle">related</code> ' + 
									  'entities are not available anymore and and should be re-selected as well:<br/> ';
								
								for(var i=0; i<report.relatedAvailability.unavailableEntityList.length; i++) {
									msg = msg + '<br/><code><i>' + '\'' + 
										  report.relatedAvailability.unavailableEntityList[i].name + 
										  '\';</i></code>';
								}
							}
							
							// Only one related entity is unavailable
							else {
								msg = msg + '<br/>Similar, the selected <code class="blueCodeStyle">related</code> entity <code><i>\'' + 
									  report.relatedAvailability.unavailableEntityList[0].name + 
									  '\'</i></code> is also not avaialbe anymore and shoold be re-selected.'
							}
						}
						
						// Case - Only target entity is unavailable
						else if(!report.targetAvailability.available) {
							msg = 'The selected <code class="blueCodeStyle">target</code> entity <code><i>\'' + 
								  $scope.selectedTargetEntity.name + 
								  '\'</i></code> is no longer avaialbe and shoold be re-selected.'
						}
						
						// Case - Only related entity is unavailable
						else if(!report.relatedAvailability.available) {
							
							// More than one related entities are unavailable
							if(report.relatedAvailability.unavailableEntityList.length > 1) {
								msg = 'The folowing selected <code class="blueCodeStyle">related</code> entities are no longer ' + 
									  'available and should be re-selected:<br/> ';
								
								for(var i=0; i<report.relatedAvailability.unavailableEntityList.length; i++) {
									msg = msg + '<br/><code><i>\'' + 
										  report.relatedAvailability.unavailableEntityList[i].name + 
										  '\';</i></code>';
								}
							}
							
							// Only one related entity is unavailable
							else {
								msg = 'The selected <code class="blueCodeStyle">related</code> entity <code><i>\'' + 
									  report.relatedAvailability.unavailableEntityList[0].name + 
									  '\'<i></code> is no longer avaialbe and shoold be re-selected.'
							}
							
						}

						msg = msg + '</br></br>Do you want to proceed?'
						
						showConfirmDialogForUnavailableEntities(ev, msg, response.data, checkboxNode)
					}
					
					// No conflicts
					else {
						loadNewEntityLists(response.data); // Apply the change
					}
				}
				else {
					$log.info(response.status);
					modalInstance.close();
					$scope.showLogoutAlert();
				}
			
			} // else close
			
		
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
			modalInstance.close();
		});
	}
	
	// Method used to recursively update the available entities in 
	// the RowModelList tree
	function recursivelyUpdateEntityListsOfRowModelList(rowModelList, entityList) {
		
		angular.forEach(rowModelList, function(rowModel, rowModelKey) {
			// Updating available related entities in rowModel
			rowModel.relatedEntities = entityList;
			
			// Iterating the new list in order to retain the selected one if still available
			if(rowModel.selectedRelatedEntity != null) {
				angular.forEach(entityList, function(entity, key) {
					if(rowModel.selectedRelatedEntity.name == entity.name)
						rowModel.selectedRelatedEntity = entity;
				});
			}
			recursivelyUpdateEntityListsOfRowModelList(rowModel.rowModelList, entityList);
		});
	}
		
	// Method used to recursively check whether the selected entities in the
	// rowModelList tree are still available in the list of entities after it 
	// is updated (the list)
	function recursivelyCheckEntityAvailabilityInRowModelList(rowModelList, entityList) {
		var isAvailable = false;
		for(var i=0; i<rowModelList.length; i++) {
			
			// First we check if the selected is null
			if(rowModelList[i].selectedRelatedEntity != null) {
				// Iterating the new list in order to check availability
				for(var j=0; j<entityList.length; j++) {
					if(rowModelList[i].selectedRelatedEntity.name == entityList[j].name) {
						isAvailable = true;
						break;
					}				
				}
			}
			// When not selected, then it is available
			else {
				isAvailable = true;
			}
			
			if(isAvailable == false) {
				// Push if not already pushed
				if(angular.toJson($scope.unavailableCurrentlySelectedRelatedEntities).indexOf(angular.toJson(rowModelList[i].selectedRelatedEntity)) == -1)
					$scope.unavailableCurrentlySelectedRelatedEntities.push(rowModelList[i].selectedRelatedEntity);
			}

			recursivelyCheckEntityAvailabilityInRowModelList(rowModelList[i].rowModelList, entityList);
		}
		return isAvailable;
	}
	
	// Check availability of currently selected entities for both target and rowModelTree
	// wrt to the new updated list of entities
	function checkForUnavailableEntities(newEntityList) {
		
		$scope.unavailableCurrentlySelectedRelatedEntities = []; // Init list
		
		var selectedTargetEntityIsAvailable = false;
		var selectedRelatedEntitiesAreAvailable = false;
		
		// Checking if there is any selected target entity
		if($scope.selectedTargetEntity != null) {
			for(var i=0; i<newEntityList.length; i++) {
				if($scope.selectedTargetEntity.name == newEntityList[i].name) {
					selectedTargetEntityIsAvailable = true;
				    break;
				}
			}
		}
		
		// Always consider available the target entity if no yet selected
		else {
			selectedTargetEntityIsAvailable = true;
		}
		
		selectedRelatedEntitiesAreAvailable = 
				recursivelyCheckEntityAvailabilityInRowModelList($scope.rowModelList, newEntityList);
		
		return {
			targetAvailability: {
				available: selectedTargetEntityIsAvailable
			},
			relatedAvailability: {
				available: selectedRelatedEntitiesAreAvailable,
				unavailableEntityList: $scope.unavailableCurrentlySelectedRelatedEntities
			}
		};
		
	}
	
	// Shows confirm dialog whether to change the list of entities.
	// Only displayed when some of the currently selected entities are no
	// available in the new list of entities
	function showConfirmDialogForUnavailableEntities(ev, messageContent, entityList, checkboxNode) {
		var confirm = $mdDialog.confirm()
		.title('Important Message')
		.htmlContent(messageContent)
		.ariaLabel('Target Entity Selection - No longer Available')
		.targetEvent(ev)
		.ok('Yes Continue')
		.cancel('Cancel');

	    $mdDialog.show(confirm).then(function() {
	    	loadNewEntityLists(entityList); // Apply the change
	    }, function() {
	    	// Un-check or check the last selected/de-selected VRE checkbox
	    	/*
	    	if(checkboxNode.selected)
	    		ivhTreeviewMgr.deselect($scope.namegraphs, checkboxNode.id);
	    	else
	    		ivhTreeviewMgr.select($scope.namegraphs, checkboxNode.id);
	    	*/
	    	if(checkboxNode.selected) {
		    	checkboxNode.selected = false;
		    	angular.forEach(checkboxNode.children, function(child, key) {
		    		child.selected = false;
		    	});
	    	}
	    	else {
	    		checkboxNode.selected = true;
		    	angular.forEach(checkboxNode.children, function(child, key) {
		    		child.selected = true;
		    	});
	    	}
	    	console.log("TODO: append IDs to the namegraph children such that ivhTreeviewMgr is used for unchecking in case of canseling applied change")
	    });
	};
	
	// Changes all the available entity list with new one
	function loadNewEntityLists(entityList) {
		// Update targetEntities List and retain selection if still available
		$scope.targetEntities = entityList;
		
		// Retain the selection if there is one and is 
		// still available in the new list
		if($scope.selectedTargetEntity != null) {
			angular.forEach(entityList, function(entity, key) {
				if($scope.selectedTargetEntity.name == entity.name) {
					$scope.selectedTargetEntity = entity;
					console.log('targetEntities changed');
				}
			});
		}
		// Update all related entity lists that have already been defined
		recursivelyUpdateEntityListsOfRowModelList($scope.rowModelList, entityList);
		// Update allEntities list
		$scope.allEntities = entityList;
		// Update available entities for the EmptyRowModel to be used for now on
		$scope.initEmptyRowModel.relatedEntities = entityList;
		
		// Display msg
		$mdToast.show(
			$mdToast.simple()
	        .textContent('All available entity lists have been modified.')
	        .position('top right')
	        .parent(angular.element('#mainContent'))
	        .hideDelay(3000)
	    );
	}//textContent
	
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
	$scope.selectedTargetEntity = null;
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
	}
	
	// SpeedDialModes
	$scope.targetSpeedDialMode = 'md-scale'; //'md-scale'
	$scope.relatedSpeedDialMode = 'md-scale';
	
	
	$scope.selectRelatedEntityFromResults = function(rowModel, index) {
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
		if(!(entity.thesaurus == "" || entity.thesaurus == null)) {
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
		}
		else {
			if(outerIndex == -1) {
        		$scope.targetThesaurus = "";
        	}
        	// Case Related Entity
        	else { //if(entityCase == 'related')
        		$scope.thesaurus = "";
        	}
		}
    };
	
    // Used in autocomplete recommendations
    $scope.querySearch = function(query, outerIndex) {
    	var results = [];
    	if(outerIndex == -1) {
    		if($scope.targetThesaurus != '')
    			results = query ? $scope.targetThesaurus.filter( createFilterFor(query) ) : $scope.targetThesaurus;
    	}
    	else { //if(entityCase == 'related')
    		if($scope.thesaurus != '')
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
	
    $scope.relatedEntityResultsCount = 0;
    
    $scope.showRelatedResultsDialog = function(ev, rowModel) {
    	
    	// Trying with promise - Start
    	
    	var modalOptions = {
			headerText: 'Loading Please Wait...',
			bodyText: 'Search process undergoing...'
		};
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
    		entity: rowModel.selectedRelatedEntity.name,
    		query: rowModel.selectedRelatedEntity.queryModel.query,
    		geospatial: rowModel.selectedRelatedEntity.geospatial,
    		searchText: querySearchText,
    		fromSearch: $scope.queryFrom
    		//fromSearch: 'from <http://ekt-data> from <http://rcuk-data> from <http://fris-data> from <http://epos-data> from <http://envri-data>'
    	}
    	
    	// Getting the query from back-end - Promise
    	
    	var updatedQueryModel = '';
    	var updatedQueryModelPerPage = '';
    	
    	queryService.computeRelatedEntityQuery(searchEntityModel, $scope.credentials.token).then(function (queryResponse) {

    		updatedQueryModel = angular.copy(rowModel.selectedRelatedEntity.queryModel)
        	updatedQueryModel.query = queryResponse.data.query;
    		delete updatedQueryModel.geo_query;
    		delete updatedQueryModel.text_geo_query;
    		
    		// Calling Service to get the count wrt to the query - Promise
    		queryService.getEntityQueryResultsCount($scope.serviceModel, updatedQueryModel, $scope.credentials.token)
    		.then(function (queryCountResponse) {
    			
    			// Holding total number of results
    			$scope.relatedEntityResultsCount = queryCountResponse.data.results.bindings[0].count.value;
    			console.log('$scope.relatedEntityResultsCount: ' + $scope.relatedEntityResultsCount);
    			
    			// Change query such that only the first 10 are returned
    			updatedQueryModelPerPage = Object.assign(updatedQueryModel);
    			/////// After the line below, the updatedQueryModelPerPage.query becomes NA 
    			var queryPerPage = "";//angular.copy(updatedQueryModelPerPage.query);
    			queryPerPage = angular.copy(updatedQueryModelPerPage.query) + ' limit ' + $scope.itemsPerPage.toString() + ' offset ' + ($scope.currentPage-1).toString();
    			updatedQueryModelPerPage.query = queryPerPage;
    			
	    		// Calling service to executing Query - Promise
	    		queryService.getEntityQueryResults($scope.serviceModel, updatedQueryModelPerPage, $scope.credentials.token)
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
	    					
	    					// Iterating response that doesn't have 'isChecked' element
	    					for(var i=0; i<response.data.results.bindings.length; i++) { // Iterating response that doesn't have 'isChecked' element
	    						if(containedInList($scope.relatedEntityResults.results.bindings[i], rowModel.selectedRelatedInstanceList, true).contained) {
	    							$scope.relatedEntityResults.results.bindings[i].isChecked = true;
	    						}
	    			    	}
	    					
	    					//if($scope.relatedEntityResults.results != undefined)
	    		    		//	$scope.totalItems = $scope.relatedEntityResults.results.bindings.length;
	    					
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
	        	// Execute query promise - End
	    		
    		}, function (error) {
    			$scope.message = 'There was a network error. Try again later.';
    			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
    				data : error
    			}));
    			modalInstance.close();
    		});
    		// Count query promise - End
    		
    	}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
			modalInstance.close();
		});
    	// Construct query promise - End
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
	
	// Used when entering a new chip
	$scope.transformChip = function(chip) {
		// If it is an object, it's already a known chip
		if (angular.isObject(chip)) {
			return chip;
		}

		// Otherwise, create a new one
		return {
			//id: 'new',
			name: chip
		}
	}
	
	$scope.applySearch = function() {
		$log.info(angular.toJson($scope.rowModelList));
	};
	
		
	
		
		
		
		
		
		
		
		
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
		
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
    				loadMapForRelatedEntity(rowModel);
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

	function loadMapForRelatedEntity(rowModel) {
		
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
				})
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
			//$scope.$apply();
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

		// Initializing
		var polyFeatures = new ol.Collection();	// Array to hold the polygons
    	var pointFeatures = new ol.Collection();	// Array to hold the points
		
    	var polyVectorLayer = new ol.layer.Vector();
    	var pointVectorLayer = new ol.layer.Vector();
    	var select = new ol.interaction.Select();
    	var popoverElement = document.getElementById('popup');
		var element = null; // The Popup Element
		
		// clear selection when drawing a new box and when clicking on the map
		$scope.dragBox.on('boxstart', function() {
			//$scope.infoBox.innerHTML = '&nbsp;';
			polyFeatures.clear();
			pointFeatures.clear();
			select.getFeatures().clear();
		});
		
		// Styling Pins
		
		// Unselected Pink Icon
    	var iconStylePinkUnselected = new ol.style.Style({
    		image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    			anchor: [0.3, 1],
    			offset: [22, 0],
    			size: [128, 128],
    			src: '../images/Map-Marker-Marker-Outside-Pink-icon.png',
    			scale: 0.3
    		}))
    	});
    	  
    	// Selected Pink Icon
    	var iconStylePinkSelected = new ol.style.Style({
    		image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    			anchor: [0.3, 1],
    			offset: [22, 0],
    			size: [128, 128],
    			src: '../images/Map-Marker-Marker-Inside-Pink-icon.png',
    			scale: 0.3
    		}))
    	});
    	  
    	// Unselected Green Icon
    	var iconStyleGreenUnselected = new ol.style.Style({
    		image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    			anchor: [0.3, 1],
    			offset: [22, 0],
    			size: [128, 128],
    			src: '../images/Map-Marker-Marker-Outside-Chartreuse-icon.png',
    			scale: 0.3
    		}))
    	});
    	  
    	// Selected Green Icon
    	var iconStyleGreenSelected = new ol.style.Style({
    		image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    			anchor: [0.3, 1],
    			offset: [22, 0],
    			size: [128, 128],
    			src: '../images/Map-Marker-Marker-Inside-Chartreuse-icon.png',
    			scale: 0.3
    		}))
    	});
    	  
    	// Unselected Blue Icon
    	var iconStyleBlueUnselected = new ol.style.Style({
    		image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    			anchor: [0.3, 1],
    			offset: [22, 0],
    			size: [128, 128],
    			src: '../images/Map-Marker-Marker-Outside-Azure-icon.png',
    			scale: 0.3
    		}))
    	});
    	  
    	// Selected Blue Icon
    	var iconStyleBlueSelected = new ol.style.Style({
    		image: new ol.style.Icon(/** @type {olx.style.IconOptions} */ ({
    			anchor: [0.3, 1],
    			offset: [22, 0],
    			size: [128, 128],
    			src: '../images/Map-Marker-Marker-Inside-Azure-icon.png',
    			scale: 0.3
    		}))
    	});
		
    	// Pop-up preparation			
		var popup = new ol.Overlay({
			element: popoverElement,
			positioning: 'bottom-center',
			stopEvent: true, // If false popover's click and wheel events won't work 
			offset: [0, -50]
		});
    	
		// Displaying on map
		function handleGeoResultsForMap(geoResults) {
	    	  
			if($scope.map.getLayers().getLength() > 1) {
		    		  
				// Removing with inverse order all layers apart from that one with index 0 (this is the map) 
				for (i = $scope.map.getLayers().getLength(); i > 0; i--) {
		    		$scope.map.removeLayer($scope.map.getLayers().item(i));
		    	}
			}
	    	  
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
		    	
	    		// Constructing the pointFeature hard-coded
    			// Point Feature (with marker icon)
    			// Since we have rectangular this point is the center of the polygon
    			/*
    			var pointFeature = new ol.Feature({
    				geometry: polyFeature.getGeometry().getInteriorPoint(),
    				featureType: 'marker',
    				name: '<a href="' + uriWrapper + '" target="_blank">' + nameWrapper + '</a>',
    				responsible: responsibleWrapper,
    				service: serviceWrapper
    			});
    			*/
    			
    			// Constructing the pointFeature dynamically
    			var pointFeature = new ol.Feature();
    			pointFeature.setGeometry(polyFeature.getGeometry().getInteriorPoint());
    			pointFeature.set('featureType', 'marker');
    			// Leaving it as it is in the original data and will 
    			// be changed when constructing the actual pop-up element
    			angular.forEach(geoResults[i], function (property, key) {
    				pointFeature.set(key, property);
    			});
    			    			
    			/*
    			// Constructing the pointFeature hard-coded
    			var pointFeature = new ol.Feature();
    			pointFeature.setGeometry(polyFeature.getGeometry().getInteriorPoint());
    			pointFeature.set('featureType', 'marker');
    			pointFeature.set('name', '<a href="' + uriWrapper + '" target="_blank">' + nameWrapper + '</a>');
    			pointFeature.set('responsible', responsibleWrapper);
    			pointFeature.set('service', serviceWrapper);
		        */ 
    			
    			// Change  coordinate systems to display on the map
    			polyFeature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
    			pointFeature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
	        	  
    			// Setting unselected style
    			//pointFeature.setStyle(iconStylePinkUnselected);
	        	          	  
    			pointFeatures.push(pointFeature); // Adding into Array
	        	          	          	  
    		} // loop ends
	    	
	    	// A vector layer holding the polygon features
			polyVectorLayer = new ol.layer.Vector({
				name: 'polyVectorLayer',
				source: new ol.source.Vector({
					features: polyFeatures
				})
			});
	    	  
			$scope.map.addLayer(polyVectorLayer);	// Adding Layer with all the polygons
	    	  
			// A vector layer holding the point features
			pointVectorLayer = new ol.layer.Vector({
				name: 'pointVectorLayer',
				source: new ol.source.Vector({
					features: pointFeatures
				}),
				style: iconStylePinkUnselected
			});
	    	  
			$scope.map.addLayer(pointVectorLayer);	// Adding Layer with all the points

			// Adding pop-up
			$scope.map.addOverlay(popup);
			
			// The pop-up Element
			element = popup.getElement();
			
			// Adding hovering
			
			var hoverInteraction = new ol.interaction.Select({
			    condition: ol.events.condition.pointerMove,
			    layers: [pointVectorLayer],  //Setting layers to be hovered
			    style: iconStyleGreenUnselected
			});
			$scope.map.addInteraction(hoverInteraction);
			
			// Adding select for pins
			select = new ol.interaction.Select({
				layers: [pointVectorLayer],
				style: iconStyleGreenSelected,
				toggleCondition: ol.events.condition.always
		    });
			$scope.map.addInteraction(select);
			
			// On Select
			select.on('select', function(evt) {
				
				var jsonItem = {};
				
				console.log('evt.selected: ');
				angular.forEach(evt.selected[0].getProperties(), function (property, key) {
					//console.log(key + ': ' + property.value);
					if(key != 'geometry' && key != 'featureType'&& key != 'east'&& key != 'west'&& key != 'north'&& key != 'south')
						jsonItem[key] = property
				});
				
				// Show related entity results panel on the respective rowModel
				if(rowModel.shownEntitySearchResults == false && rowModel.selectedRelatedInstanceList.length > 0) {
					rowModel.shownEntitySearchResults = true;
				}
				else if (rowModel.shownEntitySearchResults == true && rowModel.selectedRelatedInstanceList.length < 1) {
					rowModel.shownEntitySearchResults = false;
				}
				//console.log('jsonItem: ' + angular.toJson(jsonItem));
				rowModel.selectedRelatedInstanceList.push(jsonItem);
			  
			});
			
		}
		
		// On Hover
		
		// Used for changing cursor type
		var target = $scope.map.getTarget();
		var jTarget = typeof target === "string" ? $("#" + target) : $(target);
		
		// Used for not rendering it on every pixel when already displayed
		var isDisplayed = false;
		
		// Holding some history of the moves for reference
		var previousFeature = new ol.Feature(); // The previous feature hovered over the map (can be null)
		var oldFeature = new ol.Feature();		// The previous feature hovered over any pin (can never null)
		
		// On Hover
		$scope.map.on('pointermove', function(e) {
			
			// Changing mouse cursor when over pointFeature
			var pixel = $scope.map.getEventPixel(e.originalEvent);
			var hit = $scope.map.hasFeatureAtPixel(pixel);
			if (hit) {
				jTarget.css("cursor", "pointer");
			} else {
				jTarget.css("cursor", "");
			}
			
			// Only return marker Features (not polygons) or null
			var feature = $scope.map.forEachFeatureAtPixel(pixel, function(newFeature) {
				if (newFeature.get('featureType') == 'marker') {
					// Don't redisplay if the new feature 
					// is the same as the previous one
					// (hovering over the pixels of the same pin)
					if(previousFeature != null) {
						if(previousFeature.get('name') == newFeature.get('name'))
							isDisplayed = true;
						else
							isDisplayed = false;
					}
					// Don't redisplay if the new feature 
					// has the popup already open
					// (hovering over some pin then go out and then 
					// hover immediately over the same pin)
					else if(oldFeature != null) {
						if (oldFeature.get('name') == newFeature.get('name'))
							isDisplayed = true;
						else
							isDisplayed = false;
					}
					// Any other case
					else
						isDisplayed = false;
					
					return newFeature;
				}
				else {
					isDisplayed = true;
					return null;
				}
			});
			
			// Holding a copy of the feature to compare it with the new one in the future 
			// and decide whether to display the popup or not. The actual usage is in the 
			// cases where the pins are stick together with out gap. If there is gap, it 
			// is detected and the isDisplayed flag becomes false, but if it dosn't exist 
			// I'm using the comparison of the old & new features.
			//previousFeature = $.extend( {}, feature);
			//previousFeature = angular.copy(feature);
			previousFeature = feature;
			
			if(feature != null) { // Thus it is a marker
				
				oldFeature = feature;
				
				if(!isDisplayed) {
					
					var coordinates = feature.getGeometry().getCoordinates();
					popup.setPosition(coordinates);
					
					// This is how to get keys or properties
					// Keys is an array of strings
					// properties is a JSON element {key1: value1, key2: value2 ...}
					//console.log("feature.getKeys():");
					//console.log(feature.getKeys());
					//console.log("feature.getProperties():");
					//console.log(feature.getProperties());
					
					// Dynamically constructing the element from the featurePoint
					if($(element) != null) {
						$(element).attr('data-placement', 'top');
						$(element).attr('data-original-title', '<b>' + 'Info' + '</b>');
						$(element).attr('data-animation', true );
						$(element).attr('data-html', true);
						
						var htmlContent = '';
						var propIndex = 0; // geometry and featureType are the two first ones
						angular.forEach(feature.getProperties(), function (property, key) {
							if(key != 'geometry' && key != 'featureType' && key != 'east' && 
							   key != 'west' && key != 'north' && key != 'south') {
								if(property.type != 'uri') {
									if(propIndex == 2)
										htmlContent = htmlContent + '<a href=\"' + feature.getProperties().uri.value + '\" target="_blank">' + property.value + '</a>' + '<br/><br/>';
									else
										htmlContent = htmlContent + '<span style=\"text-decoration: underline;\">' + key + ':</span> <i>' + property.value + '</i><br/>';
								}
							}
							$(element).attr('data-content', htmlContent);
							propIndex++;
						});
						
						//$(element).attr('data-content', feature.get('name') + " by " + feature.get('Service') + "</br></br><span style=\"text-decoration: underline;\">Responsible:</span> <i>" + feature.get('Responsible') + "</i>");
						$(element).popover();
						$(element).popover('show');
					}
				}
			}
			
			else {
				//$(element).popover('destroy');
			}
			
		});
		
		// Clicking anywhere on the map 
		// Tthis is different than selecting a feature (used above)
		$scope.map.on('click', function(evt) {
			console.log(evt);
			
			// Determine whether anything but a pin is clicked
			
			var feature = $scope.map.forEachFeatureAtPixel(evt.pixel, function(feature) {
				return feature;
			});
			
			// When a rectangle is clicked then destroy popup
			if(feature != undefined) {
				if (feature.get('featureType') != 'marker') {
					if($(element) != null)
						$(element).popover('destroy');
				}
			}
			// When the map (not any pin) is clicked then destroy popup
			else {
				if($(element) != null)
					$(element).popover('destroy');
			}
			
		});
		

		// This is no longer needed - If I've changed the service
		$scope.retrieveGeoData = function() {
			/*
			var queryModel = {
				north : $scope.coordinatesRegion.north,
				south : $scope.coordinatesRegion.south,
				west : $scope.coordinatesRegion.west,
				east : $scope.coordinatesRegion.east,
				itemsPerPage : $scope.itemsPerPage
			};
			*/
			// Modal
	  		var modalOptions = {
  				headerText: 'Loading Please Wait...',
  				bodyText: 'Search process undergoing...'
  			};
			var modalInstance = modalService.showModal(modalDefaults, modalDefaultOptions);
			
			// Some dynamically defined preparation
			
			// The search text to feed the query
	    	var querySearchText = '';
	    	
	    	angular.forEach(rowModel.relatedChips, function(value, key) {
	    		querySearchText = querySearchText + ' ' + value.name;
	    	});
	    	
	    	if(rowModel.relatedEntitySearchText != null && rowModel.relatedEntitySearchText != '') {
	    		querySearchText = querySearchText + ' ' + rowModel.relatedEntitySearchText;
	    	}
	    	
			var searchEntityModel = {
				entity: rowModel.selectedRelatedEntity.name,
				geo_query: rowModel.selectedRelatedEntity.queryModel.geo_query,
				text_geo_query: rowModel.selectedRelatedEntity.queryModel.text_geo_query,
		    	geospatial: rowModel.selectedRelatedEntity.geospatial,
	    		searchText: querySearchText,
	    		fromSearch: $scope.queryFrom,
	    		north : $scope.coordinatesRegion.north,
	            south : $scope.coordinatesRegion.south,
	            west : $scope.coordinatesRegion.west,
	            east : $scope.coordinatesRegion.east
	    	}
			
			var updatedQueryModel = '';
			
			queryService.computeRelatedEntityQuery(searchEntityModel, $scope.credentials.token).then(function (queryResponse) {
				console.log('queryResponse:');
				console.log(queryResponse);
				updatedQueryModel = angular.copy(rowModel.selectedRelatedEntity.queryModel)
	        	updatedQueryModel.query = queryResponse.data.query;
				delete updatedQueryModel.geo_query;
	    		delete updatedQueryModel.text_geo_query;
				console.log('Geospatial Query:');
				console.log(updatedQueryModel);
				
	    		// Calling service to executing Query - Promise
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
    					// handling results
    					handleGeoResultsForMap(response.data.results.bindings);
    					//console.log(angular.toJson(response.data.results.bindings));
    					modalInstance.close();
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
	        	// Execute query promise - End
				
			},
			function (error) {
				$scope.message = 'There was a network error. Try again later.';
				alert("failure message: " + $scope.message + "\n" + JSON.stringify({
					data : error
				}));
				modalInstance.close();
			});
	  		
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