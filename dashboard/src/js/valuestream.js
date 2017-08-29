var kuonaValueStream = angular.module('kuona.valuestream', [ 'ui.bootstrap']);


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
      .attr("transform", function(d, i) { return "translate(100,100)"; });
  
  bar.append("rect")
    .attr("width", 100)
    .attr("height", 100)
    .attr("fill", "pink")
    .attr("stroke", "black")
    .attr("stroke-width", 1);

  
  bar.append("text")
    .attr("dy", "+1.35em")
    .text(function(d) { return d; });

  $http.get("/api/valuestreams").then(function(res){
    $scope.valuestreams = res.data.valuestreams;
  });
}

kuonaValueStream.controller('ValueStreamController',['$scope', '$http', '$location',  valueStreamController]);

kuonaValueStream.directive('valuestreamSummaryPanel', function () {
  return {
    restrict: 'E',
    scope: {
      valuestream: '='
    },
    templateUrl: '/directives/valuestream-summary.html'
  };
});


kuonaValueStream.filter('elapsed', function(){
  return function(duration) {
    var seconds = Math.floor(duration / 1000);
    var minutes = Math.floor(seconds / 60);
    var hours = Math.floor(minutes / 60);
    var days = Math.floor(hours / 24);
    
    if (days > 1) {
      return days + " days";
    } else if (hours > 1) {
      return hours + " hours";
    } else if (minutes > 1) {
      return minutes + " minutes";
    }  else {
      return seconds + " seconds";
    }
  }
});

kuonaValueStream.filter('age', function(){
  return function(date){
    if (!date) return;
    var time = Date.parse(date),
        timeNow = new Date().getTime(),
        difference = timeNow - time,
        seconds = Math.floor(difference / 1000),
        minutes = Math.floor(seconds / 60),
        hours = Math.floor(minutes / 60),
        days = Math.floor(hours / 24);
    if (days > 1) {
      return days + " days ago";
    } else if (days == 1) {
      return "1 day ago"
    } else if (hours > 1) {
      return hours + " hours ago";
    } else if (hours == 1) {
      return "an hour ago";
    } else if (minutes > 1) {
      return minutes + " minutes ago";
    } else if (minutes == 1){
      return "a minute ago";
    } else {
      return "seconds ago";
    }
  }
});

kuonaValueStream.directive('commitsPanel', function () {
  return {
    restrict: 'E',
    scope: {
      commits: '=',
      count: '='
    },
    templateUrl: '/directives/commits.html'
  };
});
