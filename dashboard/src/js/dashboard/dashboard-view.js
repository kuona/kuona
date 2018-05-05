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
      {
        type: 'build-status',
        data: {
          name: 'The build name 1',
          status: {
            build: 'passed',
            run: 'running',
            started: Date.now()
          }
        }
      },
      {
        type: 'build-status',
        data: {
          name: 'The failed build name 2',
          status: {
            build: 'failed',
            run: 'paused',
            started: Date.now()
          }
        }
      },
      {
        type: 'build-status',
        data: {
          name: 'The aborted build name 3',
          status: {
            build: 'aborted',
            run: 'running',
            started: Date.now()
          }
        }
      },
      {
        type: 'build-status',
        data: {
          name: 'build 4',
          status: {
            build: 'passed',
            run: 'running',
            started: Date.now()
          }
        }
      },
      {
        type: 'build-status',
        data: {
          name: 'build 5',
          status: {
            build: 'passed',
            run: 'sleeping',
            started: Date.now()
          }
        }
      },
      {
        type: 'build-status',
        data: {
          name: 'build 6',
          status: {
            build: 'aborted',
            run: 'running',
            started: Date.now()
          }
        }
      },
      {
        type: 'count-metric',
        source: 'query',
        query: {
          title: 'Builders (gradle/maven/make etc)',
          source: 'builds',
          type: 'json',
          json: {
            "query": {
              "match_all": {}
            }
          },


        },
        transform: {
          type: 'count'
        },
        data: {
          title: 'Count metric - e.g. Number of Gradle Builds',
          value: 100,
          icon: 'far fa-hand-paper'
        }
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

dashboardApp.directive('countMetricPanel', function () {
  return {
    restrict: 'E',
    scope: {
      metric: "=data"
    },
    templateUrl: '/directives/count-metric-panel.html'
  };
});

dashboardApp.filter('age', ageFilter);
