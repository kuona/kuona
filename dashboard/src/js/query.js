class QueryController {
  constructor($scope, $http) {
    this.$scope = $scope;
    this.$http = $http;

    this.$scope.sources = [];
    this.$scope.source = "";
    this.$scope.formats = ['json', 'table'];
    this.$scope.result_format = 'json';
    $scope.$watch('response_data', (f, t) => {
      $scope.$evalAsync(() => {
        $('pre code').each((i, block) => {
          hljs.highlightBlock(block);
        });
      });
    })
  }

  static initialiseEditor(elementName) {
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

  initialise() {
    this.$scope.editor = QueryController.initialiseEditor("query-editor");
    // language=JSON
    this.$scope.editor.setValue("{\n  " +
      "\"query\": {\n  " +
      "  \"match_all\": {}\n  " +
      "}\n" +
      "}");

    this.$http.get('/api/query').then((res) => {
      this.$scope.sources = res.data.sources;
      this.$scope.source = res.data.sources[0];
    });

    this.resetResults();
    this.$scope.runQuery = () => this.runQuery();
  }

  runQuery() {
    this.resetResults();

    this.$http.post("/api/query/" + this.$scope.source.name, this.$scope.editor.getValue()).then(res => {
      this.processQueryResults(res.data);
    });
  }

  resetResults() {
    this.$scope.response_data = {};
    this.$scope.hasError = false;
    this.$scope.result = "Results appear here";
  }

;

  processQueryResults(data) {
    this.$scope.response_data = data;

    if (data.error !== undefined) {
      this.$scope.result = data.error.description;
      this.$scope.hasError = true;
    } else if (data.count > 0) {
      const model = this.schemaToModel(data.schema);


      $("#result_grid").jqGrid({
        data: this.enhanceResults(data.schema, data.results),
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

  schemaToModel(schema) {
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

  enhanceResults(schema, values) {
    let enhanced = [];

    values.forEach((v) => {
      enhanced.push(this.enhance(schema, v));
    });

    return enhanced;
  }

  enhance(schema, m) {
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

}

const kuonaQueries = angular.module('kuona.query', [
  'ui.bootstrap'
]);

function queryController($scope, $http) {
  let controller = new QueryController($scope, $http);

  controller.initialise();
}

kuonaQueries.controller('QueryController', ['$scope', '$http', queryController]);

