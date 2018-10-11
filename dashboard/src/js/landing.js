var kuona = angular.module('kuona.dashboard', [
  'angular-websocket',
  'ui.router',                    // Routing
  'oc.lazyLoad',                  // ocLazyLoad
  'ui.bootstrap',                 // Ui Bootstrap
  'ngResource'
]);

kuona.filter('elapsed', elapsedFilter);
kuona.filter('age', ageFilter);

function MainController($scope, $http) {
  this.helloText = 'Welcome to Kuona';
  this.descriptionText = 'Use the navigation to look around :)';

  $scope.currentDate = new Date();
  $scope.repository_count = "[loading]";
  $scope.vcs_count = "[loading]";
  $scope.buildTools = [];
  $scope.info = {};
  $scope.collector_activity = [];
  $scope.code_metric_count = 0;
  $scope.code_snapshot_count = 0;
  $scope.query = null;
  $scope.query_response = "";
  $scope.search = function () {
    console.log("Query for " + $scope.query);
    $http.post('api/chat', {query: $scope.query}).then(function (res) {
      $scope.query_response = res.data.response.message;
    });
  };
  $http.get('/api/build/tools').then(function (res) {
    $scope.buildTools = [];
    var data = res.data.buckets;

    var colorIndex = 0;
    for (var k in data) {
      var item = data[k];
      $scope.buildTools.push({"label": item.key, "color": colors[colorIndex++], "value": item.doc_count});
    }
    barChart(document.getElementById("buildToolCanvas"), "Module Count", "Build tool counts for identified modules", $scope.buildTools);
  });

  $http.get('/api/repositories/count').then(function (res) {
    $scope.repository_count = res.data.count;
  });

  $http.get('/api/metrics/commits/count').then(function (res) {
    $scope.vcs_count = res.data.count;
  });

  $http.get('/api/metrics/code/count').then(function (res) {
    $scope.code_metric_count = res.data.count;
  });

  $http.get('/api/metrics/snapshots/count').then(function (res) {
    $scope.code_snapshot_count = res.data.count;
  });

  $http.get('/api/info').then(function (res) {
    $scope.info = res.data;
  });

  $http.get('/api/collectors/activities').then(function (res) {
    $scope.collector_activity = res.data.items;
  });

}

kuona.controller('MainController', ['$scope', '$http', MainController]);
