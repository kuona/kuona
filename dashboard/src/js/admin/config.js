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
      data: {pageTitle: 'Admin'}
    })
    .state('index.add', {
      url: "/add",
      templateUrl: "views/add.html",
      data: {pageTitle: 'Admin - Add Source'}
    })
    .state('index.minor', {
      url: "/minor",
      templateUrl: "views/minor.html",
      data: {pageTitle: 'Example view'}
    })
    .state('index.crawlers', {
      url: "/crawlers",
      templateUrl: "views/crawlers.html",
      data: {pageTitle: 'Repository Crawlers'}
    })
    .state('index.new-github-repository', {
      url: "/github/repository",
      templateUrl: "views/github/repository.html",
      data: {pageTitle: 'Add GitHub Repository'}
    })
    .state('index.new-jenkins-server', {
      url: "jenkins/server",
      templateUrl: "views/jenkins/server.html",
      data: {pageTile: "Add Jenkins Build Server"}
    })
    .state('index.build-servers', {
      url: 'build/servers',
      templateUrl: 'views/build-servers.html',
      data: {pageTitle: 'Build Servers'}
    })
    .state('index.new-github-collector', {
      url: 'github/collector',
      templateUrl: 'views/github/collector.html',
      data: {pageTitle: 'GitHub Collector'}
    })
    .state('index.new-tfs-collector', {
      url: 'tfs/collector',
      templateUrl: 'views/tfs/collector.html',
      data: {pageTitle: 'TFS/Visual Studio Collector'}
    })
    .state('index.new-search-code-server', {
      url: 'integration/searchcode',
      templateUrl: 'views/integration/searchcode.html',
      data: {pageTitle: 'Searchcode integration'}
    })
    .state('index.new-health-check', {
      url: 'health-check',
      templateUrl: 'views/health-check.html',
      data: {pageTitle: 'New Health Check'}
    });
}

angular
  .module('kuona-admin')
  .config(config)
  .run(function ($rootScope, $state) {
    $rootScope.$state = $state;
  });
