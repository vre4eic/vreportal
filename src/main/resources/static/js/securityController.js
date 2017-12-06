/**
 * The security angularJS controllers
 * 
 * @author Vangelis Kritsotakis
 */


app.directive('compareTo', function() { 
	return {
		require: "ngModel",
		scope: {
			otherModelValue: "=compareTo"
		},
		link: function(scope, element, attributes, ngModel) {
			ngModel.$validators.compareTo = function(modelValue) {
				return modelValue == scope.otherModelValue;
			};

			scope.$watch("otherModelValue", function() {
				ngModel.$validate();
			});
		}
	};
});

app.controller("loginCtrl", ['$scope', 'authenticationService', '$location', '$timeout', '$state', '$mdDialog', '$sessionStorage', 
                             function($scope, authenticationService, $location, $timeout, $state, $mdDialog, $sessionStorage) {
	$scope.headingTitle = "Login";
	
	// Alert (danger, warning, success)
	$scope.alerts = [];
	
	$scope.credentials = {};
	
	$scope.tempMFAToken = {};
	
	// Usd for Multifactor Authentication checkbox
	$scope.multifactorAuthenticatorActive = false;
	
	$scope.goToRegistration = function() {
		$state.go('registration', {});
	}
	
	$scope.login = function(ev) {
		//alert("username is: " + $scope.credentials.username + "and password is: " + $scope.credentials.password);
		$scope.credentials.dataLoading = true;
		
		// Regular Authentication
		if($scope.multifactorAuthenticatorActive == false) {
			authenticationService.login($scope.credentials.username, $scope.credentials.password)
			.then(function (response) {
				if (response.status == 'SUCCEED') {
					// Redirect to the tabs.queryTab
	                console.log("response", response.token);
	                // Retrieve User's profile
	                authenticationService.retrieveUserProfile(response.token, $scope.credentials.username)
	                .then(function (profileResponse) {
	        			if (profileResponse.name !== null) {
	        				console.log("profileResponse", profileResponse);
	        				$sessionStorage.userProfile = profileResponse;
	        				//updateUserProfile();
	        	        } else {
	        	        	$scope.alerts.push({type: 'danger-funky', msg: profileResponse.message + "! Cannot retrieve user's profile. "});	        	            
	        	        }
	        		},function (error){
	        			$scope.message = 'There was a network error. Try again later.';
	        			alert("failure message: There was a network error. Try again later");
	        		});
	                
	                $state.go('welcome', {});
	                
		        } else {
		        	$scope.alerts.splice(0); // Close alerts
		        	$scope.alerts.push({type: 'danger-funky', msg: response.message + "! Authendication failed, please check your credentials and try again. "});
		            $scope.credentials.dataLoading = false;
		            
		        }
			},function (error){
				$scope.message = 'There was a network error. Please try again later.';
				alert("There was a network error. Please try again later");
			});
		
		}
		
		// Multifactor Authentication
		else {
			authenticationService.loginMFA($scope.credentials.username, $scope.credentials.password)
			.then(function (response) {
				if (response.status == 'SUCCEED') {
					//$scope.tempMFAToken = response.token;
					$scope.tempMFAToken = response.token;
	                console.log("response", response.token);
	                $scope.showMFACodePrompt(ev);
		        } else {
		        	$scope.alerts.splice(0); // Close alerts
		        	$scope.alerts.push({type: 'danger-funky', msg: response.message + "! Authendication failed, please check your credentials and try again. "});
		            $scope.credentials.dataLoading = false;
		            
		        }
			},function (error){
				$scope.message = 'There was a network error. Please try again later.';
				alert("There was a network error. Please try again later");
			});
			
		}
		
	}
	
	$scope.showMFACodePrompt = function(ev) {
		
	    // Appending dialog to document.body to cover sidenav in docs app
		var htmlContent = 'In a few seconds you will receive some <code style="color:#106CC8;background: rgba(0,0,0,0.065);">code</code> on your mobile phone through the Telegram application.'
		$mdDialog.show({
			scope: $scope,
			preserveScope: true,
		    controller: 'mfaDialogController',
		    templateUrl: 'views/dialog/mfaDialog.tmpl.html',
		    parent: angular.element(document.body),
		    targetEvent: ev
		});
		
	  };
	  	  
} ]);

app.controller("mfaDialogController", ['$scope', 'authenticationService', '$mdDialog', '$state', 
                                   function($scope, authenticationService, $mdDialog, $state) {
	
	$scope.dialogAlerts = [];
	
	$scope.mfaCode = "";
	
	$scope.tempMFAToken;
	
	$scope.proceedWidthMFACode = function() {
		
		console.log("$scope.tempMFAToken: " + $scope.tempMFAToken);
		
		authenticationService.loginMFACode($scope.tempMFAToken, $scope.mfaCode)
		.then(function (response) {
			if (response.status == 'SUCCEED') {
                console.log("response", response.token);
                // Retrieve User's profile
                authenticationService.retrieveUserProfile(response.token, $scope.credentials.username)
	            .then(function (profileResponse) {
	            	if (profileResponse.name !== null) {
	            		console.log("profileResponse", profileResponse);
	            	} else {
	            		$scope.dialogAlerts.push({type: 'danger-funky', msg: profileResponse.message + "! Cannot retrieve user's profile. "});	        	            
	            	}
	            },function (error){
	            	$scope.message = 'There was a network error. Try again later.';
	            	alert("failure message: There was a network error. Try again later");
	            });
                
                $state.go('tabs.queryTab', {});
                
	        } else {
	        	$scope.dialogAlerts.splice(0); // Close alerts
	        	$scope.dialogAlerts.push({type: 'danger-funky', msg: response.message + "! Authendication failed, please check your credentials and try again. "});
	            $scope.credentials.dataLoading = false;
	        }
		},function (error){
			$scope.message = 'There was a network error. Please try again later.';
			alert("There was a network error. Please try again later");
		});
		
	}
	
	$scope.cancelMdfCode = function() {
		$mdDialog.hide();
	}
      	
} ]);


app.controller("beforeLoginCtrl", ['$scope', 'authenticationService', 'homeStateConfirmService', '$location', '$timeout', '$state', '$mdDialog', 
                             function($scope, authenticationService, homeStateConfirmService, $location, $timeout, $state, $mdDialog) {
	$scope.headingTitle = "Login";		
	$scope.userProfile = {};
	
	// Calling service to get the user's profile
	$scope.updateUserProfile = function() {
		$scope.userProfile = authenticationService.getUserProfile();
	}
	
	$scope.$watch($scope.updateUserProfile, function() {
    });
	
	$scope.sessionAuthenticatedStatus = function() {
	    return authenticationService.isAuthenticated();
	};

	$scope.logout = function() {
		//authenticationService.clearCredentials();
		authenticationService.logout(authenticationService.getCredentials().token)
		$state.go('login', {});
	}
	
	$scope.openTopMenu = function($mdMenu, ev) {
		originatorEv = ev;
		$mdMenu.open(ev);
	};
	
	$scope.goToHomeView = function(ev) {
		// Checks if there is any query currently under construction
		// and prompts message or just navigate to the new page
		if(homeStateConfirmService.isQueryUnderConstruction())
			confirmLeavingFromQueryBuilder(ev, 'welcome');
		else
			$state.go('welcome', {});
	}
	
	$scope.goToFavoritesView = function(ev) {
		// Checks if there is any query currently under construction
		// and prompts message or just navigate to the new page
		if(homeStateConfirmService.isQueryUnderConstruction())
			confirmLeavingFromQueryBuilder(ev, 'favorites');
		else
			$state.go('favorites', {});
	}
	
	// Ask confirmation before leaving query builder if you are there
	function confirmLeavingFromQueryBuilder(ev, state) {
		var confirm = $mdDialog.confirm()
			.title('Warning Message')
			.htmlContent('It seems that some query is under construction. </br>Are you sure you want to leave this page?')
			.ariaLabel('Confirmation')
			.targetEvent(ev)
			.ok('Yes I want to leave')
			.cancel('No, stay here');
		
		$mdDialog.show(confirm).then(function() { // OK
			homeStateConfirmService.setQueryUnderConstruction(false);
			$state.go(state, {});
		}, function() { // Cancel
			// do nothing
		});
	}
	
} ]);

app.controller("registrationCtrl", ['$scope', 'authenticationService', '$timeout', '$state', 
                             function($scope, authenticationService, $timeout, $state) {
	
	$scope.headingTitle = "Registration Form";
	
	$scope.roles = [
        {label: "Researcher", value: "RESEARCHER"},
        {label: "Operator", value: "OPERATOR"},
        {label: "Administrator", value: "ADMIN"},
        {label: "Controller", value: "CONTROLLER"},
    ];
	
	$scope.registration = {};
	
	// Alert (danger, warning, success)
	$scope.alerts = [];
	/*
	$scope.register = function() {
		alert("dsdsd");
	}
	*/
	$scope.register = function() {
		
		// Register
		authenticationService.register($scope.registration)
		.then(function (response){
			if (response.status == 'SUCCEED') {
				$timeout(function () {
					// Redirect to the tabs.queryTab
	                $state.go('login', {});
	                $scope.alerts.splice(0); // Close alerts
	                $scope.message = 'User ' + $scope.registration.userid + ' was successfully registered!';
	            }, 2);
	        } else {
	        	$scope.alerts.push({type: 'danger-funky', msg: response.message});
	        }
		},function (error){
			$scope.alerts.splice(0); // Close alerts
			$scope.message = 'There was a network error. Please try again later.';
			//$scope.alerts.push({type: 'danger-funky', msg: message});
			alert("There was a network error. Please try again later");
		});
				
	}
	
	$scope.goToLogin = function() {
		$state.go('login', {});
	}
		
} ]);
