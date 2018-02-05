/**
 * The main angularJS controllers (handling tabs, the query submission and serverside paginator and the file importing
 * 
 * @author Vangelis Kritsotakis
 */
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
	
	$scope.namedGraphs = [];
	$scope.namedGraphTree = [];
	$scope.selectedCategory = {label: null, id: null};
	
	
	
	
	
	// Initializing All available named graphs
	function initNamedGraphs(newCase) {
		
		queryService.getAllNamegraphs().then(function (response) {
			if(response.status == '200') {
				// Holding the whole tree for future usage
				$scope.namedGraphTree = response.data;
				// response.data is an array of children. Each children is an array of named graphs
				for(var i=0; i<response.data.length; i++) {
					for(var j=0; j<response.data[i].children.length; j++) {
						$scope.namedGraphs.push(response.data[i].children[j]);
					}
				}
				console.log();
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
	
	initNamedGraphs();
	
	/**
     * Search for namedGraphs... use $timeout to simulate
     * remote dataservice call.
     */
	$scope.querySearch = function (query) {
		var results = query ? $scope.namedGraphs.filter( createFilterFor(query) ) : $scope.namedGraphs, deferred;
		return results;
	}
	
	/**
     * Create filter function for a query string
     */
    function createFilterFor(query) {
    	var lowercaseQuery = angular.lowercase(query);

    	return function filterFn(state) {
    		return (angular.lowercase(state.label).indexOf(lowercaseQuery) === 0);
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
            acceptedFiles: '.rdfs,.rdf,.owl,.nt,.n3,.nt,.ntriples,.ttl,.jsonld,.trig,.trix',
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
                
                $scope.$apply(function() {
                    $scope.fileAdded = true;
                });
                                
            },
            'processing': function (file) {
            	
            	// In Case of new named graph
            	if($scope.selectedNamedGraph.id == null) {
            		
        		    
            		
            	}
            	
            	// Setting additional parameter dynamically 
            	// The parameter holds the content-type (i.e. application/rdf+xml)
            	if($scope.selectedFormat == 'Automatic') {
	            	this.options.params = { 
	            		'contentTypeParam': getContentTypeFromFileExtension(file.name.split('.').pop()),
	            		'namedGraphIdParam': $scope.selectedNamedGraph.id,
	            		'namedGraphLabelParam': $scope.selectedNamedGraph.label,
	            		'selectedCategoryLabel': $scope.selectedCategory.value,
	            		'selectedCategoryId': $scope.selectedCategory.id,
	            		'authorizationParam': $scope.credentials.token 
	            	};
            	}
            	else {
            		this.options.params = { 
	            		'contentTypeParam': $scope.selectedFormat, 
	            		'namedGraphIdParam': $scope.selectedNamedGraph.id,
	            		'namedGraphLabelParam': $scope.selectedNamedGraph.label,
	            		'selectedCategoryLabel': $scope.selectedCategory.value,
	            		'selectedCategoryId': $scope.selectedCategory.id,
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
            		initNamedGraphs();
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
    
    $scope.categories = [];
    
    // Initializing the list of categories of the named graphs 
    // with respect to id and label
    $scope.initCategories = function() {
    	if($scope.categories.length <=0) {
			angular.forEach($scope.namedGraphTree, function(value, key) {
				$scope.categories.push({value: value.label, id: value.id});
			});
    	}
	}
    
    $scope.closeNamedGraphSelectCategoryDialog = function() {
    	$scope.selectedCategory = {label: null, id: null};
    	$mdDialog.cancel();
    }
    
    // Called when the "Import Data" button is clicked
    $scope.uploadFile = function(ev) {
    	
    	// Checking if it is new namedGraph
    	if($scope.selectedNamedGraph == null) {
    		$scope.selectedNamedGraph = {id: null, label: $scope.searchText}
    		
    		// Prompting dialog to select category for the new named graph
    		$mdDialog.show({
	    		scope: $scope,
	    		templateUrl: 'views/dialog/selectNamedGraphCategory.tmpl.html', 
	    		parent: angular.element(document.body),
	    		targetEvent: ev,
	    		preserveScope: true,
	    		fullscreen: false // Only for -xs, -sm breakpoints.
	    	});
    		
    	}
    	
    	else {
    		$scope.processDropzone();
    	}
    };
    
    // Called when pressing the 'continue' button from the 
    // dialog shown when selecting category
    $scope.continueAfterSelectingCategory = function() {
    	// Close the dialog
    	$scope.closeNamedGraphSelectCategoryDialog();
    	// Start processing the files
    	$scope.processDropzone();
    }
    
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
    	else if (fileExtension == 'n3') {
    		return "text/rdf+n3";
    	}
        else if (fileExtension == 'nt' || fileExtension == 'ntriples') {
    		return "text/plain";
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

