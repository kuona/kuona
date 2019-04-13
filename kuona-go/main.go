package main

import (
	"github.com/gobuffalo/packr"
	"github.com/gorilla/mux"
	"log"
	"net/http"
)

var SearchEngine ElasticSearchEngine

func main() {
	SearchEngine = NewElasticSearchEngine("http://localhost:9200")

	box := packr.NewBox("./site")
	router := mux.NewRouter()

	apiRouter := router.PathPrefix("/api").Subrouter()
	apiRouter.HandleFunc("/info", InfoHandler).Methods("GET")
	apiRouter.HandleFunc("/status", StatusHandler).Methods("GET")
	apiRouter.HandleFunc("/search", SearchHandler).Methods("GET")
	apiRouter.HandleFunc("/repositories", RepositoriesHandler)
	apiRouter.HandleFunc("/repositories/{id}", GetRepositoryHandler)
	apiRouter.HandleFunc("/repositories/{id}/commits", GetRepositoryCommitsHandler)
	apiRouter.HandleFunc("/snapshots/{id}", GetSnapshotHandler)
	apiRouter.HandleFunc("/repositories/count", RepositoriesCountHandler)
	apiRouter.HandleFunc("/indices", IndicesHandler)
	apiRouter.HandleFunc("/repositories/count", RepositoriesCountHandler)
	apiRouter.HandleFunc("/metrics/{index}/count", MetricsCountHandler)
	apiRouter.HandleFunc("/collectors/activities", CollectorActivitiesHandler)
	apiRouter.HandleFunc("/build/tools", BuildToolsHandler)


	http.Handle("/", router)
	router.PathPrefix("/").Handler(http.FileServer(box))
	log.Fatal(http.ListenAndServe(":8080", router))
}
