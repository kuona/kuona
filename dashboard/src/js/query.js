class KuonaQuery {
  initialiseEditor(elementName) {
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
}


const kuonaQueries = angular.module('kuona.query', [
  'ui.bootstrap'
]);

function initialiseEditor(elementName) {
  let editor = ace.edit(elementName);
  editor.getSession().setMode("ace/mode/json");
  editor.setTheme("ace/theme/chrome");
  editor.getDisplayIndentGuides(true);
  editor.session.setTabSize(2);
  editor.session.setFoldStyle("markbegin");
  editor.setBehavioursEnabled(true);
  editor.session.setUseSoftTabs(true);
  return editor;
}


function schemaToModel(schema) {
  let result = {
    model: [],
    names: []
  };

  let keys = Object.keys(schema);

  keys.forEach((k) => {
    var columnType = schema[k];

    switch (columnType) {
      case "timestamp":
        result.model.push({name: k, index: k, sorttype: "date", formatter: "date"});
        result.names.push(k);
        break;
      case "long":
        result.model.push({name: k, index: k, sorttype: "integer"});
        result.names.push(k);
        break;
      case "string":
        result.model.push({name: k, index: k, sorttype: "text"});
        result.names.push(k);
        break;
      case "object":
    }
  });
  return result;
}

function enhance(schema, m) {
  var keys = Object.keys(schema);
  var result = {};

  keys.forEach(function (k) {
    var columnType = schema[k];

    switch (columnType) {
      case "timestamp":
        result[k] = new Date(m[k]);
        break;
      case "long":
        result[k] = m[k];
        break;
      case "string":
        result[k] = m[k];
        break;
      case "object":
    }
  });
  return result;
}

function enhanceResults(schema, values) {
  let enhanced = [];

  values.forEach(function (v) {
    enhanced.push(enhance(schema, v));
  });

  return enhanced;
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

  $http.get('/api/query').then(function (res) {
    $scope.sources = res.data.sources;
    $scope.source = $scope.sources[0];
  });

  var resetResults = function () {
    $scope.hasError = false;
    $scope.result = "Results appear here";
  };

  var processQueryResults = function (data) {
    console.log("Received results " + typeof(data.error) + (typeof(data.error) !== undefined));

    if (data.error !== undefined) {
      $scope.result = data.error.description;
      $scope.hasError = true;
    } else if (data.count > 0) {
      const model = schemaToModel(data.schema);


      $("#result_grid").jqGrid({
        data: enhanceResults(data.schema, data.results),
        datatype: "local",
        height: 450,
        autowidth: true,
        shrinkToFit: true,
        rowNum: 20,
        rowList: [10, 20, 30],
        colNames: model.names,
        colModel: model.model,
        viewrecords: true,
        caption: "Query Results",
        add: true,
        edit: true,
        addtext: 'Add',
        edittext: 'Edit',
        hidegrid: false
      });
    }
  }

  resetResults();

  $scope.runQuery = () => {
    resetResults();

    $http.post("/api/query/" + $scope.source.name, $scope.editor.getValue()).then(res => {
      processQueryResults(res.data);
    });
  }
}


kuonaQueries.controller('QueryController', ['$scope', '$http', QueryController]);

