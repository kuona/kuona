function DashboardListController($scope, $http) {
  $scope.pageHeading = 'Kuona Dashboards';
  $scope.pageDescription = 'Manage your dashboards.';
  $scope.dashboards = [];

  let loadDashboards = () => {
    $http.get("/api/dashboards").then(function (res) {
      $scope.dashboards = res.data.dashboards;
    });
  };

  $scope.deleteDashboard = (id) => {
    $http.delete("/api/dashboards/" + id);
    loadDashboards();
  };
  loadDashboards();
}

let testDashobardPanels = [
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
        icon: 'far fa-code'
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
  },
  {
    type: 'health-check',
    source: 'query',
    query: {
      title: 'Kuona Health checks',
      source: 'health-check-snapshot',
      format: 'elastic-json',
      type: 'results',
      json: {
        "query": {
          "bool": {
            "must": [{"term": {"tags": "kuona"}},
              {"term": {"tags": "selftest"}}]
          }
        }
      }
    }
  }


];

const sample = [
  {
    type: 'health-check',
    width: 6,
    source: 'query',
    query: {
      title: 'Kuona Health checks',
      source: 'health-check-snapshot',
      format: 'elastic-json',
      type: 'results',
      json: {
        "query": {
          "bool": {
            "must": [{"term": {"tags": "kuona"}},
              {"term": {"tags": "selftest"}}]
          }
        }
      }
    }
  }
];

function NewDashboardController($scope, $http, $location) {
  $scope.dashboard = {
    name: "",
    title: "",
    description: "",
    definition: JSON.stringify(sample)
  };
  $scope.dashboardNameRegex = '[A-Za-z0-9_-]+';

  $scope.saveDashboard = () => {
    const dashboard = {
      name: $scope.dashboard.name,
      title: $scope.dashboard.title,
      description: $scope.dashboard.description,
      panels: JSON.parse($scope.dashboard.definition)
    };
    $http.post("/api/dashboards", dashboard).then(() => $location.path("/dashboards"));
  };
  $scope.addTestDashboards = () => {
    const dashboard = {
      name: "test1",
      title: "Dashboard for Testing dashboards",
      description: "A dashboard that exercises dashboard panels using both fixed data and the results of queries against the data sets.",
      panels: testDashobardPanels
    };
    $http.post("/api/dashboards", dashboard).then(() => $location.path("/dashboards"));
  }
}

angular
  .module('kuona-dashboards')
  .controller('DashboardListController', ['$scope', '$http', DashboardListController])
  .controller('NewDashboardController', ['$scope', '$http', '$location', NewDashboardController]);
