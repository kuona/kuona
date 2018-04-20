function config($stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {
  $urlRouterProvider.otherwise("/index/main");

  $ocLazyLoadProvider.config({
    // Set to true if you want to see what and when is dynamically loaded
    debug: false
  });

  $stateProvider
    .state('index', {
      abstract: true,
      url: "/index",
      templateUrl: "views/common/content.html",
    })
    .state('index.main', {
      url: "/main",
      templateUrl: "views/main.html",
      data: {pageTitle: 'Dashboards'}
    })
    .state('index.new-dashboard', {
      url: "/new-dashboard",
      templateUrl: "views/dashboard.html",
      data: {pageTitle: 'New Dashboard'}

    });
}

angular
  .module('kuona-dashboards')
  .config(config)
  .run(function ($rootScope, $state) {
    $rootScope.$state = $state;
  });
