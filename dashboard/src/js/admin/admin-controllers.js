function MainCtrl($scope, $http) {
  this.userName = 'Kuona Admin';
  this.helloText = 'Kuona Administration';
  this.descriptionText = 'Manage your Kuona metrics server here. Add and remove resources for analysis.';

  $scope.info = {};
  $scope.indices = [];

  $scope.rebuildIndex = function (name) {
    $http.post('/api/indices/' + name + "/rebuild").then(function () {
      $scope.refresh();
    });
  };

  $scope.unlock = function (name) {
    $http.post('/api/indices/' + name + "/unlock").then(function () {
      $scope.refresh();
    });
  };

  $scope.deleteIndex = function (name) {
    $http.delete("/api/indicies/" + name).then(function () {
      $scope.refresh();
    });
  };

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
    let request = {
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
    let request = {
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

    let request = {
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
    let request = {
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
    let request = {
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

function NewSearchCodeServerController($scope, $http) {
  $scope.server_url = null;
  $scope.server_url_placeholder = 'http://searchcode.com';
  $scope.api_key = {
    private: null,
    public: null
  };

  $scope.api_response = null;

  $scope.addSearchCodeServer = function () {
    console.log("Search Code")
  };

  $scope.testSearchCodeServer = function () {
    let request = {
      integration: {
        type: "searchcode",
        url: $scope.server_url,
        api_key: $scope.api_key
      }
    };
    $http.post("/api/integration/test", request).then(function (res) {
      $scope.api_response = res.data;
    });
  };
}

function commaListToArray(text, sepearator) {
  let elements = text.split(sepearator);
  let result = [];

  for (let i = 0; i < elements.length; i++) {
    result.push(elements[i].trim())
  }
  return result;
}

function NewServerHealthCheckController($scope, $http) {
  $scope.healthcheck = {
    tags: "",
    endpoints: "",
    type: "HTTP_GET"
  };
  $scope.api_response = null;
  $scope.healthchecks = [];
  $scope.healthcheck_snapshots = [];

  $scope.updateHealthChecks = () => {
    $http.get("/api/health-checks").then((res) => {
      $scope.healthchecks = res.data.health_checks;
    });
  };

  $scope.updateHealthCheckSnapshots = () => {
    $http.get("/api/health-checks/snapshots").then((res) => {
      $scope.healthcheck_snapshots = res.data.snapshots;
    });
  };

  $scope.addHealthChecks = () => {
    let request = {
      type: $scope.healthcheck.type,
      tags: commaListToArray($scope.healthcheck.tags, ','),
      endpoints: commaListToArray($scope.healthcheck.endpoints, '\n')
    };
    $http.post("/api/health-checks", request).then((res) => {
      $scope.api_response = res;
    });
  };

  $scope.deleteHealthCheck = (id) => {
    $http.delete("/api/health-checks/" + id).then(() => {
      $scope.updateHealthChecks();
    });
  };

  $scope.updateHealthChecks();
  $scope.updateHealthCheckSnapshots();
}

angular
  .module('kuona-admin')
  .filter('age', ageFilter)
  .controller('MainCtrl', ['$scope', '$http', MainCtrl])
  .controller('NewGithubRepoController', ['$scope', '$http', NewGithubRepoController])
  .controller('NewJenkinsServerController', ['$scope', '$http', NewJenkinsServerController])
  .controller('BuildServersController', ['$scope', '$http', BuildServersController])
  .controller('CrawlerController', ['$scope', '$http', CrawlerController])
  .controller('TfsCrawlerController', ['$scope', '$http', '$window', TfsCrawlerController])
  .controller('GitHubCrawlerController', ['$scope', '$http', '$window', GitHubCrawlerController])
  .controller('NewSearchCodeServerController', ['$scope', '$http', NewSearchCodeServerController])
  .controller('NewServerHealthCheckController', ['$scope', '$http', NewServerHealthCheckController])
;
