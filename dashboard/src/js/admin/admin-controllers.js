function MainCtrl($scope, $http) {
  this.userName = 'Kuona Admin';
  this.helloText = 'Kuona Administration';
  this.descriptionText = 'Manage your Kuona metrics server here. Add and remove resources for analysis.';

  $scope.info = {};
  $scope.indices = [];

  $scope.rebuildIndex = function (name) {
    $http.delete("/api/indicies/" + name).then(function (res) {
      $scope.refresh();
    });
  }

  $scope.refresh = function () {

    $http.get('/api/info').then(function (res) {
      $scope.info = res.data;
    });

    $http.get("/api/indices").then(function (res) {
      $scope.indices = res.data.indices;
    });
  };

  $scope.refresh();
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
      "collector_type": "BUILD",
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
  $scope.collectors = [];

  $scope.refresh = function () {
    $http.get("/api/collectors?collector-type=BUILD").then(function (res) {
      $scope.collectors = res.data.items;
    });
  };

  $scope.deleteCollector = function (id) {
    console.log("Delete collector" + id);
    $http.delete("/api/collectors/" + id).then(function () {
      $scope.refresh();
    });
    $scope.refresh();
  };
  $scope.refresh();
}

function CrawlerController($scope, $http) {
  $scope.collectors = [];

  $scope.refresh = function () {
    $http.get("/api/collectors?collector-type=VCS").then(function (res) {
      $scope.collectors = res.data.items;
    });
  };

  $scope.deleteCollector = function (id) {
    console.log("Delete collector" + id);
    $http.delete("/api/collectors/" + id).then(function () {
      $scope.refresh();
    });
    $scope.refresh();
  };

  $scope.refresh();
}

function GitHubCrawlerController($scope, $http, $window) {
  $scope.user_org_name = "";
  $scope.password = "";
  $scope.username = "";

  $scope.addGithub = function () {
    var request = {
      "collector_type": "VCS",
      "collector": "GitHubOrg",
      "config": {
        "org": $scope.user_org_name,
        "username": $scope.username,
        "token": $scope.password
      }
    };

    $http.post("/api/collectors", request).then(function (res) {
      $scope.api_response = res;
      $window.location.href = '/admin/index.html';
    });
  };
}

function TfsCrawlerController($scope, $http, $window) {
  $scope.tfs_org_name = "";
  $scope.token = "";
  $scope.tfs_url = "";

  $scope.orgChange = function () {
    $scope.tfs_url = "https://" + $scope.tfs_org_name + ".visualstudio.com/";
  };

  $scope.addTfsServer = function () {
    var request = {
      "collector_type": "VCS",
      "collector": "TFS",
      "config": {
        "org": $scope.tfs_org_name,
        "token": $scope.token
      }
    };

    $http.post("/api/collectors", request).then(function (res) {
      $scope.api_response = res;
      $window.location.href = '/admin/index.html';
    });
  };

  $scope.orgChange();
}


angular
  .module('kuona-admin')
  .controller('MainCtrl', ['$scope', '$http', MainCtrl])
  .controller('NewGithubRepoController', ['$scope', '$http', NewGithubRepoController])
  .controller('NewJenkinsServerController', ['$scope', '$http', NewJenkinsServerController])
  .controller('BuildServersController', ['$scope', '$http', BuildServersController])
  .controller('CrawlerController', ['$scope', '$http', CrawlerController])
  .controller('TfsCrawlerController', ['$scope', '$http', '$window', TfsCrawlerController])
  .controller('GitHubCrawlerController', ['$scope', '$http', '$window', GitHubCrawlerController])
;
