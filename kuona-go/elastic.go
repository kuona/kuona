package main

import (
	"encoding/json"
	"errors"
	"fmt"
	"log"
	"net/http"
	"strings"
)

type ElasticIndex struct {
	Engine      ElasticSearchEngine
	name        string
	esName      string
	description string
}
type ElasticSearchEngine struct {
	Url     string
	Indices map[string]ElasticIndex
}

func (index ElasticIndex) Name() string {
	return "kuona-" + index.name
}

func (index ElasticIndex) Count() (interface{}, error) {
	url := index.Engine.Url + "/" + index.Name() + "/_count"
	log.Printf("Requesting %s", url)
	resp, err := http.Get(url)
	if err != nil {
		return nil, err
	} else {
		var content interface{}
		_ = json.NewDecoder(resp.Body).Decode(&content)
		return content, nil
	}
}

type ElasticSearchResponse struct {
	Hits struct {
		Total int         `json:"total"`
		Hits  interface{} `json:"hits"`
	} `json:"hits"`
}

type SearchResult map[string]interface{}

func mapSearchResultsToItemsAndCount(data interface{}) interface{} {
	items := mapSelect(data, "hits", "hits").([]interface{})
	hits := mapSelect(data, "hits").(map[string]interface{})
	count := hits["total"].(int)
	resultItems := []SearchResult{}
	for _, item := range items {
		resultItem := SearchResult{}
		itemMap := item.(map[string]interface{})
		resultItem["id"] = itemMap["_id"]
		source := itemMap["_source"].(map[string]interface{})
		for k, v := range source {
			resultItem[k] = v
		}
		resultItems = append(resultItems, resultItem)
	}
	return struct {
		Count int         `json:"count"`
		Items interface{} `json:"items"`
	}{Count: count,
		Items: resultItems,}

}

func (index ElasticIndex) Latest() (interface{}, error) {
	url := index.Engine.Url + "/" + index.Name() + "/_search"
	log.Printf("Requesting %s", url)
	resp, err := http.Get(url)
	if err != nil {
		return resp, err
	} else {
		var response ElasticSearchResponse
		_ = json.NewDecoder(resp.Body).Decode(&response)

		items := response.Hits.Hits.([]interface{})
		resultItems := []SearchResult{}
		for _, item := range items {
			resultItem := SearchResult{}
			itemMap := item.(map[string]interface{})
			resultItem["id"] = itemMap["_id"]
			source := itemMap["_source"].(map[string]interface{})
			for k, v := range source {
				resultItem[k] = v
			}
			resultItems = append(resultItems, resultItem)
		}
		return struct {
			Count int         `json:"count"`
			Items interface{} `json:"items"`
		}{Count: response.Hits.Total,
			Items: resultItems,}, nil
	}
}

func (index ElasticIndex) Search(query string) (interface{}, error) {
	url := index.Engine.Url + "/" + index.Name() + "/_search"
	log.Printf("Requesting %query", url)
	resp, err := http.Post(url, "application/json", strings.NewReader(query))

	if err != nil {
		return resp, err
	} else {
		var response interface{}
		_ = json.NewDecoder(resp.Body).Decode(&response)
		return response, nil
	}
}

func (index ElasticIndex) Get(id string) (interface{}, error) {
	url := index.Engine.Url + "/" + index.Name() + "/" + id
	resp, err := http.Get(url)

	if err != nil {
		return resp, err
	} else {
		var response interface{}
		_ = json.NewDecoder(resp.Body).Decode(&response)
		return response, nil
	}

}

func (e ElasticSearchEngine) Index(indexName string) (ElasticIndex, error) {
	if val, ok := e.Indices[indexName]; ok {
		return val, nil
	} else {
		log.Printf("Index %v not found", indexName)
	}
	return ElasticIndex{}, errors.New(fmt.Sprintf("Index %v not found", indexName))
}

func NewIndex(name string, engine ElasticSearchEngine) ElasticIndex {
	return ElasticIndex{name: name,
		esName:      name,
		description: "Build data - software construction data read from Jenkins",
		Engine:      engine,
	}
}

func NewElasticSearchEngine(url string) ElasticSearchEngine {
	engine := ElasticSearchEngine{Url: url, Indices: map[string]ElasticIndex{}}
	engine.Indices["builds"] = NewIndex("builds", engine)
	engine.Indices["repositories"] = NewIndex("repositories", engine)
	engine.Indices["commits"] = NewIndex("vcs-commit", engine)
	engine.Indices["code"] = NewIndex("vcs-content", engine)
	engine.Indices["snapshots"] = NewIndex("snapshots", engine)
	engine.Indices["collectors"] = NewIndex("collectors", engine)

	return engine
}

type ElasticStats struct {
	Indices struct {
		Indices interface{} `json:"indices"`
	} `json:"indices"`
}

func (e ElasticSearchEngine) Stats() (ElasticStats, error) {
	resp, err := http.Get(e.Url + "/_stats")
	if err != nil {
		return ElasticStats{}, err
	} else {
		var content ElasticStats
		_ = json.NewDecoder(resp.Body).Decode(&content)
		return content, nil
	}
}
func (e ElasticSearchEngine) Info() (interface{}, error) {
	resp, err := http.Get(e.Url)
	if err != nil {
		return nil, err
	} else {
		var content interface{}
		_ = json.NewDecoder(resp.Body).Decode(&content)
		return content, nil
	}
}

func (e ElasticSearchEngine) ClusterHealth() (interface{}, error) {
	resp, err := http.Get(e.Url + "/_cluster/health")
	if err != nil {
		return nil, err
	} else {
		var content interface{}
		_ = json.NewDecoder(resp.Body).Decode(&content)
		return content, nil
	}
}
