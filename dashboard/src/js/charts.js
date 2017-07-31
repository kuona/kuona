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

function barChart(canvas, title, subtitle, data) {
  var dataValues = [];
  var dataColors = [];
  var dataLabels = [];

  for (var i = 0; i < data.length; i++) {
    var item = data[i];
    dataValues.push(item.value);
    dataColors.push(item.color);
    dataLabels.push(item.label);
  }

  var ctx = canvas.getContext("2d");
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

var dependencyTreeChart = function(treeData, treeContainer) {
  var width = 600;
  var height = 600;
  var svg = treeContainer.append("svg").attr("width", width).attr("height", height);
  var g = svg.append("g").attr("transform", "translate(150,0)");

  var tree = d3.cluster()
      .size([height, width - 400]);

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
          + "C" + (d.parent.y + 10) + "," + d.x
          + " " + (d.parent.y + 10) + "," + d.parent.x
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
    .text(function(d) { return d.id; });
//    .text(function(d) { return d.id.substring(d.id.lastIndexOf(".") + 1); });
}

