function MainController($scope, $http) {
  this.helloText = 'Welcome to Kuona';
  this.descriptionText = 'Use the navigation to look around :)';
  $scope.currentDate = new Date();
  $scope.repository_count = "[loading]";
  $scope.vcs_count = "[loading]";
  $scope.repositoriesFound = [];
  
  $http.get('/api/repositories/count').then(function(res) {
    $scope.repository_count = res.data.count;
  });
  
  $http.get('/api/metrics/vcs/count').then(function(res) {
    $scope.vcs_count = res.data.count;
  });

  $http.get('/api/metrics/code/count').then(function(res) {
    $scope.code_metric_count = res.data.count;
  });

  $scope.repoSearch = function(term) {
    console.log("Searching for " + term);
    $http.get("/api/repositories?search=" + term).then(function(res){
      $scope.repositoriesFound = res.data;
    });
  }

  $scope.repoSearch("");
};

kuona.controller('MainController',['$scope', '$http', MainController]);


var environmentDashboardController = function ($scope, $http) {
  $scope.environments = [];
  $scope.grid = { rows: [] }
  
  $scope.enhance = function(env) {
    if (env.comment) {
      env.comment.colour = "primary";
      if (env.comment.assessment == "Available") {
	env.comment.colour = "primary"
      }
      if (env.comment.assessment == "Warning") {
	env.comment.colour = "warning"
      }
      if (env.comment.assessment == "Unavailable") {
	env.comment.colour = "danger"
      }
    }
    return env;
  }

  $scope.process = function(environments) {
      for (var i = 0; i < environments.length; i++) {
	$scope.enhance(environments[i].environment);
      }
  }
    
  $scope.refresh = function() {
    $http.get('/api/environments').then(function (res) {
      $scope.environments = res.data.environments;
      
      for (var i = 0; i < $scope.environments.length; i++) {
	$scope.enhance($scope.environments[i].environment);
      }
    });
  }

  $scope.comment = function(assessment, env) {
    var url = "/api/environments/" + env.environment.name + "/comments";
    
    $http.post(url, $scope.comment_request(env, assessment)).then(function(res) {
      env.environment = res.data.environment;
      $scope.enhance(env.environment);
    });
  }
  
  $scope.comment_request = function(env, assessment) {
    var req = {};
    req.comment = {};
    req.comment.assessment = assessment;;
    req.comment.message = env.message;
    req.comment.username = env.username;
    return req
  }

  $scope.refresh();
  
  $scope.markOffline = function(env) {
    $scope.comment("Unavailable", env)
  }
  
  
  $scope.markCaution = function(env) {
    $scope.comment("Warning", env);
  }
  
  $scope.markOnline = function(env, cell) {
    $scope.comment("Available", env);
  }
};

kuona.controller("environmentDashboardController", ['$scope', '$http', environmentDashboardController]);


kuona.controller('ModalEnvironmentController', function ($uibModal, $log, $document) {
  var $ctrl = this;
  $ctrl.environment = { name: null };
//  $ctrl.environment.name = undefined;

  $ctrl.animationsEnabled = true;

  $ctrl.open = function (size, parentSelector) {
    var parentElem = parentSelector ? angular.element($document[0].querySelector('.modal-demo ' + parentSelector)) : undefined;
    var modalInstance = $uibModal.open({
      animation: $ctrl.animationsEnabled,
      ariaLabelledBy: 'modal-title',
      ariaDescribedBy: 'modal-body',
      templateUrl: 'myModalContent.html',
      controller: 'ModalInstanceCtrl',
      controllerAs: '$ctrl',
      size: size,
      appendTo: parentElem,
      resolve: {
        environment: function() { return $ctrl.environment; }
      }
    });

    modalInstance.result.then(function (selectedItem) {
      $ctrl.selected = selectedItem;
    }, function () {
      $log.info('Modal dismissed at: ' + new Date());
    });
  };

  $ctrl.openComponentModal = function () {
    var modalInstance = $uibModal.open({
      animation: $ctrl.animationsEnabled,
      component: 'modalComponent',
      resolve: {
        environment: function() { return $ctrl.environment; }
      }
      });
                                       
     modalInstance.result.then(function (selectedItem) {
       $ctrl.selected = selectedItem;
     }, function () {
       $log.info('modal-component dismissed at: ' + new Date());
     });
  };

  $ctrl.openMultipleModals = function () {
    $uibModal.open({
      animation: $ctrl.animationsEnabled,
      ariaLabelledBy: 'modal-title-bottom',
      ariaDescribedBy: 'modal-body-bottom',
      templateUrl: 'stackedModal.html',
      size: 'sm',
      controller: function($scope) {
        $scope.name = 'bottom';  
      }
    });

    $uibModal.open({
      animation: $ctrl.animationsEnabled,
      ariaLabelledBy: 'modal-title-top',
      ariaDescribedBy: 'modal-body-top',
      templateUrl: 'stackedModal.html',
      size: 'sm',
      controller: function($scope) {
        $scope.name = 'top';  
      }
    });
  };

  $ctrl.toggleAnimation = function () {
    $ctrl.animationsEnabled = !$ctrl.animationsEnabled;
  };
});

// Please note that $uibModalInstance represents a modal window (instance) dependency.
// It is not the same as the $uibModal service used above.

var modalInstanceController = function ($uibModalInstance, $http, environment) {
  var $ctrl = this;
  $ctrl.environment= environment;
  
  $ctrl.ok = function () {
    console.log("Environment name " + $ctrl.environment.name);
    var url = "/api/environments";
    env = {
      environment: {
        name: $ctrl.environment.name,
        status: $ctrl.environment.status,
        status_url: $ctrl.environment.status_url,
        version: $ctrl.environment.version
      }
    };
    $http.post(url, env).then(function(res) {
      console.log("New environment response" + res.data);
    });
    $uibModalInstance.close($ctrl.environment.name);
  };
  
  $ctrl.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };
}

kuona.controller('ModalInstanceCtrl', ['$uibModalInstance','$http', modalInstanceController]);

kuona.controller('ModalStatusController', function ($uibModal, $log, $document) {
  var $status = this;
  $status.environment = { name: null };

  $status.animationsEnabled = true;

  console.log("Defining open function");
  $status.open = function (size, parentSelector) {
    console.log("Status modal controller open() called");
    var parentElem = parentSelector ? angular.element($document[0].querySelector('.modal-status ' + parentSelector)) : undefined;

    var modalInstance = $uibModal.open({
      animation: $status.animationsEnabled,
      ariaLabelledBy: 'modal-title',
      ariaDescribedBy: 'modal-body',
      templateUrl: 'modalStatus.html',
      controller: 'ModalStatusInstanceCtrl',
      controllerAs: '$status',
      size: size,
      appendTo: parentElem,
      resolve: {
        environment: function() { return $status.environment; }
      }
    });

    modalInstance.result.then(function (selectedItem) {
      $status.selected = selectedItem;
    }, function () {
      $log.info('Modal dismissed at: ' + new Date());
    });
  };

  $status.openComponentModal = function () {
    var modalInstance = $uibModal.open({
      animation: $status.animationsEnabled,
      component: 'modalComponent',
      resolve: {
        environment: function() { return $status.environment; }
      }
      });
                                       
     modalInstance.result.then(function (selectedItem) {
       $status.selected = selectedItem;
     }, function () {
       $log.info('modal-component dismissed at: ' + new Date());
     });
  };

  $status.openMultipleModals = function () {
    $uibModal.open({
      animation: $status.animationsEnabled,
      ariaLabelledBy: 'modal-title-bottom',
      ariaDescribedBy: 'modal-body-bottom',
      templateUrl: 'stackedModal.html',
      size: 'sm',
      controller: function($scope) {
        $scope.name = 'bottom';  
      }
    });

    $uibModal.open({
      animation: $status.animationsEnabled,
      ariaLabelledBy: 'modal-title-top',
      ariaDescribedBy: 'modal-body-top',
      templateUrl: 'stackedModal.html',
      size: 'sm',
      controller: function($scope) {
        $scope.name = 'top';  
      }
    });
  };

  $status.toggleAnimation = function () {
    $status.animationsEnabled = !$status.animationsEnabled;
  };
});

// Please note that $uibModalInstance represents a modal window (instance) dependency.
// It is not the same as the $uibModal service used above.

var modalStatusInstanceController = function ($uibModalInstance, $document, environment) {
  var $status = this;
  $status.environment= environment;
  $status.ok = function () {
    console.log("Status done");

    $uibModalInstance.close($status.environment.name);
  };
  
  $status.cancel = function () {
    $uibModalInstance.dismiss('cancel');
  };
}

kuona.controller('ModalStatusInstanceCtrl', ['$uibModalInstance','$http', modalStatusInstanceController]);

