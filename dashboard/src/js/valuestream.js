var kuonaValueStream = angular.module('kuona.valuestream', ['ui.bootstrap']);


function valueStreamController($scope, $http, $location) {
  $scope.valuestreams = [];
  var container = d3.select("#chart-container");
  var svg = d3.select("#chart-container").append("svg")
    .attr("width", "100%")
    .attr("height", 200)
    .style("background-color", 'red');


  var data = ["TEXT SAMPLE"];

  var bar = svg.selectAll("g")
    .data(data)
    .enter().append("g")
    .attr("transform", function (d, i) {
      return "translate(100,100)";
    });

  bar.append("rect")
    .attr("width", 100)
    .attr("height", 100)
    .attr("fill", "pink")
    .attr("stroke", "black")
    .attr("stroke-width", 1);


  bar.append("text")
    .attr("dy", "+1.35em")
    .text(function (d) {
      return d;
    });

  $http.get("/api/valuestreams").then(function (res) {
    $scope.valuestreams = res.data.valuestreams;
  });
}

kuonaValueStream.controller('ValueStreamController', ['$scope', '$http', '$location', valueStreamController]);
registerKuonaAngularFilters(kuonaValueStream);

kuonaValueStream.directive('valuestreamSummaryPanel', function () {
  return {
    restrict: 'E',
    scope: {
      valuestream: '='
    },
    templateUrl: '/directives/valuestream-summary.html'
  };
});


kuonaValueStream.directive('commitsPanel', function () {
  return {
    restrict: 'E',
    scope: {
      commits: '=',
      count: '='
    },
    templateUrl: '/directives/commit-log-panel.html'
  };
});
