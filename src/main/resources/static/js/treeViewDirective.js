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
 * Directive for the treeview (used for left side menu)
 * 
 * @author Vangelis Kritsotakis
 */


app.directive('mdBox', function() {
	return {
		restrict: 'AE',
		//require: '^ivhTreeview',
		template: [
		    '<span class="ascii-box">',
	        	'<span ng-show="node.selected" class="x"><md-checkbox aria-label="checked" ng-checked="true"></md-checkbox></span>',
	        	'<span ng-show="node.__ivhTreeviewIndeterminate" class="y"><md-checkbox aria-label="checked" ng-checked="false"></md-checkbox></span>',
	        	'<span ng-hide="node.selected || node.__ivhTreeviewIndeterminate"><md-checkbox aria-label="checked" ng-checked="false"></md-checkbox></span>',
	      	'</span>',
		].join(''),
		link: function(scope, element, attrs) {
		      element.on('click', function() {
		        scope.trvw.toggleSelected(scope.node);
		        //scope.$apply();
		      });
		}
	};
});

/*
app.directive('mdBox', function(ivhTreeviewMgr) {
	return {
		restrict: 'AE',
		//require: '^ivhTreeview',
		template: [
			'<span class="ascii-box">',
				'<span ng-show="node.selected" class="x"><md-checkbox style="min-height: 100%; line-height: 0" aria-label="checked" ng-checked="true"></md-checkbox></span>',
				'<span ng-show="node.__ivhTreeviewIndeterminate" class="y"><md-checkbox style="min-height: 100%; line-height: 0" aria-label="checked" ng-checked="false"></md-checkbox></span>',
				'<span ng-hide="node.selected || node.__ivhTreeviewIndeterminate"><md-checkbox style="min-height: 100%; line-height: 0" aria-label="checked" ng-checked="false"></md-checkbox></span>',
			'</span>',
		].join(''),
		link: function(scope, element, attrs) {
			element.on('click', function() {
				//ivhTreeviewMgr.select(stuff, scope.node, !scope.node.selected);
				ivhTreeviewMgr.select(scope.node, !scope.node.selected);
				scope.trvw.toggleSelected(scope.node);
				//scope.trvw.select(scope.node);
				
				console.log("status: " + scope.node.selected);
				scope.$apply();
			});
		}
	};
});
*/