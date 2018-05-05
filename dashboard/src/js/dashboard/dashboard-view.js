var dashboardApp = angular.module('dashboard-app', [
  'ui.bootstrap'                 // Ui Bootstrap
]);


dashboardApp.controller('DashboardViewController', ['$scope', '$http', function ($scope, $http) {
  $scope.helloText = 'Kuona Dashboards';

  $scope.buildStatus = {
    name: 'The build name',
    passed: true,
    disabled: true,
    failed: true,
    running: true,
    status: "blue"
  };

  $scope.dashboard = {
    title: 'Some title that someone chose',
    description: 'Some lame description',
    panels: [
      // {
      //   type: 'build-status',
      //   data: $scope.buildStatus
      // },
      {
        type: 'build-status',
        data: $scope.buildStatus
      }
    ]
  }

}]);

dashboardApp.directive('pietyChart', function () {
  return {
    link: function link(scope, element, attrs) {
      element.peity(attrs.pietyChart, {
        fill: ['#b3012b',
          '#0ed70b',
          '#ffffff']
      });
    }
  };
});

dashboardApp.directive('dashboardPanel', function () {
  return {
    restrict: 'E',
    link: function (scope, element, attrs) {
      console.log(attrs.type);
      scope.contentUrl = '/directives/' + attrs.type + '-panel.html';
      scope.build = attrs.data;
      attrs.$observe("ver", function (v) {
        scope.contentUrl = '/directives/' + v + '-panel.html';
        scope.build = attrs.data;
      });
    },
    template: '<div ng-include="contentUrl"></div>'
  };
})

dashboardApp.directive('buildStatusPanel', function () {
  return {
    restrict: 'E',
    scope: {
      build: "=data"
    },
    templateUrl: '/directives/build-status-panel.html'
  };
});
