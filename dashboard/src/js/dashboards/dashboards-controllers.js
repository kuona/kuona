function MainCtrl($scope, $http) {
  this.helloText = 'Kuona Dashboards';
  this.descriptionText = 'Manage your dashboards.';

  $scope.dashboards = [];

  $http.get("/api/dashboards").then(function (res) {
    $scope.dashboards = res.data.items;
  });
}

function DashboardController($scope, $http, $location) {
  $scope.dashboard = {name: "", description: ""};
  $scope.dashboardNameRegex = '[A-Za-z0-9_-]+';

  $scope.saveDashboard = function () {
    var request = {
      "name": $scope.dashboard.name,
      "description": $scope.dashboard.description
    };
    $http.post("/api/dashboards", request).then(function (res) {
      $location.path("/dashboards")
    });
  };
}


angular
  .module('kuona-dashboards')
  .controller('MainCtrl', ['$scope', '$http', MainCtrl])
  .controller('DashboardController', ['$scope', '$http', '$location', DashboardController]);
