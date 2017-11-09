var kuonaQueries = angular.module('kuona.query', [
  'ui.bootstrap'
]);

function initialiseEditor(elementName) {
  var editor = ace.edit(elementName);
  editor.getSession().setMode("ace/mode/json");
  editor.setTheme("ace/theme/chrome");
  editor.getDisplayIndentGuides(true);
  editor.session.setTabSize(2);
  editor.session.setFoldStyle("markbegin");
  editor.setBehavioursEnabled(true);
  editor.session.setUseSoftTabs(true);
  return editor;
}


function QueryController($scope, $http) {
  $scope.sources = [];
  $scope.source = "";
  $scope.editor = initialiseEditor("query-editor");

  // language=JSON
  $scope.editor.setValue("{\n  " +
    "\"query\": {\n  " +
    "  \"match_all\": {}\n  " +
    "}\n" +
    "}");

  $http.get('/api/query').then(function(res) {
    $scope.sources = res.data.sources;
    $scope.source = $scope.sources[0];
  });

  var resetResults = function() {
    $scope.hasError = false;
    $scope.result = "Results appear here";

    $scope.tableData = {
      headers: [],
      values: []
    };
  };

  var processQueryResults = function (data) {
    console.log("Received results " + typeof(data.error) + (typeof(data.error) !== undefined));
    
    if (data.error !== undefined) {
      $scope.result = data.error.description;
      $scope.hasError = true;
    }
      
    if (data.count > 0) {
      $scope.tableData.headers = Object.keys(data.results[0]);
      
      $("#result_grid").jqGrid({
        data: data.results,
        datatype: "local",
        height: 450,
        autowidth: true,
        shrinkToFit: true,
        rowNum: 20,
        rowList: [10, 20, 30],
        colNames: Object.keys(data.results[0]),
        colModel: [
        
          {name: "system", index: "system", sorttype: "text"},
	        {name: "number", index: "number", sorttype: "text"},
	        {name: "name", index: "name", sorttype: "text"},
	        {name: "duration", index: "duration", sorttype: "text"},
	        {name: "source", index: "source", sorttype: "text"},
	        {name: "result", index: "result", sorttype: "text"},
	        {name: "collector", index: "collector", sorttype: "text"},
	        {name: "id", index: "id", sorttype: "text"},
	        {name: "url", index: "url", sorttype: "text"},
	        {name: "jenkins", index: "jenkins", sorttype: "text"},
	        {name: "timestamp", index: "timestamp", sorttype: "text"},
	        {name: "collected", index: "collected", sorttype: "text"}
        ],
        viewrecords: true,
        caption: "Query Results",
        add: true,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
      });
      
      data.results.forEach(function(v) {
        $scope.tableData.values.push(Object.values(v));
      });
    }
  }

  resetResults();
  
  $scope.runQuery = function () {
    resetResults();

    $http.post("/api/query/" + $scope.source.name, $scope.editor.getValue()).then(function(res) {
      processQueryResults(res.data);
    });
  }
}


kuonaQueries.controller('QueryController', ['$scope', '$http', QueryController]);

