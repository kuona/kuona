var kuona_repositories = angular.module('kuona.repositories', [
    'oc.lazyLoad',                  // ocLazyLoad
    'ui.bootstrap',                 // Ui Bootstrap
    'ngResource'
]);



function RepositoriesController($scope, $http) {
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

kuona_repositories.controller('RepositoriesController',['$scope', '$http', RepositoriesController]);
