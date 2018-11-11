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
  },
  'health-check-view': (data, panel, params) => {
    let result_table = [];
    for (let i = 0; i < data.results.length; i++) {
      for (let j = 0; j < data.results[i].results.length; j++) {
        result_table.push({
          date: data.results[i].date,
          type: data.results[i].type,
          tags: data.results[i].tags.join(', '),
          url: data.results[i].results[j].health.url,
          status: data.results[i].results[j].health.status
        });
      }
    }
    panel.data.view_data = {
      title: panel.query.title,
      entries: result_table
    }
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
  },
  'health-check': (panel, $http) => {
    $http.post("/api/query/" + panel.query.source, panel.query.json).then(res => {
      panel.data = res.data;
      transformResult('health-check-view', res.data, panel, null);
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

  $scope.panelWidth = (width) => {
    return width ? 'col-lg-' + width : 'col-lg-3';
  };
  let refreshView = (dashboard) => {
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
registerModuleDirectives(dashboardApp);
registerKuonaAngularFilters(dashboardApp);
