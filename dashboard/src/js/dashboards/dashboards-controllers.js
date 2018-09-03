function DashboardListController($scope, $http) {
  $scope.pageHeading = 'Kuona Dashboards';
  $scope.pageDescription = 'Manage your dashboards.';
  $scope.dashboards = [];

  $http.get("/api/dashboards").then(function (res) {
    $scope.dashboards = res.data.items;
  });
}

function NewDashboardController($scope, $http, $location) {
  $scope.dashboard = {name: "", description: "", definition: ""};
  $scope.dashboardNameRegex = '[A-Za-z0-9_-]+';

  $scope.saveDashboard = () => {
    const request = {
      name: $scope.dashboard.name,
      description: $scope.dashboard.description,
      panels: JSON.parse($scope.dashboard.definition)
    };
    $http.post("/api/dashboards", request).then(res => $location.path("/dashboards"));
  };
}

angular
  .module('kuona-dashboards')
  .controller('DashboardListController', ['$scope', '$http', DashboardListController])
  .controller('NewDashboardController', ['$scope', '$http', '$location', NewDashboardController]);
