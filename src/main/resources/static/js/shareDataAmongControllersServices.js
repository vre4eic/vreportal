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
 * A angularJS service to share data among controllers
 * 
 * @author Vangelis Kritsotakis
 */

angular.module('app.shareDataAmongControllersServices', [])

.factory('homeStateConfirmService', function($http, $timeout, $q) {
	
	// To hold the currentUser
	var queryUnderConstruction = false;

	return {
        isQueryUnderConstruction: function () {
            return queryUnderConstruction;
        },
        setQueryUnderConstruction: function (someBoolean) {
        	queryUnderConstruction = someBoolean;
        }
    };
    
});