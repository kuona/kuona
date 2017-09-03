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
    },
    link: function (scope, element, attrs) {
      barChart(d3.select(element[0]).select('.buid-tool-canvas'), scope.title, scope.description, scope.data);
    }
  };
});

kuonaRepositories.filter('elapsed', elapsedFilter);

kuonaRepositories.filter('age', ageFilter);

function RepositoriesController($scope, $http) {
  this.helloText = 'Welcome to Kuona';
  this.descriptionText = 'Use the navigation to look around :)';

  $scope.currentDate = new Date();
  $scope.repositoriesFound = [];

  $scope.repoSearch = function(term) {
    $http.get("/api/repositories?search=" + term).then(function(res){
      $scope.repositoriesFound = res.data;
    });
  };

  $scope.repoSearch("");
}



kuonaRepositories.controller('RepositoriesController',['$scope', '$http', RepositoriesController]);

