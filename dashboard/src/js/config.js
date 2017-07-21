function config($stateProvider, $urlRouterProvider, $ocLazyLoadProvider) {
    $urlRouterProvider.otherwise("/index/environment-dashboard");

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
            data: {pageTitle: 'Home'}
        })
        .state('index.environment-dashboard', {
            url: "/environment-dashboard",
            controller: "environmentDashboardController",
            templateUrl: "views/environment-dashboard.html",
            data: {pageTitle: "Environment Dashboard"},
        });
}

kuona
    .config(config)
    .run(function ($rootScope, $state) {
        $rootScope.$state = $state;
    });
