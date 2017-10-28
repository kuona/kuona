var kuonaQueries = angular.module('kuona.query', [
  'ui.bootstrap'
]);


function QueryController($scope, $http) {

  var editor = ace.edit("query-editor");
  editor.getSession().setMode("ace/mode/json");
  editor.setTheme("ace/theme/chrome");
  editor.getDisplayIndentGuides(true);
  editor.session.setTabSize(2);
  editor.session.setFoldStyle("markbegin");
  editor.setBehavioursEnabled(true);
   editor.session.setUseSoftTabs(true);


  // language=JSON
  editor.setValue("{\n  " +
    "\"query\": {\n  " +
    "  \"match_all\": {}\n  " +
    "}\n" +
    "}");
}


kuonaQueries.controller('QueryController', ['$scope', '$http', QueryController]);

