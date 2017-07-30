var kuonaRepositories = angular.module('kuona.repositories', [
    'ui.bootstrap'
]);


kuonaRepositories.directive('kuonaBarChart', function() {
  console.log('Initializing directive');
  return {
    restrict: 'E',
    template: '<canvas class="build-tool-canvas" height="140"></canvas>',
    scope: {
      data: '='
//      title: '=',
//      description: '='
    },
    link: function (scope, element, attrs) {
      barChart(d3.select(element[0]).select('.buid-tool-canvas'), scope.title, scope.description, scope.data);
    }
  };
});

function RepositoriesController($scope, $http) {
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


  $scope.repoSearch = function(term) {
    $http.get("/api/repositories?search=" + term).then(function(res){
      $scope.repositoriesFound = res.data;
    });
  };

  $scope.repoSearch("");
}

kuonaRepositories.controller('RepositoriesController',['$scope', '$http', RepositoriesController]);

