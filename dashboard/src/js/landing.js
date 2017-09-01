
var kuona = angular.module('kuona.dashboard', [
  'angular-websocket',
  'ui.router',                    // Routing
  'oc.lazyLoad',                  // ocLazyLoad
  'ui.bootstrap',                 // Ui Bootstrap
  'ngResource'
]);

function MainController($scope, $http) {
  this.helloText = 'Welcome to Kuona';
  this.descriptionText = 'Use the navigation to look around :)';

  $scope.currentDate = new Date();
  $scope.repository_count = "[loading]";
  $scope.vcs_count = "[loading]";
  $scope.repositoriesFound = [];
  $scope.buildTools = []

  $http.get('/api/build/tools').then(function(res) {
    $scope.buildTools = [];
    var data = res.data.buckets;

    var colorIndex = 0;
    for (var k in data) {
      var item = data[k];
      $scope.buildTools.push({"label": item.key, "color": colors[colorIndex++], "value": item.doc_count });
    }
    barChart(document.getElementById("buildToolCanvas"), "Module Count", "Build tool counts for identified modules", $scope.buildTools);
  });

  $http.get('/api/repositories/count').then(function(res) {
    $scope.repository_count = res.data.count;
  });

  $http.get('/api/metrics/vcs/count').then(function(res) {
    $scope.vcs_count = res.data.count;
  });

  $http.get('/api/metrics/code/count').then(function(res) {
    $scope.code_metric_count = res.data.count;
  });
};

kuona.controller('MainController',['$scope', '$http', MainController]);

