var kuonaQueries = angular.module('kuona.query', [
  'ui.bootstrap'
]);


function QueryController($scope, $http) {

  $scope.result = "Results appear here";
  var editor = ace.edit("query-editor");
  editor.getSession().setMode("ace/mode/json");
  editor.setTheme("ace/theme/chrome");
  editor.getDisplayIndentGuides(true);
  editor.session.setTabSize(2);
  editor.session.setFoldStyle("markbegin");
  editor.setBehavioursEnabled(true);
  editor.session.setUseSoftTabs(true);

  $scope.editor = editor;

  // language=JSON
  editor.setValue("{\n  " +
    "\"query\": {\n  " +
    "  \"match_all\": {}\n  " +
    "}\n" +
    "}");

  $scope.runQuery = function () {
    console.log("Run Query");
    console.log($scope.editor.getValue());

    $http.post("/api/query/commits", $scope.editor.getValue()).then(function(res) {
      console.log("Received results");
      $scope.result = res;
    });
  }
}


kuonaQueries.controller('QueryController', ['$scope', '$http', QueryController]);

