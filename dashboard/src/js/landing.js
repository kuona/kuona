let kuona = angular.module('kuona.dashboard', [
  'ui.router',
  'oc.lazyLoad',
  'ui.bootstrap',
  'ngResource'
]);

registerKuonaAngularFilters(kuona);

function MainController($scope, $http) {
  $scope.currentDate = new Date();
  $scope.repository_count = "[loading]";
  $scope.vcs_count = "[loading]";
  $scope.buildTools = [];
  $scope.collector_activity = [];
  $scope.code_metric_count = 0;
  $scope.code_snapshot_count = 0;
  $scope.query = null;
  $scope.query_response = null;
  $scope.search = function () {
    console.log("Query for " + $scope.query);
    $http.post('api/chat', {query: $scope.query}).then(function (res) {
      $scope.query_response = res.data.response.message;
    });
  };
  $http.get('/api/build/tools').then(function (res) {
    $scope.buildTools = [];
    let data = res.data.buckets;

    let colorIndex = 0;
    for (let k in data) {
      let item = data[k];
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

  $http.get('/api/collectors/activities').then(function (res) {
    $scope.collector_activity = res.data.items;
  });
}

function FooterController($scope, $http) {
  $scope.info = {};
  $http.get('/api/info').then(function (res) {
    $scope.info = res.data;
  });
}

kuona
  .controller('MainController', ['$scope', '$http', MainController])
  .controller("FooterController", ['$scope', '$http', FooterController]);
