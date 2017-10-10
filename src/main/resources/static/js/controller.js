/**
 * The main angularJS controllers (handling tabs, the query submission and serverside paginator and the file importing
 * 
 * @author Vangelis Kritsotakis
 */


















app.controller("tabCtrl", [ '$transitions', '$state', '$scope', '$sessionStorage', function($transitions, $state, $scope, $sessionStorage) {
	
	//alert("ToDO Read controller.js top notes");
	// /Fix for making the selected tab look selected
	
	$scope.currentTab = $sessionStorage.currentTab;
	
	// Using session to fix the browser refresh page issue
	$transitions.onSuccess({to: true}, function ($state) {
		//console.log("$state.data.selectedTab: " + $state.router.globals.current.data.selectedTab)
		if($state.router.globals.current.data != undefined)
			$sessionStorage.currentTab = $state.router.globals.current.data.selectedTab;
	});
	/*
	$scope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
		$scope.currentTab = toState.data.selectedTab;
		//$sessionStorage.currentTab = toState.data.selectedTab;
	});
	*/

	$scope.tabs = [{ 
    	   index:"0", 
    	   title:"Query", 
    	   description:"Some description for query", 
    	   content:"query.html", 
    	   disabled:"false",
    	   href:"#/query",
    	   state:"tabs.queryTab", 
    	   view:"tab-query"
       }, {
    	   index:"1", 
    	   title:"Geo-Query", 
    	   description:"Some description for geoQuery", 
    	   href:"#/geoQuery", 
    	   content:"geoQuery.html", 
    	   disabled:"false", 
    	   href:"#/geoQuery",
    	   state:"tabs.geoQueryTab", 
    	   view:"tab-geoQuery"
    	}, {
    	   index:"2", 
    	   title:"Advanced Query", 
    	   description:"Some description for advanced query", 
    	   href:"#/advancedQuery", 
    	   content:"advancedQuery.html", 
    	   disabled:"false", 
    	   href:"#/advancedQuery",
    	   state:"tabs.advancedQueryTab", 
    	   view:"tab-advancedQuery"
    	}, {
    	   index:"3", 
    	   title:"Import", 
    	   description:"Some description for import", 
    	   href:"#/import", 
    	   content:"import.html", 
    	   disabled:"false", 
    	   href:"#/import",
    	   state:"tabs.importTab", 
    	   view:"tab-import"
    	}
    ];
	/*
	$scope.changeHash = function(data) {
		window.location.hash = data;
	};
	*/
} ]);


app.controller("queryCtrl", [ '$scope', '$http', 'modalService', 'queryService', 'authenticationService', '$state', '$mdDialog', '$mdSidenav',
                               function($scope, $http, modalService, queryService, authenticationService, $state, $mdDialog, $mdSidenav) {
	$scope.headingTitle = "Query Search";
	
	// Toggles SidePanel
	$scope.toggleInfo = buildToggler('rightInfo');

    function buildToggler(componentId) {
      return function() {
        $mdSidenav(componentId).toggle();
      };
    }
	
	// Calling service to get the user's credentials (token, userId)
	function initCredentials() {
		$scope.credentials = authenticationService.getCredentials();
	}
	initCredentials();
	
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
	
	// Alert (danger, warning, success)
	$scope.alerts = [];
	
	// Pagination
	$scope.maxSize = 5;
	$scope.currentPage = 1;
	$scope.itemsPerPage = 20;
	
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
	
	// Drop down list for types
	/*$scope.entities = [
		'Persons',
		'Projects',
		'Publications',
		'Organization Units'
	];
	*/
	$scope.entities = [
        {name: 'Persons', thesaurus: "thesaurus/persons-firstAndLastNames.json"},
        {name: 'Projects', thesaurus: "thesaurus/project-acronyms.json"},
        {name: 'Publications', thesaurus: "thesaurus/publications-titles.json"},
        {name: 'Organization Units', thesaurus: "thesaurus/organizationUnits-acronyms.json"},
        {name: 'Resources', thesaurus: "thesaurus/resources.json"}
	];
	
	$scope.projections = [
	    {name: 'Persons'},
	    {name: 'Projects'},
	    {name: 'Publications'},
	    {name: 'Organization Units'},
	    {name: 'Resources'}
	];
	
	//$scope.selectedProjection = {name: 'Projects', thesaurus: "project-acronyms.json"};
	
	// Initializing
	$scope.selectedEntity = {name: '', thesaurus: ''};
	$scope.selectedProjection = {name: ''};
	
	$scope.nameGraphs = [];
	
	function initNameGraphs() {
		// Call promise to retrieve namedGraphs
		var additionalParams = {excludeEposNamegraph: false}
		queryService.getNameGraphsResults($scope.credentials.token, additionalParams)
		.then(function (response){
			$scope.statusRequestInfo = response.statusRequestInfo;
			
			// Checking the response from blazegraph
			if(response.statusRequestCode == '200') {
				$scope.nameGraphs = response.result.results.bindings;
				
				//$scope.selectedNamegraphs = $scope.nameGraphs
				
				// Adding low case values for checking and property that 
				// sets whether it should be selected by default
				angular.forEach($scope.nameGraphs, function(obj) {
					obj.lowcaseName = obj.g.value.toLowerCase();
					obj.selected=true;
					//alert(obj.lowcaseName);
			    });
				
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
			
		},function (error){
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + message + "\n" + JSON.stringify({
				data : error
			}));
		});
		
	}
	
	initNameGraphs();
	
	// Server-Side Pagination for query
	function getData() {
		var pageParams = {
			page: $scope.currentPage, 
			itemsPerPage: $scope.itemsPerPage,
			selectedEntity: $scope.selectedEntity.name,
			selectedProjection: $scope.selectedProjection.name,
			selectedNamegraphs: $scope.selectedNamegraphs
		}
		
		queryService.getPageResults(pageParams)
		.then(function (response){
			$scope.endpointResult = response.result;
		},function (error){
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message2 + "\n" + JSON.stringify({
				data : error
			}));
		});
	}
	
	$scope.pageChanged = function() {
		getData();
	};
	
    $scope.loadTags = function($query) {
        //alert($scope.selectedEntity.thesaurus);
        return $http.get($scope.selectedEntity.thesaurus, {cache: true}).then(function(response) {
          var tags = response.data;
          return tags.filter(function(tag) {
            return tag.name.toLowerCase().indexOf($query.toLowerCase()) != -1;
          });
        });
    };
    
	$scope.submitAsyncQueryJSON = function() {
		var queryModel = {
            terms : $scope.terms,
            itemsPerPage : $scope.itemsPerPage,
            template: $scope.selectedTemplate,
            selectedEntity: $scope.selectedEntity.name,
            selectedProjection: $scope.selectedProjection.name,
			selectedNamegraphs: $scope.selectedNamegraphs
		};
		
		// ToDo: Highlight the respective component
		
		if ($scope.selectedEntity.name == null || $scope.selectedEntity.name == '') {
			$scope.alerts.splice(0); // Close alerts
			$scope.alerts.push({type: 'warning-funky', msg: 'Please select an entity from the drop down list and try again!'});
		}
		
		else if ($scope.terms == null || $scope.terms == '') {
			$scope.alerts.splice(0); // Close alerts
			$scope.alerts.push({type: 'warning-funky', msg: 'Please enter one or more key-words in the query field and try again!'});
		}
		
		else if ($scope.selectedProjection.name == null || $scope.selectedProjection.name == '') {
			$scope.alerts.splice(0); // Close alerts
			$scope.alerts.push({type: 'warning-funky', msg: 'Please select a projection from the drop down list and try again!'});
		}
		
		else if ($scope.selectedNamegraphs == null || $scope.selectedNamegraphs == '') {
			$scope.alerts.splice(0); // Close alerts
			$scope.alerts.push({type: 'warning-funky', msg: 'Please select at least one collection from the drop down list and try again!'});
		}
	
		// Only if not empty (everything is fine)
		else {
						
			// Modal
			var modalInstance = modalService.showModal(modalDefaults, modalOptions);
			
			var splitStr = '#@#';
			queryService.getQueryResults(queryModel, $scope.credentials.token)
			.then(function (response) {
				
				// Close alerts
				$scope.alerts.splice(0);
				
				if(response.supportedCase == true) {
				
					$scope.statusRequestInfo = response.statusRequestInfo;
										
					// Checking the response from blazegraph
					if(response.statusRequestCode == '200') {
						
						$scope.lastEndPointForm = response;
						$scope.endpointResult = response.result;
						$scope.totalItems = response.totalItems;
						$scope.currentPage = 1;
						$scope.alerts.push({type: 'success-funky', msg: 'The query was submitted successfully'});
						
					}
					else if(response.statusRequestCode == '400') {
						$scope.alerts.push({type: 'danger-funky', msg: response.statusRequestInfo + '. Please check the query for syntax errors and try again.'});
					}
					else if(response.statusRequestCode == '401') {				
						$scope.showLogoutAlert();
						authenticationService.clearCredentials();
					}
					else {
						$scope.alerts.push({type: 'danger-funky', msg: response.statusRequestInfo});
					}
				}
				
				else {
					$scope.alerts.push({type: 'danger-funky', msg: 'This case is not supported yet. Please try another one.'});
					$scope.lastEndPointForm = response;
				}
				
				modalInstance.close();
				
			},function (error) {
				$scope.message = 'There was a network error. Try again later.';
				alert("failure message: " + $scope.message + "\n" + JSON.stringify({
					data : error
				}));
				modalInstance.close();
			});

		}

		// Submit stuff Ends here
		
	}
	
	$scope.showTooltip = false;
	
	
} ]);

app.controller("geoQueryCtrl", [ '$scope', '$http', 'modalService', 'queryService', 'authenticationService', '$state',  '$timeout', '$mdDialog', '$mdSidenav',
                              function($scope, $http, modalService, queryService, authenticationService, $state,  $timeout, $mdDialog, $mdSidenav) {
	
	$scope.headingTitle = "Geo-Spatial Search";
	
	var splitStr = '#@#';
	
	// Toggles SidePanel
	$scope.toggleInfo = buildToggler('rightInfo');

    function buildToggler(componentId) {
      return function() {
        $mdSidenav(componentId).toggle();
      };
    }
	
	// Calling service to get the user's credentials (token, userId)
	function initCredentials() {
		$scope.credentials = authenticationService.getCredentials();
	}
	initCredentials();
	
	// Just a way of checking credentials
	// I call the query service which checks credentials anyway
	function checkAuthorization() {
		// Call promise to retrieve namedGraphs		
		queryService.checkAuthorization($scope.credentials.token)
		.then(function (response){
			$scope.statusRequestInfo = response.statusRequestInfo;
			
			// Checking the response from blazegraph
			if(response.statusRequestCode == '200') {
				// Do nothing
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
			
		},function (error){
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
		});
	}
	checkAuthorization();
	
	var modalOptions = {
		headerText: 'Loading Please Wait...',
		bodyText: 'Searching Geospatial Data...'
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
	
	// Alert (danger, warning, success)
	$scope.alerts = [];
	$scope.importErrorAlerts = [];
	$scope.importSuccessAlerts = [];
	
	// Pagination
	$scope.maxSize = 5;
	$scope.currentPage = 1;
	$scope.itemsPerPage = 20;
		
	// Server-Side Pagination for query
	function getData() {
		var pageParams = {
			page: $scope.currentPage, 
			itemsPerPage: $scope.itemsPerPage
		}
		
		queryService.getPageResults(pageParams)
		.then(function (response){
			$scope.endpointResult = response.result;
		},function (error){
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message2 + "\n" + JSON.stringify({
				data : error
			}));
		});
	}
	
	$scope.pageChanged = function() {
		getData();
	};
	
	// Starting with map
	
	$timeout(function(){
	
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
		        }).extend([mousePositionControl]),
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
	
	});//, 500);
    // Finished with map
	
	
} ]);
	
app.controller("importCtrl", [ '$scope', 'queryService', '$mdDialog', 'authenticationService', '$state', '$mdSidenav', 
                               function($scope, queryService, $mdDialog, authenticationService, $state, $mdSidenav) {
	// Toggles SidePanel
	$scope.toggleInfo = buildToggler('rightInfo');

    function buildToggler(componentId) {
      return function() {
        $mdSidenav(componentId).toggle();
      };
    }
	
	$scope.headingTitle = "Import to Triplestore from File";	
	
	// Calling service to get the user's credentials (token, userId)
	function initCredentials() {
		$scope.credentials = authenticationService.getCredentials();
	}
	initCredentials();
	
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
	
	// Alert (danger, warning, success)
	$scope.alerts = [];
	$scope.importErrorAlerts = [];
	$scope.importSuccessAlerts = [];
	
	$scope.nameGraphs = [];
	
	function initNameGraphs() {
		// Call promise to retrieve namedGraphs		
		var additionalParams = {excludeEposNamegraph: false}
		queryService.getNameGraphsResults($scope.credentials.token, additionalParams)
		.then(function (response){
			$scope.statusRequestInfo = response.statusRequestInfo;
			
			// Checking the response from blazegraph
			if(response.statusRequestCode == '200') {
				$scope.nameGraphs = response.result.results.bindings;
				
				// Adding low case values for checking
				angular.forEach($scope.nameGraphs, function(obj) {
					obj.lowcaseName = obj.g.value.toLowerCase();
			    });
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
			
		},function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + message + "\n" + JSON.stringify({
				data : error
			}));
		});
		
	}
	
	initNameGraphs();
	
	/**
     * Search for namegraphs... use $timeout to simulate
     * remote dataservice call.
     */
	$scope.querySearch = function (query) {
		var results = query ? $scope.nameGraphs.filter( createFilterFor(query) ) : $scope.nameGraphs, deferred;
		return results;
	}
	
	/**
     * Create filter function for a query string
     */
    function createFilterFor(query) {
      var lowercaseQuery = angular.lowercase(query);

      return function filterFn(state) {
        return (state.lowcaseName.indexOf(lowercaseQuery) === 0);
      };

    }
	
	$scope.selectedFormat = 'Automatic';
	$scope.formats = [
		'Automatic',
		'application/rdf+xml',
		'text/rdf+n3',
		'text/plain',
		'application/x-turtle',
		'application/ld+json',
		'application/x-trig',
		'text/x-nquads'
	];
		
	$scope.setFormat = function(format) {
		$scope.selectedFormat = format;
	};
	
	$scope.setSearchText = function(text) {
		$scope.searchText = text;
	};
    	
	// Configuration for the fileAppDirective
    $scope.dropzoneConfig = {
    	
    	/*
    	rdf, rdfs, owl -> RDFXML
		nt -> NTRIPLES
		n3 -> N3
		ttl -> TURTLE
		trig -> TRIG
		trix -> TRIX
    	*/
        options: { // passed into the Dropzone constructor
            url: '/upload',
            uploadMultiple: true,
            paramName: "uploadfile", // The name that will be used to transfer the file
            params: { 'contentTypeParam': $scope.selectedFormat, 'authorizationParam': $scope.credentials.token },
            maxFilesize: 400, // MB
            maxThumbnailFilesize: 10,
            parallelUploads: 1,
            acceptedFiles: '.rdfs,.rdf,.owl,.nt,.n3,.nt,.ttl,.jsonld,.trig,.trix',
            autoProcessQueue: false,
            maxFiles: 100//,
            //method: "POST"
        },
        'eventHandlers': {
        	'init': function () {
            	this.options.acceptedFiles = $scope.acceptedFile;
            	$scope.$apply(function() {
	            	$scope.alerts.splice(0);
	        		$scope.importErrorAlerts.splice(0);
            	});
            },
            'addedfile': function (file,response) {
            	$scope.file = file;
                /*
            	if (this.files[1]!=null) {
                    this.removeFile(this.files[0]);
                }
                */
                $scope.$apply(function() {
                    $scope.fileAdded = true;
                });
                                
            },
            'processing': function (file) {
            	// Setting additional parameter dynamically 
            	// The parameter holds the content-type (i.e. application/rdf+xml)
            	if($scope.selectedFormat == 'Automatic') {
	            	this.options.params = { 
	            		'contentTypeParam': getContentTypeFromFileExtension(file.name.split('.').pop()),
	            		'namedGraphParam': $scope.searchText,
	            		'authorizationParam': $scope.credentials.token 
	            	};
            	}
            	else {
            		this.options.params = { 
    	            		'contentTypeParam': $scope.selectedFormat, 
    	            		'namedGraphParam': $scope.searchText,
    	            		'authorizationParam': $scope.credentials.token 
    	            	};
            	}
            },
            'success': function (file, response) {
            	
            	$scope.$apply(function() {
            		
            		if(response == null) {
                		$scope.alerts.push({
                			type: 'danger-funky', 
                			msg: 'There was an internal error while trying to upload the file \"' + file.name + '\".'
                		});
                		$scope.importErrorAlerts.push({
                			type: 'danger-funky', 
                			msg: 'There was an internal error while trying to upload the file \"' + file.name + '\".',
                			titleType: 'Error'
                		});
            		}
            		else {
	            		$scope.alerts.splice(0);
	            		$scope.alerts.push({
	            			type: 'success-funky', 
	            			msg: 'File \"' + file.name + '\" was imported successfully in ' + response.message.data.milliseconds + ' milliseconds.'
	            		});
	            		$scope.importSuccessAlerts.push({
	            			type: 'success-funky', 
	            			msg: 'File \"' + file.name + '\" was imported successfully in ' + response.message.data.milliseconds + ' milliseconds.',
	            			titleType: 'Success'
	            		});
            		}
            		
            		$scope.file = null;
            		            		
            	});
            	
            	this.removeFile(file);
            	
            	if (this.getUploadingFiles().length === 0 && this.getQueuedFiles().length === 0) {
                	$scope.alerts.splice(0);
                	
                	// Two cases: either "only success" or "success and errors". 
                	// Errors without a single success is not a case. Remember that you are in the success response handler.
                	if($scope.importErrorAlerts.length === 0) {
                		$scope.alerts.push({
                			type: 'success-funky', 
                			msg: 'All files have been successfully imported!', 
                			showDetails: true
                		});
                	}
                };
            	
            },
            
            'complete': function (file, response) {
            	if (this.getUploadingFiles().length === 0 && this.getQueuedFiles().length === 0) {
            		
            		if($scope.importErrorAlerts.length > 0) {
            			            			
	            		$scope.$apply(function() {
	            			$scope.alerts.splice(0);
	            			
	            			// Show warning message when there are both errors and success messages
	            			if($scope.importSuccessAlerts.length > 0) {
	            				$scope.alerts.push({
	            					type: 'warning-funky', 
	            					msg: 'Some of the files have been successfully imported! However there were errors on certain files!', 
	            					showDetails: true
	            				});
	            			}
	            			
	            			// Show errors
	            			angular.forEach($scope.importErrorAlerts, function(value, key) {
	            				$scope.alerts.push({type: value.type, msg: value.msg});
	            			});
	            			
	            		});
            		
            		}
            		
            		// Refreshing NamedGraphs list for the auto-complete
            		initNameGraphs();
            	}
            },
            
            'successmultiple': function (files, response) {
            	if(files.length>1) {
	            	$scope.$apply(function() {
	            		$scope.alerts.splice(0);
	            		$scope.alerts.push({
	            			type: 'success-funky', 
	            			msg: 'All files have been successfully imported.'
	            		});
	            	});
            	}
            	/*
            	for (var i=0; i<files.length; i++) {
            		this.removeFile(files[i]);
            	}
            	*/
            },
            
            'completemultiple': function (files, response) {
            	
            	this.processQueue();
            	//alert("all good")
            },
            
            'error': function (file, response) {
            	
            	$scope.$apply(function() {
            		//$scope.importErrorAlerts.splice(0);
            		//$scope.alerts.push({type: 'danger-funky', msg: response.slice(0, -1) + '  while trying to upload the file \"' + file.name + '\". Maybe that\'s caused due to non matching file\'s content-type.'});
            		$scope.importErrorAlerts.push({
            			type: 'danger-funky', 
            			msg: response.slice(0, -1) + '  while trying to upload the file \"' + file.name + '\". Maybe that\'s caused due to non matching file\'s content-type.', 
            			titleType: 'Error'
            		});
            		$scope.file = null;
            	});
            }
            
        }
    };
    
    $scope.uploadFile = function() {
    	$scope.processDropzone();
    };
    
    $scope.reset = function() {
    	$scope.file = null;
    	$scope.setFormat('Automatic');
    	$scope.setSearchText('');
        $scope.resetDropzone();
        $scope.alerts.splice(0);
        $scope.importErrorAlerts.splice(0);
        $scope.importSuccessAlerts.splice(0);
    };
    
    function getContentTypeFromFileExtension(fileExtension) {

    	if(fileExtension == 'rdfs' || fileExtension == 'rdf' || fileExtension == 'owl') {
    		return "application/rdf+xml";
    	}
    	else if (fileExtension == 'n3' || fileExtension == 'nt') {
    		return "text/rdf+n3";
    	}
    	else if (fileExtension == 'ttl') {
    		return "application/x-turtle";
    	}
    	else if (fileExtension == 'trig') {
    		return "application/x-trig";
    	}
    	else if (fileExtension == 'trix') {
    		return "text/x-nquads";
    	}
    	else if (fileExtension == 'jsonld') {
    		return "application/ld+json";
    	}
    	else {
    		return "text/plain";
    	}
    }
    
	$scope.showAdvanced = function(ev) {
    	$mdDialog.show({
    		//controller: 'importCtrl',
    		scope: $scope,
    		templateUrl: 'views/dialog/successImportFileList.html',
    		parent: angular.element(document.body),
    		targetEvent: ev,
    		clickOutsideToClose:true,
    		preserveScope: true,
    		fullscreen: false // Only for -xs, -sm breakpoints.
    	});
    	
	};
	
} ]);

app.controller("advancedQueryCtrl", [ '$scope', '$http', 'modalService', 'queryService', 'authenticationService', '$state', '$mdSidenav',
                              function($scope, $http, modalService, queryService, authenticationService, $state, $mdSidenav) {
	$scope.headingTitle = "Advanced Queries";
	
	// Toggles SidePanel
	$scope.toggleInfo = buildToggler('rightInfo');

   function buildToggler(componentId) {
     return function() {
       $mdSidenav(componentId).toggle();
     };
   }
	
	// Calling service to get the user's credentials (token, userId)
	function initCredentials() {
		$scope.credentials = authenticationService.getCredentials();
	}
	initCredentials();
	
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
	
	// Alert (danger, warning, success)
	$scope.alerts = [];
	
	// Pagination
	$scope.maxSize = 5;
	$scope.currentPage = 1;
	$scope.itemsPerPage = 20;
	
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
	
	// Array model used for the advanced queries
	$scope.advancedQueries = [{ 
    	   index: "0", 
    	   title: "Use Case 1", 
    	   description: " Find the organizations which employed “Keith Jeffery” and “Philippe Rohou”. Moreover, find the time period of their employment in each organization.",
    	   query: "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n" +
    	   		  "SELECT distinct (concat(str(?object), '#@#', str(?namel)) as ?Name)\n" +
    	   		  "(concat(str(?ou2URI), '#@#',str(?org2)) as ?Parent_Organization)\n" +
    	   		  "(concat(str(?ouURI), '#@#',str(?org)) as ?Organization)\n" +
    	   		  "(?role as ?Role)\n" +
    	   		  "(?sdate as ?Date_Started)\n" +
    	   		  "(?edate as ?Date_Ended)\n" +
    	   		  "WHERE {\n" +
	    	   		  "?object a cerif:Person.\n" +
	    	   		  "?object cerif:is_source_of ?PersNamePers.\n" +
	    	   		  "?object rdfs:label ?namel.\n" +
	    	   		  "?PersNamePers cerif:has_destination ?PersName.\n" +
	    	   		  "?ou a cerif:OrganisationUnit.\n" +
	    	   		  "?ou cerif:has_URI ?ouURI.\n" +
	    	   		  "?ou cerif:has_name ?org.\n" +
	    	   		  "?ou cerif:is_destination_of ?fle1.\n" +
	    	   		  "?fle1 cerif:has_source ?object.\n" +
	    	   		  "?fle1  cerif:has_startDate ?sdate.\n" +
	    	   		  "?fle1 cerif:has_endDate ?edate.\n" +
	    	   		  "?fle1 cerif:has_classification ?class.\n" +
	    	   		  "?class rdfs:label ?role.\n" +
	    	   		  "OPTIONAL {\n" +
		    	   		  "?ou cerif:is_source_of ?fle2.\n" +
		    	   		  "?fle2 cerif:has_destination ?ou2.\n" +
		    	   		  "?ou2 a cerif:OrganisationUnit.\n" +
		    	   		  "?ou2 cerif:has_URI ?ou2URI.\n" +
		    	   		  "?ou2 cerif:has_name ?org2.\n" +
		    	   		  "?fle2 cerif:has_startDate ?sdate2.\n" +
		    	   		  "?fle2 cerif:has_endDate ?edate2.\n" +
		    	   		  "?fle2 cerif:has_classification ?class2.\n" +
		    	   		  "?class rdfs:label ?role2.\n" +
	    	   		  "}\n" +
	    	   		  "BIND(if(bound(?ou2URI),?Parent_Organization,\"-\") as ?Parent_Organization).\n" +
	    	   		  "#?name bds:search \"Keith Philippe\".\n" +
	    	   		  "#filter(xsd:dateTime(?sdate) > xsd:dateTime(\"2009-01-01T00:00:00\")).\n" +
    	   		  "}",
    	   icon: "/images/search-doc.svg"
       }, {
    	   index: "1", 
    	   title: "Use Case 2", 
    	   description: "Find all the organizations which employed “Keith Jeffery” and “Philippe Rohou” within the time period from 2004 to 2008.", 
    	   notes: "Some notes for the query 2",
    	   query: "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n" +
	   		  "SELECT distinct (concat(str(?object), '#@#', str(?namel)) as ?Name)\n" +
	   		  "(concat(str(?ou2URI), '#@#',str(?org2)) as ?Parent_Organization)\n" +
	   		  "(concat(str(?ouURI), '#@#',str(?org)) as ?Organization)\n" +
	   		  "(?role as ?Role)\n" +
	   		  "(?sdate as ?Date_Started)\n" +
	   		  "(?edate as ?Date_Ended)\n" +
	   		  "WHERE {\n" +
    	   		  "?object a cerif:Person.\n" +
    	   		  "?object cerif:is_source_of ?PersNamePers.\n" +
    	   		  "?object rdfs:label ?namel.\n" +
    	   		  "?PersNamePers cerif:has_destination ?PersName.\n" +
    	   		  "?ou a cerif:OrganisationUnit.\n" +
    	   		  "?ou cerif:has_URI ?ouURI.\n" +
    	   		  "?ou cerif:has_name ?org.\n" +
    	   		  "?ou cerif:is_destination_of ?fle1.\n" +
    	   		  "?fle1 cerif:has_source ?object.\n" +
    	   		  "?fle1  cerif:has_startDate ?sdate.\n" +
    	   		  "?fle1 cerif:has_endDate ?edate.\n" +
    	   		  "?fle1 cerif:has_classification ?class.\n" +
    	   		  "?class rdfs:label ?role.\n" +
    	   		  "OPTIONAL {\n" +
	    	   		  "?ou cerif:is_source_of ?fle2.\n" +
	    	   		  "?fle2 cerif:has_destination ?ou2.\n" +
	    	   		  "?ou2 a cerif:OrganisationUnit.\n" +
	    	   		  "?ou2 cerif:has_URI ?ou2URI.\n" +
	    	   		  "?ou2 cerif:has_name ?org2.\n" +
	    	   		  "?fle2 cerif:has_startDate ?sdate2.\n" +
	    	   		  "?fle2 cerif:has_endDate ?edate2.\n" +
	    	   		  "?fle2 cerif:has_classification ?class2.\n" +
	    	   		  "?class rdfs:label ?role2.\n" +
    	   		  "}\n" +
    	   		  "BIND(if(bound(?ou2URI),?Parent_Organization,\"-\") as ?Parent_Organization).\n" +
    	   		  "?name bds:search \"Jeffery || Rohou\".\n" +
    	   		  "filter( ((xsd:dateTime(?sdate) > xsd:dateTime(\"2004-01-01T00:00:00\")) && (xsd:dateTime(?sdate) < xsd:dateTime(\"2008-12-31T00:00:00\"))) || \n" +
    	   		  "((xsd:dateTime(?sdate) < xsd:dateTime(\"2004-01-01T00:00:00\")) && (xsd:dateTime(?edate) > xsd:dateTime(\"2008-12-31T00:00:00\")))  ).\n" +
	   		  "}",
    	   icon: "/images/search-doc.svg"
    	}, {
    	   index: "2", 
    	   title: "Use Case 3", 
    	   description: "Find the organizations were “Keith Jeffery” was Director in 2006.", 
    	   notes: "Some notes for the query 3",
    	   query: "PREFIX cerif:   <http://eurocris.org/ontology/cerif#>\n" +
	   		  "SELECT distinct (concat(str(?object), '#@#', str(?namel)) as ?Name)\n" +
	   		  "(concat(str(?ou2URI), '#@#',str(?org2)) as ?Parent_Organization)\n" +
	   		  "(concat(str(?ouURI), '#@#',str(?org)) as ?Organization)\n" +
	   		  "(?role as ?Role)\n" +
	   		  "(?sdate as ?Date_Started)\n" +
	   		  "(?edate as ?Date_Ended)\n" +
	   		  "WHERE {\n" +
    	   		  "?object a cerif:Person.\n" +
    	   		  "?object cerif:is_source_of ?PersNamePers.\n" +
    	   		  "?object rdfs:label ?namel.\n" +
    	   		  "?PersNamePers cerif:has_destination ?PersName.\n" +
    	   		  "?ou a cerif:OrganisationUnit.\n" +
    	   		  "?ou cerif:has_URI ?ouURI.\n" +
    	   		  "?ou cerif:has_name ?org.\n" +
    	   		  "?ou cerif:is_destination_of ?fle1.\n" +
    	   		  "?fle1 cerif:has_source ?object.\n" +
    	   		  "?fle1  cerif:has_startDate ?sdate.\n" +
    	   		  "?fle1 cerif:has_endDate ?edate.\n" +
    	   		  "?fle1 cerif:has_classification ?class.\n" +
    	   		  "?class rdfs:label ?role.\n" +
    	   		  "OPTIONAL {\n" +
	    	   		  "?ou cerif:is_source_of ?fle2.\n" +
	    	   		  "?fle2 cerif:has_destination ?ou2.\n" +
	    	   		  "?ou2 a cerif:OrganisationUnit.\n" +
	    	   		  "?ou2 cerif:has_URI ?ou2URI.\n" +
	    	   		  "?ou2 cerif:has_name ?org2.\n" +
	    	   		  "?fle2 cerif:has_startDate ?sdate2.\n" +
	    	   		  "?fle2 cerif:has_endDate ?edate2.\n" +
	    	   		  "?fle2 cerif:has_classification ?class2.\n" +
	    	   		  "?class rdfs:label ?role2.\n" +
    	   		  "}\n" +
    	   		  "BIND(if(bound(?ou2URI),?Parent_Organization,\"-\") as ?Parent_Organization).\n" +
    	   		  "?namel bds:search \"Jeffery\".\n" +    	   		  
    	   		  "filter(regex(?role,'Director') ).\n" +
    	   		  "filter( (xsd:dateTime(?sdate) < xsd:dateTime(\"2006-01-01T00:00:00\")) && (xsd:dateTime(\"2006-01-01T00:00:00\") < xsd:dateTime(?edate)) ).\n" +
	   		  "}",
    	   icon: "/images/search-doc.svg"
    	}
    ];
	
	// Server-Side Pagination for query
	function getData() {
		var pageParams = {
			page: $scope.currentPage, 
			itemsPerPage: $scope.itemsPerPage,
		}
		
		queryService.getPageResults(pageParams)
		.then(function (response){
			$scope.endpointResult = response.result;
		},function (error){
		    $scope.message2 = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message2 + "\n" + JSON.stringify({
				data : error
			}));
		});
	}
	
	$scope.pageChanged = function() {
		getData();
	};
	
	
	$scope.runAdvancedQuery = function(item) {
		console.log("Running " + item.title);
		$scope.submitAsyncAdvancedQueryJSON(item.query);
	}
	
	var splitStr = '#@#';
	
	$scope.submitAsyncAdvancedQueryJSON = function(query) {
	
		var queryModel = {
	        itemsPerPage : $scope.itemsPerPage,
	        queryToExecute: query
		};
	
		// Modal
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
	
		queryService.getAdvancedQueryResults(queryModel, $scope.credentials.token)
		.then(function (response) {
		
			// Close alerts
			$scope.alerts.splice(0);
			
			$scope.statusRequestInfo = response.statusRequestInfo;
								
			// Checking the response from blazegraph
			if(response.statusRequestCode == '200') {
				
				$scope.lastEndPointForm = response;
				$scope.endpointResult = response.result;
				$scope.totalItems = response.totalItems;
				$scope.currentPage = 1;
				$scope.alerts.push({type: 'success-funky', msg: 'The query was submitted successfully'});
				
			}
			else if(response.statusRequestCode == '400') {
				$scope.alerts.push({type: 'danger-funky', msg: response.statusRequestInfo + '. Please check the query for syntax errors and try again.'});
			}
			else if(response.statusRequestCode == '401') {				
				$scope.showLogoutAlert();
				authenticationService.clearCredentials();
			}
			else {
				$scope.alerts.push({type: 'danger-funky', msg: response.statusRequestInfo});
			}
		
			modalInstance.close();
		
		},function (error) {
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
			modalInstance.close();
		});
	
	};
	
	
} ]);
