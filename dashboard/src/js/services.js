function registerKuonaAngularServices(module) {
  module.factory('$health_check', ['$http', ($http) => {
    return {
      healthCheckList: () => $http.get("/api/health-checks"),
      healthCheckSnapshotList: () => $http.get("/api/health-checks/snapshots"),
      addHealthCheck: (type, tags, endpoints) => {
        let request = {
          type: type,
          tags: commaListToArray(tags, ','),
          endpoints: commaListToArray(endpoints, '\n')
        };
        return $http.post("/api/health-checks", request);
      },
      deleteHeathCheck: (id) => $http.delete("/api/health-checks/" + id)
    }
  }]);


}
