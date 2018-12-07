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
 * The main angularJS service to hold the factories regarding security
 * 
 * @author Vangelis Kritsotakis
 */

angular.module('app.securityServices', [])

.factory('authenticationService', function($http, $timeout, $q, $rootScope, $cookies, $sessionStorage) {
	
	// To hold the currentUser
	var currCredentials;
	var userProfile;

	return {
		login : function(username, password) {

			return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/login',
				'method' : 'Get',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : {
					'username' : username,
					'pwd' : password
				}
			}).then(function (response) {				
				currCredentials = response.data;
				$sessionStorage.currCredentials = currCredentials;
				$sessionStorage.authenticated = true;
				return response.data;
			},function (error) {
				alert("err: " + err);
			});

		},
		
		loginMFA : function(username, password) {

			return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/loginmfa',
				'method' : 'Get',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : {
					'username' : username,
					'pwd' : password
				}
			}).then(function (response) {				
				currCredentials = response.data;
				$sessionStorage.currCredentials = currCredentials;
				$sessionStorage.authenticated = true;
				return response.data;
			},function (error) {
				alert("err: " + err);
			});

		},
		
		loginMFACode : function(token, code) {

			return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/loginmfacode',
				'method' : 'Get',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : {
					'token' : token,
					'code' : code
				}
			}).then(function (response) {				
				currCredentials = response.data;
				$sessionStorage.currCredentials = currCredentials;
				$sessionStorage.authenticated = true;
				return response.data;
			},function (error) {
				alert("err: " + err);
			});

		},
		
		logout : function(authendicationToken) {

			return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/logout',
				'method' : 'Get',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : {
					'token' : authendicationToken
				}
			}).then(function (response) {				
				$sessionStorage.$reset();
				return response.data;
			},function (error) {
				alert("err: " + err);
			});

		},
		
		// Clears credentials from stored session
		clearCredentials : function() {
			// Clears everything from stored session
			$sessionStorage.$reset();
        },
        
        // Get it from session
        getCurrentCredentials: function () {
        	currCredentials = $sessionStorage.currCredentials;
            if (currCredentials) {
                return $q.when(currCredentials);
            } else {
                return $q.reject("NO USER");
            }
        },
        
        // Get it from session
        getCredentials: function () {
        	return $sessionStorage.currCredentials;
        },
        
        // Get it from session
        getUserProfile: function () {
        	return $sessionStorage.userProfile;
        },
        
        retrieveUserProfile: function (token, username) {
        	return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/getprofile',
				'method' : 'Get',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : {
					'token' : token,
					'userLogin' : username
				}
			}).then(function (response) {
				userProfile = response.data;
				$sessionStorage.userProfile = userProfile;
				return response;
			},function (error) {
				alert("err: " + err);
			});
        },
        
        retrieveAllUserProfiles: function (token) {
        	return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/getprofiles',
				'method' : 'Get',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'params' : {
					'token' : token
				}
			}).then(function (response) {
				return response;
			},function (error) {
				alert("err: " + err);
			});
        },
        
        // Get it from session
        isAuthenticated: function () {
        	return $sessionStorage.authenticated;
        },
        
        
        register: function(registration) {

			return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/createprofile',
				'method' : 'POST',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				//'data' : registration
				'data' : {},
				'params' : registration
				/*
				{
					'userid' : registration.userid,
					'name' : registration.name,
					'email' : registration.email,
					'organization' : registration.organization,
					'role' : registration.role,
					'password' : registration.password
				}
				*/
			}).then(function (response) {
				return response.data;
			},function (error) {
				alert("err: " + err);
			});

		},
        
        updateProfile: function(token, userProfile) {
        	
        	// Adding token
        	userProfile.token = token;
        	
			return $http({
				'url' : 'http://v4e-lab.isti.cnr.it:8080/NodeService/user/updateprofile',
				'method' : 'POST',
				
				'headers' : {
					'Content-Type' : 'application/json'
				},
				'data' : {},
				'params' : userProfile
			}).then(function (response) {
				return response;
			},function (error) {
				alert("err: " + err);
			});

		},
		
		getPortalState: function () {

            return $http({
                'url': '/retrieve_portal_state',
                'method': 'POST',
                'headers': {
                    'Content-Type': 'application/json'
                }
            }).then(function (success) {
                return success;
            }, function (error) {
                //error code
            });

        }
        
	}
});