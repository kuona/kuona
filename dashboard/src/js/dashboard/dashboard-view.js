let dashboardApp = angular.module('dashboard-app', [
  'ui.bootstrap',
  'chart.js'
]);

function mergeParams(obj, params) {
  for (var attr in params) {
    obj[attr] = params[attr];
  }
}

let resultTransformers = {
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

let widgetProcessors = {
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
  let dashboardName = new URL(location.href).searchParams.get('name');
  console.log("Loading dashboard " + dashboardName);
  $scope.helloText = 'Kuona Dashboards';

  $scope.buildStatus = {
    name: 'The build name',
    passed: true,
    disabled: true,
    failed: true,
    running: true,
    status: "blue"
  };
  $scope.dashboard = null;

  let refreshView = (dashboard) => {
    console.log(dashboard);
    if (dashboard.panels) {
      for (let i = 0; i < dashboard.panels.length; i++) {
        let panel = dashboard.panels[i];

        if (widgetProcessors[panel.type]) {
          widgetProcessors[panel.type](panel, $http);
        } else {
          console.log("No processor found for " + panel.type);
        }
      }
    }
    $scope.dashboard = dashboard;
  };

  $http.get("/api/dashboards/" + dashboardName).success(data => {
    refreshView(data.dashboard);
  });
  refreshView({});
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
