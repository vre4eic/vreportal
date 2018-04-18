/**
 * AngularJS controller for handling user's profile interactions
 * 
 * @author Vangelis Kritsotakis
 */
app.controller("userProfileCtrl", ['$state', '$scope', '$timeout', '$parse', '$sessionStorage', 'authenticationService', 'modalService', 'queryService', '$mdSidenav', '$mdToast', '$http', '$log', '$mdDialog', '$mdToast', 
                                  function($state, $scope, $timeout, $parse, $sessionStorage, authenticationService, modalService, queryService, $mdSidenav, $mdToast, $http, $log, $mdDialog, $mdToast) {
	
	$scope.headingTitle = "My Profile";
	
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
	
	$scope.goToHomeView = function() {
		$state.go('welcome', {});
	}
	
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
	
	$scope.roles = [
        {label: "Researcher", value: "RESEARCHER"},
        {label: "Operator", value: "OPERATOR"},
        {label: "Administrator", value: "ADMIN"},
        {label: "Controller", value: "CONTROLLER"},
    ];
	
	// Flag determining whether this is a view or edit mode
	$scope.editMode = false;
	
	// Called to enable edit mode
	$scope.enableEditProfileMode = function() {
		$scope.editMode = true;
	}
	
	// Called to disable edit mode (used as cancel)
	$scope.disableEditProfileMode = function() {
		$scope.editMode = false;
	}
	
	$scope.userProfile = {};

	// Retrieving the list of favoriteModels of the user
	function retrieveUserProfile(username) {
		
		var model = {username: username};
				
		var modalOptions = {
			headerText: 'Loading Please Wait...',
			bodyText: 'Retrieving data...'
		};
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
		
		authenticationService.retrieveUserProfile($scope.credentials.token, username)
        .then(function (profileResponse) {
    		
			if(profileResponse.status == '200') {
				if (profileResponse.data.name !== null) {
    				console.log("profileResponse", profileResponse.data);
    				$sessionStorage.userProfile = profileResponse.data; // Store in session again
    				$scope.userProfile = profileResponse.data; // Store in scope for using it now
    	        } else {
    	        	$scope.alerts.push({type: 'danger-funky', msg: profileResponse.data.message + "! Cannot retrieve user's profile. "});	        	            
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
			//modalInstance.close();
			
		}, function (error) {
			//modalInstance.close();
			$scope.message = 'There was a network error. Try again later.';
			alert("failure message: " + $scope.message + "\n" + JSON.stringify({
				data : error
			}));
		}).finally(function(){
			$timeout(function(){
				modalInstance.close();
			});//, 500);
		});
		
	}
	
	// Get user's profile from session
	retrieveUserProfile($sessionStorage.userProfile.userId);
	
	
	// Updates the profile of user
	$scope.updateUserProfile = function() {
				
		// Pre-processing
		var tempUserProfile = angular.copy($scope.userProfile);
		
		// Delete confirmPassword just in case
		delete tempUserProfile.confirmPassword;
		
		// Due to bug at the e-VRE NodeService,
		// change parameter "userId" into "userid" (I to i)
		tempUserProfile.userid = tempUserProfile.userId;
		delete tempUserProfile.userId;
		
		var modalOptions = {
			headerText: 'Loading Please Wait...',
			bodyText: 'Updating user\'s profile...'
		};
		var modalInstance = modalService.showModal(modalDefaults, modalOptions);
		
		authenticationService.updateProfile($scope.credentials.token, tempUserProfile)
		.then(function (response) {
    		
			if(response.status == '200') {
    			console.log("Profile response: " + response.data);
    			
    			// Delete confirmPassword just in case
    			delete $scope.userProfile.confirmPassword;
    			
    			$scope.disableEditProfileMode();
    			
    			$sessionStorage.userProfile = $scope.userProfile; // Store regular profile (before pre-processing) in session again
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
			//modalInstance.close();
			$scope.message = 'There was a network error. Try again later.';
			$scope.showErrorAlert('Error', $scope.message + "\n" + error);
		}).finally(function(){
			$timeout(function(){
				modalInstance.close();
			});//, 500);
		});		
	}
	
	
	// Converting object to array in order to use orderBy filter on ng-repeat
	// To bypass the error "Error: 10 $digest() iterations reached. Aborting!"
	// the results of the function are cashed by using the Lo-Dash’s memoize function
	$scope.templateAry = _.memoize(function (tempItem) {
	    var ary = [];
	    angular.forEach(tempItem, function (val, key) {
	    	if(key != '$$hashKey' && key != 'queryModel' && key != 'favoriteId' && key != 'username') // $$hashKey is added by angular (don't need it)
	    		ary.push({key: key, val: val});
	    });
	    return ary;
	});
	
	
    
    
	
}]);