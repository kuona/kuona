var dashboardApp = angular.module('dashboard-app', [
  'ui.bootstrap',                 // Ui Bootstrap
  'chart.js'
]);

function mergeParams(obj, params) {
  for (var attr in params) {
    obj[attr] = params[attr];
  }
}

var resultTransformers = {
  count: (data, panel, params) => {
    panel.data.title = panel.query.title;
    panel.data.value = data.count;

    mergeParams(panel.data, params);
  },
  sum: (data, panel, params) => {
    panel.data.value = data.aggregations.value.value;
    panel.data.title = panel.query.title;

    mergeParams(panel.data, params);
  },
  'aggregate-buckets': (data, panel, params) => {
    panel.data.title = panel.query.title;

    panel.data.orginal = data.aggregations;
    panel.data.labels = [];
    panel.data.values = [];

    for (var i = 0; i < data.aggregations.values.buckets.length; i++) {
      panel.data.labels.push(data.aggregations.values.buckets[i].key);
      panel.data.values.push(data.aggregations.values.buckets[i].doc_count);
    }


    mergeParams(panel.data, params);
  },
  results: (data, panel, params) => {
    panel.data.values = data.results;
    panel.data.title = panel.query.title;

    mergeParams(panel.data, params);
  }
};


function transformResult(type, data, panel, params) {
  if (resultTransformers[type]) {
    resultTransformers[type](data, panel, params);
  } else {
    console.log('No transform function for "' + type + '"');
  }
}

var widgetProcessors = {
  "count-metric": function (panel, $http) {
    $http.post("/api/query/" + panel.query.source, panel.query.json).then(res => {

      transformResult(panel.transform.type, res.data, panel, panel.transform.params)
    });
  },
  'pie-chart': (panel, $http) => {
    $http.post("/api/query/" + panel.query.source, panel.query.json).then(res => {
      transformResult(panel.transform.type, res.data, panel, panel.transform.params)
    });
  },
  'bar-chart': (panel, $http) => {
    $http.post("/api/query/" + panel.query.source, panel.query.json).then(res => {
      transformResult(panel.transform.type, res.data, panel, panel.transform.params)
    });
  },
  'build-status': (panel, $http) => {
  },
  'activity-feed': (panel, $http) => {
    $http.post("/api/query/" + panel.query.source, panel.query.json).then(res => {
      transformResult(panel.transform.type, res.data, panel, panel.transform.params)
    });
  }
};


(function (ChartJsProvider) {
  ChartJsProvider.setOptions({
    colors: [
      '#00ADF9',
      '#803690',
      '#46BFBD',
      '#FDB45C',
      '#949FB1',
      '#DCDCDC',
      '#4D5360']
  });
});

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
        type: 'activity-feed',
        source: 'query',
        query: {
          title: 'Commit History',
          source: 'commits',
          format: 'elastic-json',
          type: 'results',
          json: {
            size: 3,
            sort: [{timestamp: {order: "desc"}}],
            query: {term: {repository_id: "5060d887-29ae-38a1-810e-a0b4f9694104"}}
          }
        },
        transform: {
          type: 'results',
          params: {
            icon: 'fab fa-git'
          }
        },
        data: {}
      },
      {
        type: 'pie-chart',
        source: 'query',
        query: {
          title: 'Module builds',
          source: 'snapshots',
          format: 'elastic-json',
          type: 'aggregate',
          json: {
            size: 0,
            aggregations: {values: {terms: {field: "build.builder"}}}
          }
        },
        transform: {
          type: 'aggregate-buckets',
          params: {
            'chart-options': {responsive: true},
            icon: 'far fa-code'
          }
        },
        data: {}
      },
      {
        type: 'bar-chart',
        source: 'query',
        query: {
          title: 'Module builds',
          source: 'snapshots',
          format: 'elastic-json',
          type: 'aggregate',
          json: {
            size: 0,
            aggregations: {values: {terms: {field: "build.builder"}}}
          }
        },
        transform: {
          type: 'aggregate-buckets',
          params: {
            'chart-options': {responsive: true},
            'chart-colors': [
              '#b33f00',
              '#a2d703',
              '#fff33a'],

            icon: 'far fa-code'
          }
        },
        data: {}
      },
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
            started: Date.now() - 2000000
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
      },
      {
        type: 'count-metric',
        source: 'query',
        query: {
          title: 'Lines of code',
          source: 'snapshots',
          format: 'elastic-json',
          type: 'count',
          json: {
            "aggs": {
              "value": {"sum": {"field": "content.code_lines"}}
            }
          },
        },
        transform: {
          type: 'sum',
          params: {
            icon: 'far fa-code'
          }
        },
        data: {}
      }

    ]
  };

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
})

dashboardApp.directive('pieChartPanel', function () {
  return {
    restrict: 'E',
    scope: {
      data: "=data"
    },
    templateUrl: '/directives/pie-chart-panel.html'
  };
});

dashboardApp.directive('barChartPanel', function () {
  return {
    restrict: 'E',
    scope: {
      data: "=data"
    },
    templateUrl: '/directives/bar-chart-panel.html'
  };
});

dashboardApp.directive('activityFeedPanel', function () {
  return {
    restrict: 'E',
    scope: {
      data: "=data"
    },
    templateUrl: '/directives/activity-feed-panel.html'
  };
});

dashboardApp.filter('age', ageFilter);
dashboardApp.filter('elapsed', elapsedFilter);
