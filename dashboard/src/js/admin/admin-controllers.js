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
  this.descriptionText = 'It is an application skeleton for a typical AngularJS web app. You can use it to quickly bootstrap your angular webapp projects and dev environment for these projects.';
  $scope.info = {};

  $http.get('/api/info').then(function (res) {
    $scope.info = res.data;
  });
}

function NewGithubRepoController($scope, $http) {
  $scope.repository_url = null;
  $scope.repository_default = "https://github.com/kuona/kuona-project";

  $scope.addRepo = function () {
    console.log("Add repo" + $scope.repository_url);
  };

  $scope.testRepo = function () {
    console.log("Test repo" + $scope.repository_url);
  };
}

angular
  .module('kuona-admin')
  .controller('MainCtrl', ['$scope', '$http', MainCtrl])
  .controller('NewGithubRepoController', ['$scope', '$http', NewGithubRepoController]);
