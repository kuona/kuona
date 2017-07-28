var kuona_snapshot = angular.module('kuona.snapshot', [
  'angular-websocket',
  'ui.bootstrap'                 // Ui Bootstrap
], function($locationProvider) {
  $locationProvider.html5Mode(true);
});

var colors = [
  "#2484c1",
  "#0c6197",
  "#4daa4b",
  "#90c469",
  "#daca61",
  "#e4a14b",
  "#e98125",
  "#cb2121",
  "#830909",
  "#923e99",
  "#ae83d5",
  "#bf273e",
  "#ce2aeb",
  "#bca44a",
  "#618d1b",
  "#1ee67b",
  "#b0ec44",
  "#a4a0c9",
  "#322849",
  "#86f71a",
  "#d1c87f",
  "#7d9058",
  "#44b9b0",
  "#7c37c0",
  "#cc9fb1",
  "#e65414",
  "#8b6834",
  "#248838"];

var pieChart = function(id, title, subtitle, data) {
  var chart = new d3pie(id,
                        { "header": {
                          "title": {
                            "text": title,
                            "fontSize": 24,
                            "font": "open sans"
                          },
                          "subtitle": {
                            "text": subtitle,
                            "color": "#999999",
                            "fontSize": 12,
                            "font": "open sans"
                          },
                          "location": "top-left",
                          "titleSubtitlePadding": 9
                        },
	                  "footer": {
		            "color": "#999999",
		            "fontSize": 10,
		            "font": "open sans",
		            "location": "bottom-left"
	                  },
	                  "size": {
		            "canvasHeight": 300,
		            "canvasWidth": 390,
		            "pieOuterRadius": "90%"
	                  },
	                  "data": {
		            "sortOrder": "value-desc",
	                    "content": data,

	                  },
	                  "labels": {
		            "outer": {
			      "pieDistance": 32
		            },
		            "inner": {
			      "hideWhenLessThanPercentage": 3
		            },
		            "mainLabel": {
			      "fontSize": 11
		            },
		            "percentage": {
			      "color": "#ffffff",
			      "decimalPlaces": 0
		            },
		            "value": {
			      "color": "#adadad",
			      "fontSize": 11
		            },
		            "lines": {
			      "enabled": true
		            },
		            "truncation": {
			      "enabled": true
		            }
	                  },
	                  "effects": {
		            "pullOutSegmentOnClick": {
			      "effect": "linear",
			      "speed": 400,
			      "size": 8
		            }
	                  },
	                  "misc": {
		            "gradient": {
			      "enabled": true,
			      "percentage": 100
		            }
	                  }
                        });
  
};


var polarChart = function(id, title, subtitle, data) {
  var dataValues = [];
  var dataColors = [];
  var dataLabels = [];

  for (var i = 0; i < data.length; i++) {
    var item = data[i];
    dataValues.push(item.value);
    dataColors.push(item.color);
    dataLabels.push(item.label);
  }
  
  var polarData = {
    datasets: [{
      data: dataValues,
      backgroundColor: dataColors,
      label: [
        title
      ]
    }],
    labels: dataLabels
  };

  var polarOptions = {
    segmentStrokeWidth: 2,
    responsive: true
  };

  var ctx3 = document.getElementById(id).getContext("2d");
  new Chart(ctx3, {type: 'polarArea', data: polarData, options:polarOptions});
}

var barChart = function(id, title, subtitle, data) {
  var dataValues = [];
  var dataColors = [];
  var dataLabels = [];

  for (var i = 0; i < data.length; i++) {
    var item = data[i];
    dataValues.push(item.value);
    dataColors.push(item.color);
    dataLabels.push(item.label);
  }

  var ctx = document.getElementById(id).getContext("2d");
  return new Chart(ctx, {
    type: 'horizontalBar',
    data: { labels: dataLabels,
            datasets: [{
              label: title,
              backgroundColor: dataColors[0],
              borderColor: dataColors[0],
              borderWidth: 1,
              data: dataValues
            }]},
    options: {
      // Elements options apply to all of the options unless overridden in a dataset
      // In this case, we are setting the border of each horizontal bar to be 2px wide
      elements: {
        rectangle: {
          borderWidth: 2,
        }
      },
      responsive: true,
      legend: {
        position: 'right',
      },
      title: {
        display: true,
        text: subtitle
      }
    }
  });
};


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

var dependencyTreeChart = function(treeData, treeContainer) {
  var width = 600;
  var height = 600;
  var svg = treeContainer.append("svg").attr("width", width).attr("height", height);
  var g = svg.append("g").attr("transform", "translate(40,0)");

  //var svg = d3.select("svg"),
//  width = +svg.attr("width");
//  height = +svg.attr("height");
  

  var tree = d3.cluster()
      .size([height, width - 200]);

  var stratify = d3.stratify(treeData)
      .id(toDependencyId)
      .parentId(fromDependencyId);
  
  var root = stratify(treeData).sort(dependencyCompare);

  tree(root);

  var link = g.selectAll(".link")
      .data(root.descendants().slice(1))
      .enter().append("path")
      .attr("class", "link")
      .attr("d", function(d) {
        return "M" + d.y + "," + d.x
          + "C" + (d.parent.y + 50) + "," + d.x
          + " " + (d.parent.y + 50) + "," + d.parent.x
          + " " + d.parent.y + "," + d.parent.x;
      });

  var node = g.selectAll(".node")
      .data(root.descendants())
      .enter().append("g")
      .attr("class", function(d) { return "node" + (d.children ? " node--internal" : " node--leaf"); })
      .attr("transform", function(d) { return "translate(" + d.y + "," + d.x + ")"; })

  node.append("circle")
      .attr("r", 2.5);

  node.append("text")
    .attr("dy", function(d) {
      if (!d.parent) {
        return -8;
      }
      return 2; })
    .attr("x", function(d) {
      if (!d.parent) {
        return 8;
      }
      return d.children ? -8 : 8; })
    .style("text-anchor", function(d) { return d.children ? "end" : "start"; })
    .text(function(d) { return d.id.substring(d.id.lastIndexOf(".") + 1); });
}


function enhanceDependencies(build) {

  for (var i = 0; i < build.length; i++) {
    var b = build[i];
    
    if (b.dependencies != undefined && b.dependencies.dependencies != undefined) {

      var list = []


      
      // Add root to the dependency list without a from value
      list.push({"from": null, "to": b.dependencies.root});

      for (var k in b.dependencies.dependencies) {
        var item = b.dependencies.dependencies[k]
        list.push(item);
      }

      b["dependencyTree"] = list;
      
      

      
//      var dependency_tree = {};
//      var root = { "name": b.dependencies.root.id, "children":[ ] };
//      dependency_tree[b.dependencies.root.id] = root;
//      
//      for (var k in b.dependencies.dependencies) {
//        var item = b.dependencies.dependencies[k]
        
//        if (!dependency_tree[item.from.id]) {
//          dependency_tree[item.from.id] = { "name": item.from.id, "children":[ ], "parent": null};
//        }
        
//        if (!dependency_tree[item.to.id]) {
//          dependency_tree[item.to.id] = { "name": item.to.id, "children":[ ], "parent": null };
//        }
        
//        dependency_tree[item.from.id].children.push(dependency_tree[item.to.id]);
//        dependency_tree[item.to.id].parent = item.from.id;
//      }
      
//      b["dependencyTree"] = root;
    }
  }
}


function SnapshotController($scope, $http, $location) {
  console.log("snapshot controller");

  $scope.id = $location.search().id;
  $scope.repository = {}
  $scope.snapshot = {};
  $scope.avatar_url = "";
  
  $http.get("/api/snapshots/" + $scope.id).then(function(res){
    $scope.snapshot = res.data;

    $scope.file_piechart_data = [];
    
    for (var i = 0; i < $scope.snapshot.content.file_details.length; i++) {
      var item = $scope.snapshot.content.file_details[i];
      $scope.file_piechart_data.push({
	"label": item.language,
        "color": colors[i],
	"value": item.count
      });
    }

    barChart("filesBarCanvas", "Files", "Repository files by type", $scope.file_piechart_data);

    $scope.code_piechart_data = [];
    for (var i = 0; i < $scope.snapshot.content.code_line_details.length; i++) {
      var item = $scope.snapshot.content.code_line_details[i];
      console.log(item);
      $scope.code_piechart_data.push({
	"label": item.language,
        "color": colors[i],
	"value": item.count
      });
    }

    barChart("codeBarCanvas", "Code", "Lines of code by type", $scope.code_piechart_data);


    enhanceDependencies($scope.snapshot.build);
  });

  $http.get("/api/repositories/" + $scope.id).then(function(res){
    $scope.repository = res.data;
    $scope.avatar_url = $scope.repository.project.owner.avatar_url
  });
};

kuona_snapshot.controller('SnapshotController',['$scope', '$http', '$location',  SnapshotController]);

kuona_snapshot.directive('dependencyChart', function () {
    return {
        restrict: 'E',
        scope: {
            data: '='
        },
//        template: '<div class="myTemplate"></div>',
        link: function (scope, element, attrs) {
          // We lookup .myTemplate starting from element
          dependencyTreeChart(scope.data, d3.select(element[0]));//.select('.myTemplate'))
            console.log()
        }
    };
});
