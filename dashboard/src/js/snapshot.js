var kuonaSnapshot = angular.module('kuona.snapshot', [
  'ui.bootstrap'
], function ($locationProvider) {
  $locationProvider.html5Mode(true);
});


function toDependencyId(a) {
  if (a.to) {
    return a.to.artifactId;
  }
  return "";
}

function fromDependencyId(a) {
  if (a.from) {
    return a.from.artifactId;
  }
  return "";
}

const dependencyCompare = (a, b) => (a.height - b.height) || a.id.localeCompare(b.id);


function enhanceDependencies(build) {

  for (var i = 0; i < build.length; i++) {
    var b = build[i];

    if (b.dependencies !== undefined && b.dependencies.dependencies !== undefined) {

      let list = [];
      list.push({"from": null, "to": b.dependencies.root});

      for (let k in b.dependencies.dependencies) {
        let item = b.dependencies.dependencies[k];
        list.push(item);
      }

      b["dependencyTree"] = list;
    }
  }
}

function manifestNodeShape(t) {
  if (t === 'database') {
    return 'database';
  } else {
    return 'box';
  }
}

function manifestGraph(data) {
  let result = {nodes: [], edges: []};
  let seen = {};
  for (let i in data.components) {
    let c = data.components[i];
    if (!(c.id in seen)) {
      result.nodes.push({id: c.id, label: c.description, shape: manifestNodeShape(c.kind)});
      seen[c.id] = true;
    }
    for (let j in c.dependencies) {
      let d = c.dependencies[j];
      if (!(d.id in seen)) {
        result.nodes.push({id: d.id, label: d.id, shape: manifestNodeShape(d.kind)});
        seen[d.id] = true;
      }
      result.edges.push({from: c.id, to: d.id});
    }
  }

  return result;
}

function isEmpty(obj) {
  for (let x in obj) {
    return false;
  }
  return true;
}

function SnapshotController($scope, $http, $location) {
  $scope.id = $location.search().id;
  $scope.repository = {};
  $scope.snapshot = {};
  $scope.avatar_url = null;
  $scope.commits = [];
  $scope.fileChartData = null;
  $scope.hasManifest = function () {
    if ($scope.snapshot.manifest) {
      return !isEmpty($scope.snapshot.manifest)
    }
    return false;
  };
  $scope.snapshotFound = false;

  $http.get("/api/snapshots/" + $scope.id).then(function (res) {
    let item;
    $scope.snapshot = res.data;
    if ($scope.snapshot !== "") {
      let i;
      $scope.snapshotFound = true;

      $scope.fileChartData = [];

      for (i = 0; i < $scope.snapshot.content.file_details.length; i++) {
        item = $scope.snapshot.content.file_details[i];
        $scope.fileChartData.push({
          "label": item.language, "color": colors[i], "value": item.count
        });
      }

      // barChart(document.getElementById("filesBarCanvas"), "Files", "Repository files by type", $scope.fileChartData);

      $scope.code_piechart_data = [];
      for (i = 0; i < $scope.snapshot.content.code_line_details.length; i++) {
        item = $scope.snapshot.content.code_line_details[i];
        $scope.code_piechart_data.push({
          "label": item.language, "color": colors[i], "value": item.count
        });
      }

      // barChart(document.getElementById("codeBarCanvas"), "Code", "Lines of code by type", $scope.code_piechart_data);

      enhanceDependencies($scope.snapshot.build);
    }
  });

  $http.get("/api/repositories/" + $scope.id).then(function (res) {
    $scope.repository = res.data;
    if ($scope.repository.project) {
      if ($scope.repository.project.owner) {
        $scope.avatar_url = $scope.repository.project.owner.avatar_url;
      }
    }
  });

  $http.get("/api/repositories/" + $scope.id + "/commits").then(function (res) {
    $scope.commits = res.data.items;

    for (let i in $scope.commits) {
      let c = $scope.commits[i];
      c.timestamp = Date.parse(c.commit.time);
      if (c.source.system === "git") {
        c.icon = "fab fa-git";
      } else {
        c.icon = "fas fa-code-branch";
      }
    }
  });
}

kuonaSnapshot.controller('SnapshotController', ['$scope', '$http', '$location', SnapshotController]);

kuonaSnapshot.directive('dependencyChart', function () {
  return {
    restrict: 'E',
    scope: {
      data: '='
    },
    link: function (scope, element, attrs) {
      dependencyTreeChart(scope.data, d3.select(element[0]));
    }
  };
});

kuonaSnapshot.directive('horizontalBarChart', () => ({
  restrict: 'A',
  scope: {
    data: '=',
    label: '=',
    title: '='
  },
  template: '<canvas height="140"></canvas>',
  link: (scope, element, attrs) => {
    console.log("element " + element);
    console.log("element[0] " + element[0]);
    console.log("element[0].children[0] " + element[0].children[0]);
    var canvasElement = element[0];
    scope.$watch('data', (newValue, oldValue) => {
      if (scope.data) {
        barChart(canvasElement, scope.label, scope.title, scope.data);
      }
    });
  }
}));

kuonaSnapshot.directive('commitsPanel', function () {
  return {
    restrict: 'E',
    scope: {
      commits: '=',
      count: '='
    },
    templateUrl: '/directives/commit-log-panel.html'
  };
});

kuonaSnapshot.directive('repositoryPanel', function () {
  return {
    restrict: 'E',
    scope: {
      repository: '='
    },
    templateUrl: '/directives/repository-panel.html'
  };
});

kuonaSnapshot.directive('contentPanel', function () {
  return {
    restrict: 'E',
    scope: {
      content: '='
    },
    templateUrl: '/directives/content-panel.html'
  };
});

kuonaSnapshot.directive('buildPanel', function () {
  return {
    restrict: 'E',
    scope: {
      build: '='
    },
    templateUrl: '/directives/build-panel.html'
  };
});

kuonaSnapshot.directive('artifactPanel', function () {
  return {
    restrict: 'E',
    scope: {
      path: '=',
      artifact: '=',
      dependencyTree: '=',
      build: '='
    },
    templateUrl: '/directives/build-artifact-panel.html'
  };
});

kuonaSnapshot.directive('manifestPanel', () => ({
  restrict: 'E',
  transclude: true,
  scope: {
    data: '='
  },
  link: (scope, element, attrs) => {
    scope.$watch('data', function (newValue, oldValue) {
      if (scope.data) {
        const graphData = manifestGraph(scope.data);
        new vis.Network(element[0],
          {
            nodes: new vis.DataSet(graphData.nodes),
            edges: new vis.DataSet(graphData.edges)
          }, {
            autoResize: true,
            height: '300px',
            width: '100%',
            nodes: {fixed: true},
            edges: {arrows: 'to'},
            layout: {
              hierarchical: {direction: 'UD'}
            }
          });
      } else {
        console.warn("manifest graph directive called with no data");
      }
    });
  }
}));

kuonaSnapshot.directive('jsonView', () => ({
  restrict: 'E',
  transclude: true,
  scope: {
    data: '='
  },
  templateUrl: '/directives/json-view.html',
  link: (scope, element, attrs) => {
    const codeElement = element[0].children[0].children[0];
    scope.$watch('data', (newValue, oldValue) => {
      if (scope.data) {
        scope.$evalAsync(() => {
          hljs.highlightBlock(codeElement);
        });
      }
    });
  }
}));

kuonaSnapshot.directive('markdown', () => ({
  restrict: 'E',
  transclude: true,
  scope: {
    data: "="
  },
  link: (scope, element, attrs) => {
    scope.$watch('data', () => {
      const converter = new showdown.Converter();
      const htmlText = converter.makeHtml(scope.data);
      element.html(htmlText);
    });
  }
}));

registerKuonaAngularFilters(kuonaSnapshot);
