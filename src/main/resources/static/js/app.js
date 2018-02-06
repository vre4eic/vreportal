/**
 * The angularJS application for handling the front-End / UI of the demo
 * 
 * @author Vangelis Kritsotakis
 * @author Ioannis Chrysakis
 */

var app = angular.module('app', ['ngResource','ui.bootstrap', 'ngMessages',
                                 'ngAnimate', 'ui.codemirror', 
                                 'app.mainServices', 'app.securityServices', 
                                 'app.shareDataAmongControllersServices', 
                                 'ngTagsInput', 'ngMaterial', 'ivh.treeview', 
                                 'ui.router', 'ngCookies', 'ngStorage',
                                 'ngSanitize', 'ngMaterial']);//'ngRoute', 'openlayers-directive'
/*
app.run(['$rootScope', '$state', '$stateParams', function($rootScope, $state, $stateParams) {
	$rootScope.$state = $state;
	$rootScope.$stateParams = $stateParams;
}])
*/
/*
app.run(['$rootScope', '$state', '$stateParams', function($rootScope, $state, $stateParams) {

	$rootScope.$on('$stateChangeSuccess', function(event, toState, toParams, fromState, fromParams) {
		alert("ff");
		$rootScope.currentTab = toState.data.selectedTab;
	})
}])
*/
app.filter('encodeURIComponent', function() {
    return function(input) {
        return encodeURIComponent(encodeURIComponent(input));
    }; 
})

app.filter('decodeURIComponent', function(input) {
    return decodeURIComponent(input);
});

app.config(function($stateProvider, $urlRouterProvider, $mdThemingProvider, $httpProvider, ivhTreeviewOptionsProvider) {
	
	//Enable cross domain calls
    $httpProvider.defaults.useXDomain = true;
	
    function checkForAuthenticatedUser(authenticationService, $state) {
        return authenticationService.getCurrentCredentials()
        .then(function (_credentials) {
            // if resolved successfully return a credentials object 
            return _credentials;
        }, function (_error) {
            $state.go('login');
        })
    }
    
    
	// Another theme
	$mdThemingProvider.theme('altTheme')
    	.primaryPalette('green') 	// specify primary color
    	.accentPalette('pink') 		// specify accent color
    	.warnPalette('orange') 	// specify warn color
	
	$urlRouterProvider
		.otherwise('/welcome');
	
	$stateProvider
		.state('login', {
			url: '/login',
			templateUrl: 'views/login.html',
        	controller: 'loginCtrl'
		})
		.state('registration', {
			url: '/registration',
			templateUrl: 'views/registration.html',
        	controller: 'registrationCtrl'
		})
		.state('welcome', {
			url: '/welcome',
			templateUrl: 'views/welcome.html',
        	controller: 'welcomeCtrl'
		})
		.state('navigation', {
			url: '/navigation',
			templateUrl: 'views/navigation.html',
        	controller: 'navigationCtrl'
		})
		.state('favorites', {
			url: '/favorites',
			templateUrl: 'views/favorites.html',
        	controller: 'favoritesCtrl'
		})
		.state('import', {
			url: '/import',
			templateUrl: 'views/import.html',
        	controller: 'importCtrl'
		})
		.state('tabs', {
    		abstract: true,
    		url: '/tabs',
    		templateUrl: 'views/tabs.html',
    		controller: 'tabCtrl',
            resolve : {
                resolvedUser : checkForAuthenticatedUser
            }
    	})
    	.state('tabs.queryTab', {
	        url: '/query',
	        data: {
	            'selectedTab' : 0
	        },
	        views: {
                'tab-query': {
                	templateUrl: 'views/query.html',
        	        controller: 'queryCtrl'
                }
            },resolve: {
                currentUser: function(resolvedUser){
                    return resolvedUser;
                }
            }
	        
	    })
	    .state('tabs.geoQueryTab', {
	        url: '/geoQuery',
	        data: {
	            'selectedTab' : 1
	        },
	        views: {
                'tab-geoQuery': {
                	templateUrl: 'views/geoQuery.html',
        	        controller: 'geoQueryCtrl'
                }
            },resolve: {
                currentUser: function(resolvedUser){
                    return resolvedUser;
                }
            }
	        
	    })
	    .state('tabs.advancedQueryTab', {
	        url: '/advancedQuery',
	        data: {
	            'selectedTab' : 2
	        },
	        views: {
                'tab-advancedQuery': {
                	templateUrl: 'views/advancedQuery.html',
        	        controller: 'advancedQueryCtrl'
                }
            },resolve: {
                currentUser: function(resolvedUser){
                    return resolvedUser;
                }
            }
	        
	    })
	    .state('tabs.importTab', {
	        url: '/import',
	        data: {
	            'selectedTab' : 3
	        },
	        views: {
                'tab-import': {
                	templateUrl: 'views/import.html',
        	        controller: 'importCtrl'
                }
            },resolve: {
                currentUser: function(resolvedUser){
                    return resolvedUser;
                }
            }
	        
	    });
    	
	ivhTreeviewOptionsProvider.set({
		defaultSelectedState: false,
		validate: true,
		expandToDepth: -1,
		twistieCollapsedTpl: '<md-icon md-font-icon="fa fa-chevron-right" style="font-size:16px"></md-icon>',
		twistieExpandedTpl: '<md-icon md-font-icon="fa fa-chevron-down" style="font-size:16px"></md-icon>',
		twistieLeafTpl: '<span style="cursor: default;">&#8192;&#8192;&#8192;&#8192;&#8192;</span>'
	});
	
});

