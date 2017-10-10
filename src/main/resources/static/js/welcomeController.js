/**
 * The main angularJS controllers (handling tabs, the query submission and serverside paginator and the file importing
 * 
 * @author Vangelis Kritsotakis
 */
app.controller("welcomeCtrl", ['$state', '$scope', '$sessionStorage', '$mdSidenav', function($state, $scope, $sessionStorage, $mdSidenav) {
//app.controller("welcomeCtrl", ['$transitions', '$state', '$scope', '$sessionStorage', function($transitions, $state, $scope, $sessionStorage) {
	/*
	// Using session to fix the browser refresh page issue
	$transitions.onSuccess({to: true}, function ($state) {
		//console.log("$state.data.selectedTab: " + $state.router.globals.current.data.selectedTab)
		if($state.router.globals.current.data != undefined)
			$sessionStorage.currentTab = $state.router.globals.current.data.selectedTab;
	});
	*/
	
	$scope.headingTitle = "Home";
	
	$scope.cards = [{ 
	 	   index:"0", 
	 	   title:"Search Metadata", 
	 	   description:"Data navigation through a simple and user friendly interface, enriched with auxiliary functionality. Simplifies the \"data acquisition\" process by splitting it in small easy to follow steps", 
	 	   icon:"mdi-magnify", 
	 	   disabled:"false",
	 	   size:"150px",
	 	   actionButtonLabel:"Continue",
	 	   href:"",
	 	   state:"navigation", 
	 	   view:""
	    }, { 
	 	   index:"1", 
	 	   title:"Import Data", 
	 	   description:"An easy to use tool for data import, organized into VREs. Just drag & drop the data files you need and start adding new data in minutes.", 
	 	   icon:"mdi-database-plus", 
	 	   disabled:"false",
	 	   size:"150px",
	 	   actionButtonLabel:"Continue",
	 	   href:"",
	 	   state:"import", 
	 	   view:""
	    }, { 
	 	   index:"2", 
	 	   title:"My Favorites", 
	 	   description:"Data navigation might require a few steps to be achieved. However, as soon as important findings are achieved, they can be stored for direct feature access.", 
	 	   icon:"mdi-heart", 
	 	   disabled:"false",
	 	   size:"150px",
	 	   actionButtonLabel:"Continue",
	 	   href:"",
	 	   state:"", 
	 	   view:""
	    }
	];
	
	$scope.goToState = function(stateName) {
		$state.go(stateName, {});
	}
	
	// Toggles SidePanel
	$scope.toggleInfo = buildToggler('rightInfo');
	
	function buildToggler(componentId) {
      return function() {
        $mdSidenav(componentId).toggle();
      };
    }
	
}]);