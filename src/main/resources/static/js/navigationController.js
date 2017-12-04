/**
 * The main angularJS controllers (handling tabs, the query submission and serverside paginator and the file importing
 * 
 * @author Vangelis Kritsotakis
 */
app.controller("navigationCtrl", ['$state', '$scope', '$timeout', '$parse', '$sessionStorage', 'authenticationService', 'modalService', 'queryService', '$mdSidenav', 'ivhTreeviewMgr', '$http', '$log', '$mdDialog', '$mdToast', '$q', 
                                  function($state, $scope, $timeout, $parse, $sessionStorage, authenticationService, modalService, queryService, $mdSidenav, ivhTreeviewMgr, $http, $log, $mdDialog, $mdToast, $q) {
	
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
	
	$scope.mainViewBackdrop = 'enabled-main-view-backdrop';
	
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
	
	// Used to inform user that error has occurred
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
	
	// Used to know that the treeMenu was opened and because the user opened the Info it was closed
	$scope.treeMenuWasOpen = false;
	
	// Toggles SidePanel
	$scope.toggleInfo = infoBuildToggler('rightInfo');
	function infoBuildToggler(componentId) {
		return function() {
			$mdSidenav(componentId).toggle();
			$scope.infoNavIsOpen = $mdSidenav(componentId).isOpen();
			
			if($scope.treeMenuIsOpen) {
				$scope.toggleTreeMenu();
				$scope.treeMenuWasOpen = true;
			}
			
			else if($scope.treeMenuWasOpen) {
				$scope.toggleTreeMenu();
				$scope.treeMenuWasOpen = false;
			}
			 
		};
    }
	
	$scope.toggleTreeMenu = treeMenuBuildToggler('treeMenu');
	function treeMenuBuildToggler(componentId) {
		return function() {
			$mdSidenav(componentId).toggle();
			$scope.treeMenuIsOpen = $mdSidenav(componentId).isOpen();
		};
    }
	
	/*
	$scope.hideTreeMenuTogglerButton = function(boolean) {
		$scope.hiddenTreeMenuTogglerButton = boolean;
	}
	*/
	/*
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
	*/
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
	
	function stringifyReplacerForMenuTree(key,value) {
	    if (key=="$$hashKey") return undefined;
	    else if (key=="__ivhTreeviewExpanded") return undefined;
	    else if (key=="__ivhTreeviewIndeterminate") return undefined;
	    else return value;
	}
	
	// Initializing All available entities
	function initAllNamegraphs(newCase) {
		
		queryService.getAllNamegraphs().then(function (response) {
			if(response.status == '200') {
				$scope.namegraphs = makeAllNamegraphsSelected(response.data);
				// Initializing the queryFrom string
				constructQueryForm(response.data);
				if(newCase) { // Not from favorites
					// Initial value for the flag
					$scope.treeMenuIsOpen = true; 
					// Open treeMenu
					$scope.toggleTreeMenu();
				}
				// Keep first copy
				$scope.namegraphsCopy = angular.copy($scope.namegraphs);
				
				// Initializing the available entities
				initAllEntities($scope.queryFrom, false);
			}
			else if(response.status == '400') {
				$log.info(response.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
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
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
				modalInstance.close();
			}			
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
		}).finally(function(){
			
		});
	}
			
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
	
	$scope.copyNamegraphs = function() {
		$scope.namegraphsCopy = angular.copy($scope.namegraphs);
	}
	
	// Clears the whole treeRowModel and loads new entities (on tree-menu hide)
	$scope.reLoadEntities = function(ev, queryFrom) {
		var messageContent = 'In order to complete this action, the whole query '
						   + 'constructed so far has to be reset.'
						   + '<br/>'
						   + 'That means that the query has to be re-constructed from scratch. ' 
						   + '<br/><br/>' 
						   + 'Are you sure you want to continue with this action?';
	
		//showConfirmDialogForUnavailableEntities(ev, messageContent, queryFrom, checkboxNode)
		var confirm = $mdDialog.confirm()
			.title('Important Message')
			.htmlContent(messageContent)
			.ariaLabel('Target Entity Selection - No longer Available')
			.targetEvent(ev)
			.ok('Yes Continue')
			.cancel('Cancel');
		
		// If target is selected and there are changes on the tree then ask for confirmation
		if($scope.targetModel.selectedTargetEntity == null) {
			// Keep Copy of namegraphs to compare next time
			$scope.namegraphsCopy = angular.copy($scope.namegraphs);
		}
		
		if(JSON.stringify($scope.namegraphs, stringifyReplacerForMenuTree) != JSON.stringify($scope.namegraphsCopy, stringifyReplacerForMenuTree)) {
			$mdDialog.show(confirm).then(function() {
		    	resetWholeQueryModel();			    	
		    }, function() { // Cancel
		    	$scope.namegraphs = angular.copy($scope.namegraphsCopy);
		    });
		}
		else { // No changes
	    	$scope.toggleTreeMenu();
		}
		
	}
	
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
	
	
	$scope.configuration = {
		wholeTreeModel: {
			
		},
		everyRowModel: {
			
		},
		relatedEntityMap: {
			maxResoultCountForShowingPinsOnInit: 200
		}
	}
	
	// To be used for constructing the simple related entity query dynamically
    $scope.relatedEntityQuerySearchText = '';
	
	// All entities used in the row model
	$scope.allEntities = [];
	
	// Target model
	$scope.targetModel = {
		selectedTargetEntity: null,
		backupSelectedTargetEntity: null,
		targetEntities: $scope.allEntities, //angular.copy($scope.allEntities);
		searchTargetKeywords: '',
		selectedTargetRecomentation: null,
		targetChips: []
	}
	
	var autoincrementedRowModelId = 0;
	
	// Initial empty row model
	$scope.initEmptyRowModel = {
		id: autoincrementedRowModelId,
		level: 0,
		outerSelectedFilterExpression: '',
		selectedRelation: null,
		backupSelectedRelation: null,
		relations: [],//angular.copy($scope.relations),
		rangeOfDates: {
			from: null,//new Date(),
			fromInputName: '',
			until: null,
			untilInputName: ''
		},
		selectedRelatedEntity: null,//{name: '', thesaurus: '', queryModel: ''},
		backupSelectedRelatedEntity: null,
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
		activeRelatedSearchResultsStyle: 'enabled-style',
		activeRowModelStyle: 'disabled-style'
	}
	
	$scope.thesaurus = {};
	
	// Uncheck All-Related-Search-Results option (when removing the single chip)
	$scope.unselectAllRelatedSearchResults = function(rowModel) {
		rowModel.allRelatedSearchResultsIsSelected = false;
		$scope.handleSelectAllRelatedSearchResults(rowModel);
	}
	
	$scope.currentFavorite = {
		itIsFavorite: false,
		dbTableId: null
	};
	
	this.$onInit = function () {
				
		//Initializing empty row model (new instance)
		$scope.emptyRowModel = angular.copy($scope.initEmptyRowModel);
		$scope.rowModelList = [$scope.emptyRowModel];
		
		// Initializing target and rowModelList
		// according to whether it is a "new" case or a "loading from favorites" case
		
		// Initializing model from favorite (if loaded)
		if($sessionStorage.selectedFavoriteModel != null) {
						
			$scope.rowModelList = $sessionStorage.selectedFavoriteModel.queryModel.relatedModels;
			$scope.targetModel = $sessionStorage.selectedFavoriteModel.queryModel.targetModel;
			$scope.namegraphs = $sessionStorage.selectedFavoriteModel.queryModel.namegraphs;
			// Keep first copy of namegraphs
			$scope.namegraphsCopy = angular.copy($scope.namegraphs);
			
			// Let system know that this is already favorite
			$scope.currentFavorite.itIsFavorite = true;
			$scope.currentFavorite.dbTableId = $sessionStorage.selectedFavoriteModel.favoriteId;
			// Making $sessionStorage.selectedQueryModel null, to free up memory
			$sessionStorage.selectedFavoriteModel = null;
		}
		// New Case
		else {
			initAllNamegraphs(true); // true stands for "new case"
		}
		
	}
		
	// Initializing All available entities
	function initAllEntities(queryFrom, notify) {
		
		var modalOptions = {
			headerText: 'Loading Please Wait...',
			bodyText: 'Initializing available options...'
		};
	
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
	
		queryService.getEntities(queryFrom, $scope.credentials.token).then(function (response) {
			if(response.status == '200') {
				if(response.data.remote_status == 200) {
					$scope.allEntities = response.data.entities;
					$scope.targetModel.targetEntities = response.data.entities;
					$scope.initEmptyRowModel.relatedEntities = response.data.entities;
					
					if(notify) {
						// Display msg
						$mdToast.show(
							$mdToast.simple()
					        .textContent('Query has been reset.')
					        .position('top right')
					        .parent(angular.element('#mainContent'))
					        .hideDelay(3000)
					    );
					}
				}
				else if(response.data.remote_status == 401){
					$log.info(response.data.remote_status);
					$scope.showLogoutAlert();
					authenticationService.clearCredentials();
				}
				else {
					$log.info(response.data.remote_status);
					$scope.message = 'There was an error with the remote server. Try again later and if the same error occures again please contact the administrator.';
					$scope.showErrorAlert('Error', $scope.message);
				}
			}
			else if(response.status == '400') {
				$log.info(response.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
			}
			else if(response.status == '401') {
				$log.info(response.status);
				$scope.showLogoutAlert();
				authenticationService.clearCredentials();
			}
			else {
				$log.info(response.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
			}
			modalInstance.close();
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			$scope.showErrorAlert('Error', $scope.message);
			modalInstance.close();
		}).finally(function(){
			//$scope.$apply();
		});
			
	}
		
	$scope.addNewEmptyRowModel = function(parentRowModel, str) {
		
		autoincrementedRowModelId++;
		
		if(parentRowModel == null) {
			$scope.rowModelList.push(angular.copy($scope.initEmptyRowModel));
			$scope.rowModelList[$scope.rowModelList.length-1].outerSelectedFilterExpression = str;
			$scope.rowModelList[$scope.rowModelList.length-1].id = autoincrementedRowModelId;

			// Loading related entities and relations for new related model 
			$scope.loadRelatedEntitiesAndRelationsByTarget('no-event', 'Not-Needed', undefined, $scope.targetModel.selectedTargetEntity, 'addFilter');
			
			// Enabling rowModel
			$scope.rowModelList[$scope.rowModelList.length-1].activeRowModelStyle = 'enabled-style';
		}
		else { //if(parentRowModel != null) {
			parentRowModel.rowModelList.push(angular.copy($scope.initEmptyRowModel));
			parentRowModel.rowModelList[parentRowModel.rowModelList.length-1].outerSelectedFilterExpression = str;
			parentRowModel.rowModelList[parentRowModel.rowModelList.length-1].id =  autoincrementedRowModelId;
			
			// Loading related entities and relations for new related model
			$scope.loadRelatedEntitiesAndRelationsByTarget('no-event', 'Not-Needed', parentRowModel, parentRowModel.selectedRelatedEntity, 'addFilter');
			
			// Enabling rowModel
			parentRowModel.rowModelList[parentRowModel.rowModelList.length-1].activeRowModelStyle = 'enabled-style';
			
			// Increment level by one considering the parent's level
			parentRowModel.rowModelList[parentRowModel.rowModelList.length-1].level = parentRowModel.level + 1;
		}
		
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
	
	$scope.loadRelatedEntitiesByRelation = function(parentRowModel, rowModel) {

		if(rowModel.selectedRelation != null && rowModel.selectedRelation != undefined) {
			if(rowModel.selectedRelation.relatedEntity != null && rowModel.selectedRelation.relatedEntity != undefined) {
				//console.log('selectedRelation.relatedEntity' + angular.toJson(rowModel.selectedRelation.relatedEntity));
				angular.forEach(rowModel.relatedEntities, function(relatedEntity, key) {
					if(relatedEntity.uri == rowModel.selectedRelation.relatedEntity.uri) {
						rowModel.selectedRelatedEntity = relatedEntity;
						
						if(parentRowModel == undefined) {
							paramModelForRelations = {
								fromSearch: $scope.queryFrom, 									// the collections (VREs) String
								targetEntity: $scope.targetModel.selectedTargetEntity.name,		// The selected entity name (target)
								relatedEntity: rowModel.selectedRelatedEntity.name								// The selected entity name (related entity)
							}
						}

						//Case - Target is related entity
						else {
							paramModelForRelations = {
								fromSearch: $scope.queryFrom, 									// the collections (VREs) String
								targetEntity: parentRowModel.selectedRelatedEntity.name,		// The selected entity name (target)
								relatedEntity: rowModel.selectedRelatedEntity.name								// The selected entity name (related entity)
							}
						}
						
						// Updating the list of relations based on the user selected target and the auto-completed related entities
						handleRelationsByTargetAndRelatedEntities(rowModel, paramModelForRelations, $scope.credentials.token);
						
					}
				});
			}
		}
		
	}
	
	// Loading the list of relations and related entities 
	// based on the selected entity
	// param: parentRowModel		the parentRowModel (can be null or undefined)
	// param: rowModel				the currentRowModel (can be undefined)
	// param: selectedEntity		the selected Entity (acting as target)
	// param: provenanceFunction	A string denoting provenance ('addFilter', 'levelDown', 'relatedEntitySelect', 'targetEntitySelect')
	// (In case of selecting real target entity, the parent rowModel is null, 
	// the rowModel is also undefined and the selecteEntity is the selectedTargetEntity)
	$scope.loadRelatedEntitiesAndRelationsByTarget = function(ev, parentRowModel, rowModel, selectedEntity, provenanceFunction) {
				
		if(selectedEntity !=null) { 
			
			// Rrelated Entity List handling
			
			// Parameters to sent for the Relations And Related Entities Service (the same for all cases)
			var paramModelForRelationsAndRelatedEntities = {
				fromSearch: $scope.queryFrom, 				// the collections (VREs) String
				name: selectedEntity.name,					// The selected entity name
				entities: $scope.targetModel.targetEntities	// The list of all entities (target always has them all)
			}
						
			// Case where entity selection is from target
			if(rowModel == undefined) {
				// When adding new filter
				if(provenanceFunction == 'addFilter') {
					if($scope.rowModelList.length >0) {
						handleRelationsAndRelatedEntitiesByTarget($scope.rowModelList[$scope.rowModelList.length-1], 
								paramModelForRelationsAndRelatedEntities, $scope.credentials.token);
					}					
				}
				
				// When selecting target entity
				else if(provenanceFunction == 'targetEntitySelect') {
					var isEditAction = false;
					// For each related entity - level 1
					for(var i=0; i<$scope.rowModelList.length; i++) {
						if($scope.rowModelList[i].selectedRelatedEntity != null || $scope.rowModelList[i].selectedRelation != null)
							isEditAction = true;
						break;
					}
					
					// If this is not the initial selection (user is editing)
					if(isEditAction) {
					
						var messageContent = 'In order to complete this action, the query in terms of related options has to be reset.'
							   + '<br/>'
							   + 'That means that this part of the query has to be re-constructed from scratch.' 
							   + '<br/><br/>' 
							   + 'Are you sure you want to continue with this action?';
						
						var confirm = $mdDialog.confirm()
							.title('Important Message')
							.htmlContent(messageContent)
							.ariaLabel('Target Entity Selection - No longer Available')
							.targetEvent(ev)
							.ok('Yes Continue')
							.cancel('Cancel');
					
						$mdDialog.show(confirm).then(function() { // OK
							
							for(var i=0; i<$scope.rowModelList.length; i++) {
								handleRelationsAndRelatedEntitiesByTarget($scope.rowModelList[i], 
										paramModelForRelationsAndRelatedEntities, $scope.credentials.token);
							}
							// Holding Copy Of Selected Target Entity
							$scope.targetModel.backupSelectedTargetEntity = angular.copy($scope.targetModel.selectedTargetEntity);
							
							// Display msg
							$mdToast.show(
								$mdToast.simple()
						        .textContent('Query in terms or related options has been reset.')
						        .position('top right')
						        .parent(angular.element('#mainContent'))
						        .hideDelay(3000)
						    );
							
						}, function() { // Cancel
							// Load previous selection (the backup)
							for(var i=0; i<$scope.targetModel.targetEntities.length; i++) {
								if($scope.targetModel.targetEntities[i].uri == $scope.targetModel.backupSelectedTargetEntity.uri)
									$scope.targetModel.selectedTargetEntity = $scope.targetModel.targetEntities[i];
							}
						});
						/*
						.finally(function() {
							console.log($scope.rowModelList[0].selectedRelatedEntity.name);
						});
						*/
					}
					else {
						for(var i=0; i<$scope.rowModelList.length; i++) {
							handleRelationsAndRelatedEntitiesByTarget($scope.rowModelList[i], 
									paramModelForRelationsAndRelatedEntities, $scope.credentials.token);
						}
						$scope.targetModel.backupSelectedTargetEntity = angular.copy($scope.targetModel.selectedTargetEntity);
					}
					
				} //if(provenanceFunction == 'targetEntitySelect') - Ends
				
				else { // provenanceFunction is: levelDown
					for(var i=0; i<$scope.rowModelList.length; i++) {
						handleRelationsAndRelatedEntitiesByTarget($scope.rowModelList[i], 
								paramModelForRelationsAndRelatedEntities, $scope.credentials.token);
					}
				}
				// Enabling rowModel
				$scope.rowModelList[$scope.rowModelList.length-1].activeRowModelStyle = 'enabled-style';
			}
			
			// Case where entity selection is from the related entity 
			else {
				
				var isEditAction = false; // Used to catch edit action
				
				// Relation parameters - Start
				
				// Parameters to sent for the Relations Service (considering both target and related entity)
				var paramModelForRelations = {}; // Initializing
				
				// There is no parent
				if(parentRowModel == undefined) {
					paramModelForRelations = {
						fromSearch: $scope.queryFrom, 									// the collections (VREs) String
						targetEntity: $scope.targetModel.selectedTargetEntity.name,		// The selected entity name (target)
						relatedEntity: selectedEntity.name								// The selected entity name (related entity)
					}
				}
				// Case - Level Down
				else if(provenanceFunction == 'levelDown') {
					//Do nothing
					console.log('Level Downn');
				}
				else if(provenanceFunction == 'addFilter') {
					//Do nothing
					console.log('addFilter');
				}
				//Case - Target is related entity
				else {
					paramModelForRelations = {
						fromSearch: $scope.queryFrom, 									// the collections (VREs) String
						targetEntity: parentRowModel.selectedRelatedEntity.name,		// The selected entity name (target)
						relatedEntity: selectedEntity.name								// The selected entity name (related entity)
					}
				}
				
				// Relation Parameters - Ends
				
				// When adding new filter
				if(provenanceFunction == 'addFilter') {
					if($scope.rowModelList.length >0) {
						handleRelationsAndRelatedEntitiesByTarget(rowModel.rowModelList[rowModel.rowModelList.length-1], 
								paramModelForRelationsAndRelatedEntities, $scope.credentials.token);
					}
				}
				
				// When selecting related entity
				else if(provenanceFunction == 'relatedEntitySelect') {
					
					// For each related entity - level 1
					for(var i=0; i<rowModel.rowModelList.length; i++) {
						if(rowModel.rowModelList[i].selectedRelatedEntity != null || rowModel.rowModelList[i].selectedRelation != null)
							isEditAction = true;
						break;
					}
					
					// If this is not the initial selection (user is editing)
					if(isEditAction) {
					
						var messageContent = 'In order to complete this action, the query after the point of '
							   + 'your change has to be reset.'
							   + '<br/>'
							   + 'That means that this part of the query has to be re-constructed from scratch. ' 
							   + '<br/><br/>' 
							   + 'Are you sure you want to continue with this action?';
						
						var confirm = $mdDialog.confirm()
							.title('Important Message')
							.htmlContent(messageContent)
							.ariaLabel('Related Entity Selection - No longer Available')
							.targetEvent(ev)
							.ok('Yes Continue')
							.cancel('Cancel');
					
						$mdDialog.show(confirm).then(function() { // OK
							
							confirmToContinue = true;
							for(var i=0; i<rowModel.rowModelList.length; i++) {
								handleRelationsAndRelatedEntitiesByTarget(rowModel.rowModelList[i], 
										paramModelForRelationsAndRelatedEntities,$scope.credentials.token);
							}
							
							// Holding Copy Of Selected Related Entity
							rowModel.backupSelectedRelatedEntity = angular.copy(rowModel.selectedRelatedEntity);
							// Relation List Handling (Applied after handling the relatedEntity)
							handleRelationsByTargetAndRelatedEntities(rowModel, paramModelForRelations, $scope.credentials.token);
							
							// Display msg
							$mdToast.show(
								$mdToast.simple()
						        .textContent('Query has been partly reset.')
						        .position('top right')
						        .parent(angular.element('#mainContent'))
						        .hideDelay(3000)
						    );
							
						}, function() { // Cancel
							confirmToContinue = false;
							// Load previous selection (the backup) - Selected Related Entity
							for(var i=0; i<rowModel.relatedEntities.length; i++) {
								if(rowModel.relatedEntities[i].uri == rowModel.backupSelectedRelatedEntity.uri)
									rowModel.selectedRelatedEntity = rowModel.relatedEntities[i];
							}
							
						});
					}
					
					else { //if(!isEditAction)
						// Relation List Handling (Applied after handling the relatedEntity)
						handleRelationsByTargetAndRelatedEntities(rowModel, paramModelForRelations, $scope.credentials.token);
					}
				} //if(provenanceFunction == 'relatedEntitySelect') - Ends
				
				else { // provenanceFunction is: levelDown
					for(var i=0; i<rowModel.rowModelList.length; i++) {
						handleRelationsAndRelatedEntitiesByTarget(rowModel.rowModelList[i], 
								paramModelForRelationsAndRelatedEntities, $scope.credentials.token);
					}
					// Holding Copy Of Selected Related Entity
					rowModel.backupSelectedRelatedEntity = angular.copy(rowModel.selectedRelatedEntity);
				}
				
			} // Close - else (selection from related entity
			
		} // If close - (selectedTargetEntity not null)
				
	}
	
	// Handling Relations and Related Entity By Target
	function handleRelationsAndRelatedEntitiesByTarget(rowModel, paramModel, token) {
		
		/*
		// Modal here is very desturbing
		var modalOptions = {
			headerText: 'Loading Please Wait...',
			bodyText: 'Updating available options...'
		};
		
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
		*/
		queryService.getRelationsAndRelatedEntitiesByTarget(paramModel, token)
		.then(function (response) {
    		
			if(response.status == -1) {
				$scope.message = 'There was a network error. Try again later.';
				$scope.showErrorAlert('Error', $scope.message);
				//modalInstance.close();
			}
			else {
				if(response.status == '200') {
					//$log.info(angular.toJson(response.data));
					// Response is formed like this:
					// 	[{
					// 		relation: {relation_uri: "SOME_URI", related_entity_name: "SOME_ENTITY_NAME"}
					//		related_entity: { ... }
					//	}, ... ]

					// Constructing relation List and related entity list
					rowModel.relatedEntities = [];
					
					// Handling Children when selecting an entity (if there are any)
					if(rowModel.selectedRelatedEntity != null) // Setting field touched otherwise it wont return error
						$scope.searchForm['relatedEntityInput_' + rowModel.id].$setTouched();
					
					rowModel.selectedRelatedEntity = null;
					
					rowModel.rowModelList = [];
					//$scope.searchForm['relatedEntityInput_' + rowModel.id].$setValidity('required', false);
					rowModel.relations = [];
					rowModel.selectedRelation = null;
					for(var i=0; i<response.data.length; i++) {
						//Check for duplicates in the list of related entities
						// Pure compare
						if (!containedInList(response.data[i].related_entity, rowModel.relatedEntities, false).contained)
							rowModel.relatedEntities.push(response.data[i].related_entity);
						//relations
						rowModel.relations.push(response.data[i].relation);
						rowModel.relations[i].relatedEntity = response.data[i].related_entity;
						//$log.info('value: ' + value);
					}
					//modalInstance.close();
				}
				else if(response.status == '400') {
    				$log.info(response.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    				//modalInstance.close();
    			}
    			else if(response.status == '401') {
    				$log.info(response.status);
    				//modalInstance.close();
    				$scope.showLogoutAlert();
    				authenticationService.clearCredentials();
    			}
    			else {
    				$log.info(response.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    				//modalInstance.close();
    			}
			
			} // else close
		
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
			//modalInstance.close();
		});
	}
	
	/*
	// Recursive method that makes all selected related entities in a tree rowmodel null
	function makeAllChildrenSelectedRelatedEntitiesNull(rowModel) {
		rowModel.selectedRelatedEntity = null;
		// For all children
		for(var i=0; i<rowModel.rowModelList.length; i++) {
			rowModel.rowModelList[i].selectedRelatedEntity = null;
			// Recursively
			makeAllChildrenSelectedRelatedEntitiesNull(rowModel.rowModelList[i]);
		}
	}
	*/
	
	// Handling relations based on target and related entities
	function handleRelationsByTargetAndRelatedEntities(rowModel, paramModel, token) {
		queryService.getRelationsByTargetAndRelatedEntity(paramModel, token)
		.then(function (response) {
    		
			if(response.status == -1) {
				$scope.message = 'There was a network error. Try again later.';
				$scope.showErrorAlert('Error', $scope.message);
			}
			else {
				if(response.status == '200') {
					// Response is formed like this:
					// [{name:"RELATION_NAME",uri:"SOME_URI"}, ... ]
					
					// Constructing relation List and related entity list
					rowModel.relations = [];
					for(var i=0; i<response.data.length; i++) {
						rowModel.relations.push(response.data[i]);
					}
					
					// Re-setting selected relation if still available
					if(rowModel.selectedRelation != null) {
						var found = false;
						for(var i=0; i<rowModel.relations.length; i++) {
							if(rowModel.relations[i].uri == rowModel.selectedRelation.uri) {
								rowModel.selectedRelation = rowModel.relations[i];
								found = true;
							}
						}
						if(found == false) {
							rowModel.backupSelectedRelation = angular.copy(rowModel.selectedRelation);
							rowModel.selectedRelation = null;
						}
					}
					if(rowModel.backupSelectedRelation != null)
					console.log('rowModel.backupSelectedRelation.name: ' + rowModel.backupSelectedRelation.name)
					
				}
				else if(response.status == '400') {
    				$log.info(response.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    			}
    			else if(response.status == '401') {
    				$log.info(response.status);
    				$scope.showLogoutAlert();
    				authenticationService.clearCredentials();
    			}
    			else {
    				$log.info(response.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    			}
			
			} // else close
		
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
		});
	}
	
	// Adding filter on related entity
	$scope.addFilterOnRelated = function(parentRowModel, rowModel) {
		autoincrementedRowModelId++;
		rowModel.rowModelList.push(angular.copy($scope.initEmptyRowModel));
		//rowModel.rowModelList[outerIndex].activeStyle = 'enabledStyle';
		rowModel.rowModelList[rowModel.rowModelList.length-1].id = autoincrementedRowModelId;
		// Increment level by one considering the parent's level
		rowModel.rowModelList[rowModel.rowModelList.length-1].level = rowModel.level + 1;
		// Enabling rowModel
		rowModel.rowModelList[rowModel.rowModelList.length-1].activeRowModelStyle = 'enabled-style';
		
		// Loading related entities and relations for new related model
		$scope.loadRelatedEntitiesAndRelationsByTarget('no-event', 'Not-Needed', rowModel, rowModel.selectedRelatedEntity, 'levelDown');
		
		// Enabling rowModel
		$scope.rowModelList[$scope.rowModelList.length-1].activeRowModelStyle = 'enabled-style';
		
		// Debugging:
		var model = {
			queryFrom: $scope.queryFrom,
			rowModel: angular.copy(rowModel),
			queryModel: {
				targetModel: angular.copy($scope.targetModel),
				relatedModels: angular.copy($scope.rowModelList)
			}
		}
		
		// Delete Useless for the back-end properties, occupying a lot of volume
		
		// ----------- model.queryModel:
		
		// Target
		delete model.queryModel.targetModel.backupSelectedTargetEntity;
		delete model.queryModel.targetModel.targetEntities;
		
		// Related Entity List (whole (model.queryModel))
		for(var i=0; i<model.queryModel.relatedModels.length; i++) {
			deleteUselessForBackEndRelatedProperties(model.queryModel.relatedModels[i])
		}
		
		// ----------- model.rowModel"
		
		// Related Entity List (just the current rowModel))
		delete model.rowModel.backupSelectedRelatedEntity;
		delete model.rowModel.backupSelectedRelation;
		delete model.rowModel.relatedEntities;
		delete model.rowModel.relations;
		// Delete for children recursively
		deleteUselessForBackEndRelatedProperties(model.rowModel)
		
		console.log(angular.toJson(model));
	}
	
	// Recursive Method used for development only (so far), which deletes all these properties that
	// occupy a big volume in the rowModelList
	function deleteUselessForBackEndRelatedProperties(rowModel) {
		delete rowModel.backupSelectedRelatedEntity;
		delete rowModel.backupSelectedRelation;
		delete rowModel.relatedEntities;
		delete rowModel.relations;
		
		// For all children
		for(var i=0; i<rowModel.rowModelList.length; i++) {
			// Recursively
			deleteUselessForBackEndRelatedProperties(rowModel.rowModelList[i]);
		}
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
		if(entity != null) {
			if(entity.thesaurus != "" && entity.thesaurus != null) {
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
		} // Closing - if(entity != null)
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
		url: 'http://139.91.183.97:8080/EVREMetadataServices-1.0-SNAPSHOT',
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
    	$scope.updatedQueryModelPerPage = '';
    	
    	queryService.computeRelatedEntityQuery(searchEntityModel, $scope.credentials.token).then(function (queryResponse) {
    		if(queryResponse.status == '200') {
    			updatedQueryModel = angular.copy(rowModel.selectedRelatedEntity.queryModel)
            	updatedQueryModel.query = queryResponse.data.query;
        		delete updatedQueryModel.geo_query;
        		delete updatedQueryModel.text_geo_query;
        		
        		// Calling Service to get the count wrt to the query - Promise
        		queryService.getEntityQueryResultsCount($scope.serviceModel, updatedQueryModel, $scope.credentials.token)
        		.then(function (queryCountResponse) {
        			if(queryCountResponse.status == '200') {
        				// Holding total number of results
            			$scope.relatedEntityResultsCount = queryCountResponse.data.results.bindings[0].count.value;
            			console.log('$scope.relatedEntityResultsCount: ' + $scope.relatedEntityResultsCount);
            			
            			// Change query such that only the first 10 are returned
            			$scope.updatedQueryModelPerPage = Object.assign(updatedQueryModel);

            			var queryPerPage = "";//angular.copy($scope.updatedQueryModelPerPage.query);
            			queryPerPage = angular.copy($scope.updatedQueryModelPerPage.query) + ' limit ' + $scope.itemsPerPage.toString() + ' offset ' + ($scope.currentPage-1).toString();
            			$scope.updatedQueryModelPerPage.query = queryPerPage;
            			
            			// Keeping backup of query for easy offset change when pressing the next page
    			    	//$scope.updatedQueryModelPerPageBackup = angular.copy($scope.updatedQueryModelPerPage);
    			    	//$scope.updatedQueryModelPerPageBackup.query.replace("offset 0","offset !@#NUM#@!");
            			
        	    		// Calling service to executing Query - Promise
        	    		queryService.getEntityQueryResults($scope.serviceModel, $scope.updatedQueryModelPerPage, $scope.credentials.token)
        	    		.then(function (response) {
        	        		
        	    			if(response.status == -1) {
        	    				$scope.message = 'There was a network error. Try again later.';
        	    				$scope.showErrorAlert('Error', $scope.message);
        	    				modalInstance.close();
        	    			}
        	    			
        	    			else {
        	    				if(response.status == '200') {
        	    					
        	    					$scope.relatedEntityResults = response.data;
        	    					
        	    					// Iterating response that doesn't have 'isChecked' element
        	    					for(var i=0; i<response.data.results.bindings.length; i++) { // Iterating response that doesn't have 'isChecked' element
        	    						if(containedInList($scope.relatedEntityResults.results.bindings[i], rowModel.selectedRelatedInstanceList, true).contained) {
        	    							$scope.relatedEntityResults.results.bindings[i].isChecked = true;
        	    						}
        	    			    	}
        	    					
        	    					modalInstance.close();
        	    					
        	    					// Used for capturing the current row and thus knowing where to put selected items
        	    			    	$scope.currRowModel = rowModel;
        	    			    	$mdDialog.show({
        	    			    		scope: $scope,
        	    			    		templateUrl: 'views/dialog/selectFromResults.tmpl.html', 
        	    			    		parent: angular.element(document.body),
        	    			    		targetEvent: ev,
        	    			    		preserveScope: true,
        	    			    		fullscreen: false // Only for -xs, -sm breakpoints.
        	    			    	});
        	    				}
        	    				else if(response.status == '400') {
        	        				$log.info(response.status);
        	        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
        	        				$scope.showErrorAlert('Error', $scope.message);
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
        	        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
        	        				$scope.showErrorAlert('Error', $scope.message);
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
    				}
        			else if(queryCountResponse.status == '400') {
        				$log.info(queryCountResponse.status);
        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
        				$scope.showErrorAlert('Error', $scope.message);
        				modalInstance.close();
        			}
        			else if(queryCountResponse.status == '401') {
        				$log.info(queryCountResponse.status);
        				modalInstance.close();
        				$scope.showLogoutAlert();
        				authenticationService.clearCredentials();
        			}
        			else {
        				$log.info(queryCountResponse.status);
        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
        				$scope.showErrorAlert('Error', $scope.message);
        				modalInstance.close();
        			}
        		}, function (error) {
        			$scope.message = 'There was a network error. Try again later.';
        			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
        				data : error
        			}));
        			modalInstance.close();
        		});
        		// Count query promise - End
			}
			else if(queryResponse.status == '400') {
				$log.info(queryResponse.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
				modalInstance.close();
			}
			else if(queryResponse.status == '401') {
				$log.info(queryResponse.status);
				modalInstance.close();
				$scope.showLogoutAlert();
				authenticationService.clearCredentials();
			}
			else {
				$log.info(queryResponse.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
				modalInstance.close();
			}
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
	
	// Server-Side Pagination for query
	function getData(rowModel) {
		var pageParams = {
			page: $scope.currentPage, 
			itemsPerPage: $scope.itemsPerPage,
		}
		
		// Preparing new query with offset according to the page number clicked
		var subStrIndex = JSON.stringify($scope.updatedQueryModelPerPage.query).indexOf('offset');
		var subString = JSON.stringify($scope.updatedQueryModelPerPage.query).substr(subStrIndex);
		subString = subString.slice(0, -1);
		console.log(subString);
		
		$scope.updatedQueryModelPerPage.query = JSON.stringify($scope.updatedQueryModelPerPage.query).replace(subString, 'offset ' + (pageParams.page-1) * pageParams.itemsPerPage);
		$scope.updatedQueryModelPerPage.query = angular.fromJson($scope.updatedQueryModelPerPage.query);
		console.log($scope.updatedQueryModelPerPage.query);
		
		queryService.getEntityQueryResults($scope.serviceModel, $scope.updatedQueryModelPerPage, $scope.credentials.token)
		.then(function (response) {
    		
			if(response.status == -1) {
				$scope.message = 'There was a network error. Try again later.';
				$scope.showErrorAlert('Error', $scope.message);
			}
			
			else {
				if(response.status == '200') {
					
					$scope.relatedEntityResults = response.data;
					
					// Iterating response that doesn't have 'isChecked' element
					for(var i=0; i<response.data.results.bindings.length; i++) { // Iterating response that doesn't have 'isChecked' element
						if(containedInList($scope.relatedEntityResults.results.bindings[i], rowModel.selectedRelatedInstanceList, true).contained) {
							$scope.relatedEntityResults.results.bindings[i].isChecked = true;
						}
			    	}					
				}
				else if(response.status == '400') {
    				$log.info(response.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    			}
    			else if(response.status == '401') {
    				$log.info(response.status);
    				$scope.showLogoutAlert();
    				authenticationService.clearCredentials();
    			}
    			else {
    				$log.info(response.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    			}
			
			} // else close
			
			//modalInstance.close();
		
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
			//modalInstance.close();
		});
	}
	
	$scope.pageChanged = function() {
		getData($scope.currRowModel);
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
	
	// Determines if an item is contained into a list
	function containedInListBasedOnURI(item, list, uriPath) {
		
		var containedElement = {'contained': false, 'index': -1};
		
		for(var i=0; i<list.length; i++) {
						
		    if(item[uriPath].value === list[i][uriPath].value) {
		    	containedElement.contained = true;
		    	containedElement.index = i;
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
	
	
	
	
	// Open dialog for saving into favorites
	$scope.showFavoriteDialog = function(ev) {
		$mdDialog.show({
			scope: $scope,
    		templateUrl: 'views/dialog/favoriteForm.tmpl.html', 
    		parent: angular.element(document.body),
    		targetEvent: ev,
    		clickOutsideToClose:true,
    		preserveScope: true,
    		fullscreen: false // Only for -xs, -sm breakpoints.
    	});
	}
	
	// Model to hold current favorite
	$scope.favoriteModel = {
		username: null,
		title: '',
		description: '',
		queryModel: {
			targetModel: null,
			relatedModels: null,
			namegraphs: []
		}
	};
	
	// Save into favorites
	$scope.saveIntoFavorites = function() {
		
		// Updating model holding current favorite
		$scope.favoriteModel.username = $sessionStorage.userProfile.userId;
		$scope.favoriteModel.queryModel.targetModel = $scope.targetModel;
		$scope.favoriteModel.queryModel.relatedModels = $scope.rowModelList;
		$scope.favoriteModel.queryModel.namegraphs = $scope.namegraphs;
				
		//queryService.saveIntoFavorites(JSON.stringify($scope.favoriteModel), $scope.credentials.token)
		queryService.saveIntoFavorites(angular.toJson($scope.favoriteModel), $scope.credentials.token)
			.then(function (response) {
    		
			if(response.status == '200') {
				if(response.data.dbStatus == 'success') {
					// Hide Dialog
					$mdDialog.cancel();
					// Display msg
					$mdToast.show(
						$mdToast.simple()
				        .textContent('The Query has been stored successfully!')
				        .position('top right')
				        .parent(angular.element('#mainContent'))
				        .hideDelay(3000)
				    );
					$scope.currentFavorite.itIsFavorite = true;
					$scope.currentFavorite.dbTableId = response.data.generatedId;
				}
				else {
					$scope.message = 'I\'m sorry! The query was able to be stored. Try again later and if the same error occures, please contact with the administrator.';
					$scope.showErrorAlert('Error', $scope.message);
				}
			}
			else if(response.status == '400') {
				$log.info(response.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
			}
			else if(response.status == '401') {
				$log.info(response.status);
				$scope.showLogoutAlert();
				authenticationService.clearCredentials();
			}
			else {
				$log.info(response.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
			}
			
		}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
		});
		
	}
	
	// Hide Favorite Dialog
	$scope.closeFavoriteDialog = function() {
		$mdDialog.cancel();
	}
	
	// Removing the current queryModel from the database (uses the currentFavorite.dbTableId)
	$scope.removeCurrentQueryModelFromFavorites = function(ev) {
		if($scope.currentFavorite.dbTableId != null) {
			
			// Ask before removing from favorites
			var confirm = $mdDialog.confirm()
				.title('Warning Message')
				.htmlContent('Are you sure you want to remove this query from your favorites?')
				.ariaLabel('Confirmation')
				.targetEvent(ev)
				.ok('Yes Proceed')
				.cancel('Cancel');
			
			$mdDialog.show(confirm).then(function() { // OK
				queryService.removeFromFavoritesById($scope.currentFavorite, $scope.credentials.token)
				.then(function (response) {
					if(response.status == '200') {
						if(response.data.dbStatus == 'success') {
							// Display msg
							$mdToast.show(
								$mdToast.simple()
						        .textContent('The Query has been removed from your favorites!')
						        .position('top right')
						        .parent(angular.element('#mainContent'))
						        .hideDelay(3000)
						    );
							$scope.currentFavorite.itIsFavorite = false;
							$scope.currentFavorite.dbTableId = null;
						}
						else {
							$scope.message = 'I\'m sorry! The query was able to be stored. Try again later and if the same error occures, please contact with the administrator.';
							$scope.showErrorAlert('Error', $scope.message);
						}
					}
					else if(response.status == '400') {
						$log.info(response.status);
						$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
						$scope.showErrorAlert('Error', $scope.message);
					}
					else if(response.status == '401') {
						$log.info(response.status);
						$scope.showLogoutAlert();
						authenticationService.clearCredentials();
					}
					else {
						$log.info(response.status);
						$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
						$scope.showErrorAlert('Error', $scope.message);
					}
					
				}, function (error) {
					$scope.message = 'There was a network error. Try again later.';
					alert("failure message: " + $scope.message + "\n" + JSON.stringify({
						data : error
					}));
				});
		    	
		    }, function() { // Cancel
		    	//
		    });
			
			
		}
	}
	
	$scope.applyReset = function(ev) {
		var confirm = $mdDialog.confirm()
			.title('Warning Message')
			.htmlContent('Are you sure you want to reset the query constructed so far?')
			.ariaLabel('Target Entity Selection - No longer Available')
			.targetEvent(ev)
			.ok('Yes Proceed')
			.cancel('Cancel');
		
		$mdDialog.show(confirm).then(function() { // OK
			resetWholeQueryModel();			
	    }, function() { // Cancel
	    	// Do nothing
	    });
	}
	
	// Resets the whole query model
	function resetWholeQueryModel() {
		//Initializing empty row model (new instance)
		$scope.emptyRowModel = angular.copy($scope.initEmptyRowModel);
		$scope.rowModelList = [$scope.emptyRowModel];
		
		// Resetting target model
		$scope.targetModel.selectedTargetEntity = null;
		$scope.targetModel.backupSelectedTargetEntity = null;
		$scope.targetModel.targetEntities = $scope.allEntities;
		$scope.targetModel.searchTargetKeywords = '';
		$scope.targetModel.selectedTargetRecomentation = null;
		$scope.targetModel.targetChips = [];
		
		// Handling favorites
		if($sessionStorage.selectedFavoriteModel != null) {
			// Resetting favorite related options
			$scope.currentFavorite.itIsFavorite = false;
			$scope.currentFavorite.dbTableId = $sessionStorage.selectedFavoriteModel.favoriteId;
			// Making $sessionStorage.selectedQueryModel null, to free up memory
			$sessionStorage.selectedFavoriteModel = null;
		}
		//console.log($scope.rowModelList);
		// Setting select inputs untouched
		//$scope.searchForm.$setPristine();
		$scope.searchForm['targetEntityInput'].$setUntouched();
		var rowModelId = $scope.rowModelList[0].id;
		$scope.searchForm['relatedEntityInput_' + rowModelId].$setUntouched();
		$scope.searchForm['relationInput_' + rowModelId].$setUntouched();
		
		// Open tree menu again
		
		// Initial value for the flag
		$scope.treeMenuIsOpen = true; 
		// Open treeMenu
		$scope.toggleTreeMenu();
		// Keep first copy
		$scope.namegraphsCopy = angular.copy($scope.namegraphs);
	}
	
	$scope.applySearch = function() {
		// Development purpose
		var model = {
			queryFrom: $scope.queryFrom,
			queryModel: {
				targetModel: $scope.targetModel,
				relatedModels: $scope.rowModelList
			}
		}
		
		// Delete Useless for the back-end properties, occupying a lot of volume
		
		// ----------- model.queryModel:
		
		// Target
		delete model.queryModel.targetModel.backupSelectedTargetEntity;
		delete model.queryModel.targetModel.targetEntities;
		
		// Related Entity List (whole (model.queryModel))
		for(var i=0; i<model.queryModel.relatedModels.length; i++) {
			deleteUselessForBackEndRelatedProperties(model.queryModel.relatedModels[i])
		}
				
		//$log.info(angular.toJson(model));
		//$scope.showErrorAlert('Info', 'Running the query will be available in the final version. For the moment only construction-related functionality is possible.');
		retrieveFinalResults(angular.toJson(model));
	};
	
	
	
	
	
	$scope.finalResults = {};
	
	function retrieveFinalResults(searchEntityModel) {
    	
    	// Trying with promise - Start
    	
    	var modalOptions = {
			headerText: 'Loading Please Wait...',
			bodyText: 'Search process undergoing...'
		};
    	var modalInstance = modalService.showModal(modalDefaults, modalOptions);
    	
    	queryService.computeRelatedEntityQuery(searchEntityModel, $scope.credentials.token).then(function (queryResponse) {
    		if(queryResponse.status == '200') {
    			
        		var params = {
        				format: "application/json",
        				query: queryResponse.query + ' limit 100' // final Search Query
        		}
    			// Calling service to executing Query - Promise
	    		queryService.getEntityQueryResults($scope.serviceModel, params, $scope.credentials.token)
	    		.then(function (response) {
	        		
	    			if(response.status == -1) {
	    				$scope.message = 'There was a network error. Try again later.';
	    				$scope.showErrorAlert('Error', $scope.message);
	    			}
	    			
	    			else {
	    				if(response.status == '200') {
	    					$scope.finalResults = response.data;
	    				}
	    				else if(response.status == '400') {
	        				$log.info(response.status);
	        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
	        				$scope.showErrorAlert('Error', $scope.message);
	        			}
	        			else if(response.status == '401') {
	        				$log.info(response.status);
	        				$scope.showLogoutAlert();
	        				authenticationService.clearCredentials();
	        			}
	        			else {
	        				$log.info(response.status);
	        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
	        				$scope.showErrorAlert('Error', $scope.message);
	        			}
	    			
	    			} // else close
	    			
	    		}, function (error) {
	    			$scope.message = 'There was a network error. Try again later.';
	    			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
	    				data : error
	    			}));
	    		}).finally(function() {
	    			modalInstance.close();
	        	});
	        	// Execute query promise - End
			}
    		
			else if(queryResponse.status == '400') {
				$log.info(queryResponse.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
				modalInstance.close();
			}
			else if(queryResponse.status == '401') {
				$log.info(queryResponse.status);
				modalInstance.close();
				$scope.showLogoutAlert();
				authenticationService.clearCredentials();
			}
			else {
				$log.info(queryResponse.status);
				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
				$scope.showErrorAlert('Error', $scope.message);
				modalInstance.close();
			}
    	}, function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
			modalInstance.close();
		});
    	// Construct query promise - End
	};
	
	
		
	
		
		
		
		
		
		
		
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
	
		
		
	$scope.showMapForRelatedResultsDialog = function(ev, rowModel) {
		
		// Used for capturing the current row and thus knowing where to put selected items
    	$scope.currRowModel = rowModel;
    	$mdDialog.show({
    		scope: $scope,
    		templateUrl: 'views/dialog/selectFromMap.tmpl.html', 
    		parent: angular.element(document.body),
    		targetEvent: ev,
    		//clickOutsideToClose:true,
    		onComplete:function(){
    			loadMapForRelatedEntity(rowModel);
    			// Capturing the whole map
    			$scope.coordinatesRegion.north = 90.0000;
	            $scope.coordinatesRegion.south = -90.0000;
	            $scope.coordinatesRegion.west = -180.0000;
	            $scope.coordinatesRegion.east = 180.0000;
    			// Calling the service to load results on the map, when their count is less than a max allowed number
    			$scope.retrieveGeoData(true); // true is for considering "maxResoultCountForShowingPinsOnInit" as max allowed number
    		},
    		//fullscreen: true,
    		preserveScope: true,
    		fullscreen: false // Only for -xs, -sm breakpoints.
    	})
    	.then(function(data) {
    		console.log("then");
    	}, function(err) {
    		$scope.status = 'You cancelled the dialog.';
    	}).finally(function() {
    		
    	});
	}

	function loadMapForRelatedEntity(rowModel) {
		
		// Starting with map in related entity
					
		$scope.pins = [{ 
		    	type: rowModel.selectedRelatedEntity.name, 
		    	selectedImg: "../images/Map-Marker-Marker-Inside-Pink-icon.png", 
		    	unselectedImg: "../images/Map-Marker-Marker-Outside-Pink-icon.png"
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

	    // Adding "Searching by Toponyms" on the map
	    
	    // Code regarding toponyms - Start
	    
	    // Flying animation
	    // location: coordinates
	    // Example: https://openlayers.org/en/latest/examples/animation.html
	    function flyTo(location, done) {
	        var duration = 3000;
	        var zoom = 11;//$scope.map.getView().getZoom();
	        var parts = 2;
	        var called = false;
	        function callback(complete) {
	          --parts;
	          if (called) {
	            return;
	          }
	          if (parts === 0 || !complete) {
	            called = true;
	            done(complete);
	          }
	        }
	        $scope.map.getView().animate({
	          center: location,
	          duration: duration
	        }, callback);
	        $scope.map.getView().animate({
	          zoom: zoom - 3,
	          duration: duration / 2
	        }, {
	          zoom: zoom,
	          duration: duration / 2
	        }, callback);
	      }
	    
		// Instantiate with some options and add the Control
		var geocoder = new Geocoder('nominatim', {
			provider: 'osm',
			lang: 'en',
			placeholder: 'Search for ...',
			limit: 5,
			debug: false,
			autoComplete: true,
			keepOpen: false,
			preventDefault: true
		});
		
		$scope.map.addControl(geocoder);
	  
		geocoder.on('addresschosen', function(evt){
			console.log('get address');
			flyTo(evt.coordinate, function() {});
			/*
			$scope.map.setView(
				new ol.View({
		        	center: evt.coordinate, // Default is [0, 0]
		        	zoom: 11 //Default is 2
		        })
			);
			*/
		});
	    // Code regarding toponyms - End
	    
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
			$scope.retrieveGeoData(false); // false is for ignoring "maxResoultCountForShowingPinsOnInit" (when drawing rectangle)
			
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
				
				// Select
				if(evt.selected.length > 0) {
					var jsonItem = {};
					
					console.log('evt.selected: ');
					angular.forEach(evt.selected[0].getProperties(), function (property, key) {
						//console.log(key + ': ' + property.value);
						if(key != 'geometry' && key != 'featureType'&& key != 'east'&& key != 'west'&& key != 'north'&& key != 'south')
							jsonItem[key] = property
					});
					
					//if(!containedInList(jsonItem, rowModel.selectedRelatedInstanceList, true).contained)
					if(!containedInListBasedOnURI(jsonItem, rowModel.selectedRelatedInstanceList, 'uri').contained)
						rowModel.selectedRelatedInstanceList.push(jsonItem);
					
					// Show related entity results panel on the respective rowModel
					if(rowModel.shownEntitySearchResults == false && rowModel.selectedRelatedInstanceList.length > 0)
						rowModel.shownEntitySearchResults = true;
					else if (rowModel.shownEntitySearchResults == true && rowModel.selectedRelatedInstanceList.length < 1)
						rowModel.shownEntitySearchResults = false;
				}
				
				else {
					
				}
				
				// Deselect
				if(evt.deselected.length > 0) {
					var jsonItem = {};
					
					console.log('evt.deselected: ');
					angular.forEach(evt.deselected[0].getProperties(), function (property, key) {
						//console.log(key + ': ' + property.value);
						if(key != 'geometry' && key != 'featureType'&& key != 'east'&& key != 'west'&& key != 'north'&& key != 'south')
							jsonItem[key] = property
					});
					
					//var containedObject = containedInList(jsonItem, rowModel.selectedRelatedInstanceList, true);
					var containedObject = containedInListBasedOnURI(jsonItem, rowModel.selectedRelatedInstanceList, 'uri');
					if(containedObject.contained)
						rowModel.selectedRelatedInstanceList.splice(containedObject.index, 1);
					
					// Show related entity results panel on the respective rowModel
					if(rowModel.shownEntitySearchResults == false && rowModel.selectedRelatedInstanceList.length > 0)
						rowModel.shownEntitySearchResults = true;
					else if (rowModel.shownEntitySearchResults == true && rowModel.selectedRelatedInstanceList.length < 1)
						rowModel.shownEntitySearchResults = false;
				}
				
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
    			
    			// Constructing the pointFeature dynamically
    			var pointFeature = new ol.Feature();
    			pointFeature.setGeometry(polyFeature.getGeometry().getInteriorPoint());
    			pointFeature.set('featureType', 'marker');
    			
    			// Leaving it as it is in the original data and will 
    			// be changed when constructing the actual pop-up element
    			angular.forEach(geoResults[i], function (property, key) {
    				pointFeature.set(key, property);
    			});
    			
    			// Change  coordinate systems to display on the map
    			polyFeature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
    			pointFeature.getGeometry().transform('EPSG:4326', 'EPSG:3857');
	        	
    			// Marking as checked if already selected
    			if(containedInListBasedOnURI(geoResults[i], rowModel.selectedRelatedInstanceList, 'uri').contained)
    				select.getFeatures().push(pointFeature);	// DOES NOT WORK!!!!!!!!!!!!!!!!!!!!!!!!!
    			
    			// Setting unselected style
    			//pointFeature.setStyle(iconStylePinkUnselected);
	        	          	  
    			pointFeatures.push(pointFeature); // Adding into Array
	        	          	          	  
    		} // loop ends
			
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
		
		// considerTotalResultCount: Boolean used to decide whether to consider "maxResoultCountForShowingPinsOnInit" 
		// or not. Basically we use false to display pins when drawing rectangle and true to display pins when opening 
		// the dialog if their count is less than "maxResoultCountForShowingPinsOnInit"
		$scope.retrieveGeoData = function(considerTotalResultCount) {
			
			// Modal
			var modalOptions = {
	  			headerText: 'Loading Please Wait...',
	  			bodyText: 'Search process undergoing...'
	  		};
			
			if(considerTotalResultCount) { // Options when first opening this dialog
		  		modalOptions = {
	  				headerText: 'Loading Please Wait...',
	  				bodyText: 'Loading options...'
	  			};
			}
			
			var modalInstance = modalService.showModal(modalDefaults, modalOptions);
			
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
				if(queryResponse.status == '200') {
					console.log('queryResponse:');
					console.log(queryResponse);
					updatedQueryModel = angular.copy(rowModel.selectedRelatedEntity.queryModel)
		        	updatedQueryModel.query = queryResponse.data.query;
					delete updatedQueryModel.geo_query;
		    		delete updatedQueryModel.text_geo_query;
					console.log('Geospatial Query:');
					console.log(updatedQueryModel);
					
					// Only for case where results should be loaded when opening the map
					//
					// The hack is that we add a limit 1 more the allowed one and then we should check if the 
					// total results are more than the allowed (if they are more, they will be just one more)
					// Then we either use the results or just throw them away
					if(considerTotalResultCount)
						updatedQueryModel.query = updatedQueryModel.query + ' limit ' + ($scope.configuration.relatedEntityMap.maxResoultCountForShowingPinsOnInit + 1);
					
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
		    					
		    					// Case where results should be loaded when opening the map
		    					if(considerTotalResultCount) {
		    						// Checking if count is less or equal to the allowed limit
		    						if(response.data.results.bindings.length < $scope.configuration.relatedEntityMap.maxResoultCountForShowingPinsOnInit)
		    							handleGeoResultsForMap(response.data.results.bindings);
		    					}
		    					
		    					// Case where results are loaded by drawing rectangle
		    					else // if(!considerTotalResultCount) {
		    						handleGeoResultsForMap(response.data.results.bindings);
		    					
		    					modalInstance.close();
		    				}
		    				
		    				else if(response.status == '400') {
		        				$log.info(response.status);
		        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
		        				$scope.showErrorAlert('Error', $scope.message);
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
		        				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
		        				$scope.showErrorAlert('Error', $scope.message);
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
				}
				
				else if(queryResponse.status == '400') {
    				$log.info(queryResponse.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    				modalInstance.close();
    			}
    			else if(queryResponse.status == '401') {
    				$log.info(queryResponse.status);
    				modalInstance.close();
    				$scope.showLogoutAlert();
    				authenticationService.clearCredentials();
    			}
    			else {
    				$log.info(queryResponse.status);
    				$scope.message = 'There was a network error. Try again later and if the same error occures again please contact the administrator.';
    				$scope.showErrorAlert('Error', $scope.message);
    				modalInstance.close();
    			}
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
	
	$scope.closeSelectedFromMapDialogDialog = function() { 
		$mdDialog.cancel();
	}
	
	
	/*
	angular.forEach(list, function(value, key) {
		//if()
		//exists = 
		$log.info('value: ' + value);
	});

	Promise.resolve(initRowModels()).then(function() {
		//initPurpose = false;
	});
	*/
	
	
}]);