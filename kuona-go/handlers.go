package main

import (
	"encoding/json"
	"fmt"
	"github.com/gorilla/mux"
	"net/http"
)

type ServiceStatus struct {
	Status string      `json:"status"`
	Store  interface{} `json:"store"`
}
type IndicesResponse struct {
	Indices interface{} `json:"indices"`
}

func IndicesHandler(w http.ResponseWriter, r *http.Request) {
	stats, err := SearchEngine.Stats()
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
	} else {
		_ = json.NewEncoder(w).Encode(IndicesResponse{Indices: stats.Indices})
	}
}

type VersionResponse struct {
	Version string `json:"version"`
}

type InfoResponse struct {
	Api     VersionResponse `json:"kuona_api"`
	Clojure VersionResponse `json:"clojure"`
	Elastic interface{}     `json:"elastic_search"`
}

func InfoHandler(w http.ResponseWriter, r *http.Request) {
	info, err := SearchEngine.Info()
	if err != nil {
		_ = json.NewEncoder(w).Encode(InfoResponse{Api: VersionResponse{"1.2"},
			Clojure: VersionResponse{"golang"},
			Elastic: struct{ Error string }{fmt.Sprintf("%v", err)}})

	} else {
		_ = json.NewEncoder(w).Encode(InfoResponse{Api: VersionResponse{"1.2"},
			Clojure: VersionResponse{"golang"},
			Elastic: info})
	}
}
func StatusHandler(w http.ResponseWriter, r *http.Request) {
	content, err := SearchEngine.ClusterHealth()
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
	} else {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "ok", Store: content})
	}
}

type ApiSearchResponse struct {
	Results interface{}   `json:"results"`
	Count   int           `json:"count"`
	Errors  []interface{} `json:"errors"`
}

func RepositoriesHandler(w http.ResponseWriter, r *http.Request) {
	index, err := SearchEngine.Index("repositories")
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
	} else {
		resp, err := index.Latest()
		if err != nil {
			_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
		} else {
			_ = json.NewEncoder(w).Encode(resp)
		}
	}
}
func GetRepositoryCommitsHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]
	index, err := SearchEngine.Index("commits")
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
	} else {
		query := fmt.Sprintf(`{
  "query": {
    "term": {
      "repository_id": "%s"
    }
  },
  "sort": [
    {
      "timestamp": {
        "order": "desc"
      }
    }
  ],
  "size": 100
}`, id)
		resp, err := index.Search(query)
		if err != nil {
			_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
		} else {
			_ = json.NewEncoder(w).Encode(mapSearchResultsToItemsAndCount(resp))
		}
	}
}

func GetRepositoryHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]

	index, err := SearchEngine.Index("repositories")
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
	} else {
		resp, err := index.Get(id)
		if err != nil {
			_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
		} else {

			_ = json.NewEncoder(w).Encode(mapSelect(resp, "_source"))
		}
	}
}
func GetSnapshotHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	id := vars["id"]

	index, err := SearchEngine.Index("snapshots")
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
	} else {
		resp, err := index.Get(id)
		if err != nil {
			_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: nil})
		} else {

			_ = json.NewEncoder(w).Encode(mapSelect(resp, "_source"))
		}
	}
}

func RepositoriesCountHandler(w http.ResponseWriter, r *http.Request) {
	index, err := SearchEngine.Index("repositories")
	content, _ := index.Count()
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: "kuona-repositories"})
	} else {
		_ = json.NewEncoder(w).Encode(content)
	}
}
func MetricsCountHandler(w http.ResponseWriter, r *http.Request) {
	vars := mux.Vars(r)
	indexName := vars["index"]
	index, err := SearchEngine.Index(indexName)
	content, _ := index.Count()
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: "kuona-repositories"})
	} else {
		_ = json.NewEncoder(w).Encode(content)
	}
}

func BuildToolsHandler(w http.ResponseWriter, r *http.Request) {
	query := `{
  "size": 0,
  "aggregations": {
    "builder": {
      "terms": {
        "field": "build.builder"
      }
    }
  }
}`
	index, err := SearchEngine.Index("snapshots")
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: "kuona-repositories"})
	}

	response, err := index.Search(query)
	response = mapSelect(response, "aggregations", "builder")
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: "kuona-repositories"})
	} else {
		_ = json.NewEncoder(w).Encode(response)
	}

}

func mapSelect(response interface{}, selectors ...string) interface{} {
	current := response

	for _, selector := range selectors {
		result := current.(map[string]interface{})
		current = result[selector]
	}
	return current
}

func CollectorActivitiesHandler(w http.ResponseWriter, r *http.Request) {
	index, err := SearchEngine.Index("collectors")
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: "kuona-repositories"})
	}
	content, err := index.Latest()
	if err != nil {
		_ = json.NewEncoder(w).Encode(ServiceStatus{Status: "error", Store: "kuona-repositories"})
	} else {
		// Map to list of _source with the id of the document as an id field
		_ = json.NewEncoder(w).Encode(content)
	}
}

func SearchHandler(w http.ResponseWriter, r *http.Request) {
	resp, err := http.Get("http://localhost:9200/kuona-vcs-content/content/_search")
	if err != nil {
		panic(err)
	}
	var content ElasticSearchResponse
	_ = json.NewDecoder(resp.Body).Decode(&content)
	x := ApiSearchResponse{Results: content.Hits.Hits, Count: content.Hits.Total, Errors: nil}
	_ = json.NewEncoder(w).Encode(x)
	defer resp.Body.Close()
}
