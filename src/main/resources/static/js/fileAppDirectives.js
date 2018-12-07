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
 * Directive for the dropzone (used for file uploading)
 * 
 * @author Vangelis Kritsotakis
 * @author Ioannis Chrysakis
 */

app.directive('dropzone',[ function(){

    return function(scope, element, attrs) {
    	
    	var config, dropzone;

        config = scope[attrs.dropzone];

        // create a Dropzone for the element with the given options
        dropzone = new Dropzone(element[0], config.options);

        // bind the given event handlers
        angular.forEach(config.eventHandlers, function (handler, event) {
          dropzone.on(event, handler);
        });
    	
        scope.processDropzone = function() {
            dropzone.processQueue();
        };

        scope.resetDropzone = function() {
            dropzone.removeAllFiles();
        }
        
        scope.getDropzoneAcceptedFiles = function() {
            return dropzone.getAcceptedFiles().length;
        }
    }
    
}]);