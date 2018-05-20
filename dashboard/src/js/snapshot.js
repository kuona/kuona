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

var dependencyCompare = function (a, b) {
  return (a.height - b.height) || a.id.localeCompare(b.id);
}


function enhanceDependencies(build) {

  for (var i = 0; i < build.length; i++) {
    var b = build[i];

    if (b.dependencies != undefined && b.dependencies.dependencies != undefined) {

      var list = []
      list.push({"from": null, "to": b.dependencies.root});

      for (var k in b.dependencies.dependencies) {
        var item = b.dependencies.dependencies[k]
        list.push(item);
      }

      b["dependencyTree"] = list;
    }
  }
}


function SnapshotController($scope, $http, $location) {
  $scope.id = $location.search().id;
  $scope.repository = {}
  $scope.snapshot = {};
  $scope.avatar_url = "";
  $scope.commits = [];

  $http.get("/api/snapshots/" + $scope.id).then(function (res) {
    $scope.snapshot = res.data;

    $scope.file_piechart_data = [];

    for (var i = 0; i < $scope.snapshot.content.file_details.length; i++) {
      var item = $scope.snapshot.content.file_details[i];
      $scope.file_piechart_data.push({
        "label": item.language, "color": colors[i], "value": item.count
      });
    }

    barChart(document.getElementById("filesBarCanvas"), "Files", "Repository files by type", $scope.file_piechart_data);

    $scope.code_piechart_data = [];
    for (var i = 0; i < $scope.snapshot.content.code_line_details.length; i++) {
      var item = $scope.snapshot.content.code_line_details[i];
      $scope.code_piechart_data.push({
        "label": item.language, "color": colors[i], "value": item.count
      });
    }

    barChart(document.getElementById("codeBarCanvas"), "Code", "Lines of code by type", $scope.code_piechart_data);

    enhanceDependencies($scope.snapshot.build);
  });

  $http.get("/api/repositories/" + $scope.id).then(function (res) {
    $scope.repository = res.data;
    $scope.avatar_url = $scope.repository.project.owner.avatar_url
  });

  $http.get("/api/repositories/" + $scope.id + "/commits").then(function (res) {
    $scope.commits = res.data.items;

    for (var i in $scope.commits) {
      var c = $scope.commits[i];
      c.timestamp = Date.parse(c.time);
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

kuonaSnapshot.directive('commitsPanel', function () {
  return {
    restrict: 'E',
    scope: {
      commits: '=',
      count: '='
    },
    templateUrl: '/directives/commits.html'
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
      dependencyTree: '='
    },
    templateUrl: '/directives/build-artifact-panel.html'
  };
});


// Create a new directed graph
var g = new dagreD3.graphlib.Graph().setGraph({});

// States and transitions from RFC 793
var states = ["CLOSED", "LISTEN", "SYN RCVD", "SYN SENT",
  "ESTAB", "FINWAIT-1", "CLOSE WAIT", "FINWAIT-2",
  "CLOSING", "LAST-ACK", "TIME WAIT"];

// Automatically label each of the nodes
states.forEach(function (state) {
  g.setNode(state, {label: state});
});

// Set up the edges
g.setEdge("CLOSED", "LISTEN", {label: "open"});
g.setEdge("LISTEN", "SYN RCVD", {label: "rcv SYN"});
g.setEdge("LISTEN", "SYN SENT", {label: "send"});
g.setEdge("LISTEN", "CLOSED", {label: "close"});
g.setEdge("SYN RCVD", "FINWAIT-1", {label: "close"});
g.setEdge("SYN RCVD", "ESTAB", {label: "rcv ACK of SYN"});
g.setEdge("SYN SENT", "SYN RCVD", {label: "rcv SYN"});
g.setEdge("SYN SENT", "ESTAB", {label: "rcv SYN, ACK"});
g.setEdge("SYN SENT", "CLOSED", {label: "close"});
g.setEdge("ESTAB", "FINWAIT-1", {label: "close"});
g.setEdge("ESTAB", "CLOSE WAIT", {label: "rcv FIN"});
g.setEdge("FINWAIT-1", "FINWAIT-2", {label: "rcv ACK of FIN"});
g.setEdge("FINWAIT-1", "CLOSING", {label: "rcv FIN"});
g.setEdge("CLOSE WAIT", "LAST-ACK", {label: "close"});
g.setEdge("FINWAIT-2", "TIME WAIT", {label: "rcv FIN"});
g.setEdge("CLOSING", "TIME WAIT", {label: "rcv ACK of FIN"});
g.setEdge("LAST-ACK", "CLOSED", {label: "rcv ACK of FIN"});
g.setEdge("TIME WAIT", "CLOSED", {label: "timeout=2MSL"});

// Set some general styles
g.nodes().forEach(function (v) {
  var node = g.node(v);
  node.rx = node.ry = 5;
});

// Add some custom colors based on state
g.node('CLOSED').style = "fill: #f77";
g.node('ESTAB').style = "fill: #7f7";

var svg = d3.select("svg"),
  inner = svg.select("g");

// Set up zoom support
var zoom = d3.zoom().on("zoom", function () {
  inner.attr("transform", d3.event.transform);
});
svg.call(zoom);

// Create the renderer
var render = new dagreD3.render();

// Run the renderer. This is what draws the final graph.
render(inner, g);

// Center the graph
var initialScale = 2.00;
svg.call(zoom.transform, d3.zoomIdentity.translate((svg.attr("width") - g.graph().width * initialScale) / 2, 20).scale(initialScale));

svg.attr('height', g.graph().height * initialScale + 40);
