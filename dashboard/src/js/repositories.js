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

registerKuonaAngularFilters(kuonaRepositories);

function RepositoriesController($scope, $repositories) {
  $scope.repositoriesFound = [];

  $scope.repoSearch = function(term) {
    $repositories.find(term).success(res => $scope.repositoriesFound = res.data);
  };

  $scope.repoSearch("");
}

kuonaRepositories.factory('$repositories', ['$http', ($http) => {
  return {
    find: (term) => $http.get("/api/repositories?search=" + term)
  }
}]);

kuonaRepositories.controller('RepositoriesController',['$scope', '$repositories', RepositoriesController]);

