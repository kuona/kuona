/**
 * pageTitle - Directive for set Page title - mata title
 */
function pageTitle($rootScope, $timeout) {
  return {
    link: function (scope, element) {
      var listener = function (event, toState, toParams, fromState, fromParams) {
        // Default title - load on Dashboard 1
        var title = 'Kuona | IT analytics dashboard';
        // Create your own title pattern
        if (toState.data && toState.data.pageTitle) title = 'Kuona | ' + toState.data.pageTitle;
        $timeout(function () {
          element.text(title);
        });
      };
      $rootScope.$on('$stateChangeStart', listener);
    }
  }
}

function registerModuleDirectives(module) {

  module.directive('dashboardPanel', function () {
    return {
      restrict: 'E',
      link: function (scope, element, attrs) {
        scope.contentUrl = '/directives/' + attrs.type + '-panel.html';
        scope.build = attrs.data;
        attrs.$observe("ver", function (v) {
          scope.contentUrl = '/directives/' + v + '-panel.html';
          scope.build = attrs.data;
        });
      },
      template: '<div ng-include="contentUrl"></div>'
    };
  });

  module.directive('buildStatusPanel', function () {
    return {
      restrict: 'E',
      scope: {
        build: "=data"
      },
      templateUrl: '/directives/build-status-panel.html'
    };
  });

  module.directive('countMetricPanel', function () {
    return {
      restrict: 'E',
      scope: {
        metric: "=data"
      },
      templateUrl: '/directives/count-metric-panel.html'
    };
  });

  module.directive('pieChartPanel', function () {
    return {
      restrict: 'E',
      scope: {
        data: "=data"
      },
      templateUrl: '/directives/pie-chart-panel.html'
    };
  });

  module.directive('barChartPanel', function () {
    return {
      restrict: 'E',
      scope: {
        data: "=data"
      },
      templateUrl: '/directives/bar-chart-panel.html'
    };
  });

  module.directive('activityFeedPanel', function () {
    return {
      restrict: 'E',
      scope: {
        data: "=data"
      },
      templateUrl: '/directives/activity-feed-panel.html'
    };
  });
  module.directive('healthCheckPanel', function () {
    return {
      restrict: 'E',
      scope: {
        data: "=data"
      },
      templateUrl: '/directives/health-check-panel.html'
    };
  });

}
