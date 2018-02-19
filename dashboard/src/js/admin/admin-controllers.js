/**
 * INSPINIA - Responsive Admin Theme
 *
 */

/**
 * MainCtrl - controller
 */
function MainCtrl($scope, $http) {
  this.userName = 'Kuona Admin';
  this.helloText = 'Kuona Administration';
  this.descriptionText = 'Manage your Kuona metrics server here. Add and remove resources for analysis.';

  $scope.info = {};
  $scope.indices = [];

  $http.get('/api/info').then(function (res) {
    $scope.info = res.data;
  });

  $http.get("/api/indices").then(function (res) {
    $scope.indices = res.data.indices;
  });
}

function NewGithubRepoController($scope, $http) {
  $scope.username = null;
  $scope.username_default = "kuona";
  $scope.repository = null;
  $scope.respsitory_default = "kuona-project";
  $scope.testResponse = {"status": "unknown"};
  $scope.gh = null;

  $scope.hasResponse = function () {
    return $scope.testResponse && $scope.testResponse.status;
  };

  $scope.testRepo = function () {
    $scope.gh = null;
    var request = {
      "source": "github-project",
      "username": $scope.username,
      "repository": $scope.repository
    };
    $http.post("/api/repositories/test", request).then(function (res) {
      $scope.testResponse = res.data;

      if ($scope.testResponse.github) {
        $scope.gh = $scope.testResponse.github;
      }
    });
  };

  $scope.addRepo = function () {
    $scope.gh = null;
    var request = {
      "source": "github-project",
      "username": $scope.username,
      "repository": $scope.repository
    };
    $http.post("/api/repositories", request).then(function (res) {
      $scope.testResponse = res.data;

      if ($scope.testResponse.github) {
        $scope.gh = $scope.testResponse.github;
      }
    });
  };
}


function NewJenkinsServerController($scope, $http) {
  $scope.server_url = "";
  $scope.server_url_placeholder = 'http://build.codekata.ninja';
  $scope.username = null;
  $scope.password = null;
  $scope.api_response = {};

  $scope.addJenkins = function () {

    var request = {
      "collector": "jenkins",
      "config": {
        "url": $scope.server_url,
        "username": $scope.username,
        "password": $scope.password
      }
    };

    $http.post("/api/collectors", request).then(function (res) {
      $scope.api_response = res;
    });

  };
}

function BuildServersController($scope, $http) {
  $scope.build_servers = [];

  $http.get("/api/collectors").then(function (res) {
    $scope.build_servers = res.data.items;
  });
}

angular
  .module('kuona-admin')
  .controller('MainCtrl', ['$scope', '$http', MainCtrl])
  .controller('NewGithubRepoController', ['$scope', '$http', NewGithubRepoController])
  .controller('NewJenkinsServerController', ['$scope', '$http', NewJenkinsServerController])
  .controller('BuildServersController', ['$scope', '$http', BuildServersController]);
