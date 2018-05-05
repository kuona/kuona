var dashboardApp = angular.module('dashboard-app', [
  'ui.bootstrap'                 // Ui Bootstrap
]);


var resultTransformers = {
  count: (data, panel, params) => {
    panel.data.title = panel.query.title;
    panel.data.value = data.count;

    for (var attr in params) {
      panel.data[attr] = params[attr];
    }
  },
  sum: (data, panel, params) => {
    console.log(data);

    panel.data.value = data.aggregations.value.value;
    panel.data.title = panel.query.title;

    for (var attr in params) {
      panel.data[attr] = params[attr];
    }
  }
};


var widgetProcessors = {
  "count-metric": function (panel, $http) {
    $http.post("/api/query/" + panel.query.source, panel.query.json).then(res => {

      if (resultTransformers[panel.transform.type]) {
        resultTransformers[panel.transform.type](res.data, panel, panel.transform.params);
      }
    });
  }
};


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
          title: 'Maven Repositories',
          source: 'snapshots',
          format: 'elastic-json',
          type: 'count',
          json: {
            "query": {
              "term": {"build.builder": "Maven"}
            }
          },
        },
        transform: {
          type: 'count',
          params: {
            icon: 'far fa-cogs'
          }
        },
        data: {}
      },
      {
        type: 'count-metric',
        source: 'query',
        query: {
          title: 'Clojure Repositories',
          source: 'snapshots',
          format: 'elastic-json',
          type: 'count',
          json: {
            "query": {
              "term": {"build.builder": "Leiningen"}
            }
          },
        },
        transform: {
          type: 'count',
          params: {
            icon: 'far fa-cog fa-spin'
          }
        },
        data: {}
      },
      {
        type: 'count-metric',
        source: 'query',
        query: {
          title: 'Number of file processed',
          source: 'snapshots',
          format: 'elastic-json',
          type: 'count',
          json: {
            "aggs": {
              "value": {"sum": {"field": "content.file_count"}}
            }
          },
        },
        transform: {
          type: 'sum',
          params: {
            icon: 'far fa-file'
          }
        },
        data: {}
      }
    ]
  }

  for (var i = 0; i < $scope.dashboard.panels.length; i++) {
    let panel = $scope.dashboard.panels[i];

    if (widgetProcessors[panel.type]) {
      widgetProcessors[panel.type](panel, $http);
    } else {
      console.log("No processor found for " + panel.type);
    }
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
      scope.contentUrl = '/directives/' + attrs.type + '-panel.html';
      scope.build = attrs.data;
      attrs.$observe("ver", function (v) {
        scope.contentUrl = '/directives/' + v + '-panel.html';
        scope.build = attrs.data;
      });
    },
    template: '<div ng-include="contentUrl"></div>'
  };
});

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
