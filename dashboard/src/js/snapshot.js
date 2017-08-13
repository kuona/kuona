var kuonaSnapshot = angular.module('kuona.snapshot', [
  'ui.bootstrap'
], function($locationProvider) {
  $locationProvider.html5Mode(true);
});


function toDependencyId(a) {
  if (a.to){
    return a.to.artifactId;
  }
  return "";
}

function fromDependencyId(a) {
  if (a.from){
    return a.from.artifactId;
  }
  return "";
}

var dependencyCompare = function(a, b) {
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
  
  $http.get("/api/snapshots/" + $scope.id).then(function(res){
    $scope.snapshot = res.data;

    $scope.file_piechart_data = [];
    
    for (var i = 0; i < $scope.snapshot.content.file_details.length; i++) {
      var item = $scope.snapshot.content.file_details[i];
      $scope.file_piechart_data.push({ "label": item.language, "color": colors[i], "value": item.count
      });
    }

    barChart(document.getElementById("filesBarCanvas"), "Files", "Repository files by type", $scope.file_piechart_data);

    $scope.code_piechart_data = [];
    for (var i = 0; i < $scope.snapshot.content.code_line_details.length; i++) {
      var item = $scope.snapshot.content.code_line_details[i];
      $scope.code_piechart_data.push({"label": item.language, "color": colors[i], "value": item.count
      });
    }

    barChart(document.getElementById("codeBarCanvas"), "Code", "Lines of code by type", $scope.code_piechart_data);

    enhanceDependencies($scope.snapshot.build);
  });

  $http.get("/api/repositories/" + $scope.id).then(function(res){
    $scope.repository = res.data;
    $scope.avatar_url = $scope.repository.project.owner.avatar_url
  });

  $http.get("/api/repositories/" + $scope.id + "/commits").then(function(res){
    $scope.commits = res.data.items;

    for (var i in $scope.commits) {
      var c = $scope.commits[i];
      c.timestamp = Date.parse(c.time);
    }
  });
}

kuonaSnapshot.controller('SnapshotController',['$scope', '$http', '$location',  SnapshotController]);

kuonaSnapshot.directive('dependencyChart', function () {
    return {
        restrict: 'E',
        scope: {
            data: '='
        },
//        template: '<div class="myTemplate"></div>',
        link: function (scope, element, attrs) {
          // We lookup .myTemplate starting from element
          dependencyTreeChart(scope.data, d3.select(element[0]));//.select('.myTemplate'))
        }
    };
});
