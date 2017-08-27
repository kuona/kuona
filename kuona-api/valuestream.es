
DELETE /kuona-valuestream

PUT /kuona-valuestream
{
  "mappings": {
    "valuestream": {
      "properties": {
        "artifact": {
          "properties": {
            "id": { "type": "keyword" },
            "version":  { "type": "keyword" }
          }
        },
        "build": {
          "properties": {
            "build_url":  { "type": "keyword" },
            "builder":    { "type": "keyword" },
            "duration":   { "type": "long"  },
            "timestamp":  { "type": "date"  }
          }
        },
        "deployments": {
          "properties": {
            "duration": { "type": "long" },
            "environment": {
              "properties": {
                "name": { "type": "keyword" }
              }
            },
            "timestamp": { "type": "date" }
          }
        },
        "id": { "type": "keyword" },
        "lead_time": { "type": "long"  },
        "name":  { "type": "keyword" },
        "timestamp": { "type": "date" }
      }
    }
  }
}

GET /kuona-valuestream/valuestream/_mapping

GET /kuona-valuestream/valuestream/_search
{
  "sort" : [ { "timestamp" : {"order" : "desc"}}]
}


GET /kuona-valuestream/valuestream/_search
{
  "sort" : [
    { "timestamp" : {"order" : "asc"}}]
}

  
GET /kuona-valuestream/valuestream/_search
{
  "query": {
    "term" : { "name" : "unique-name" }
  }
}

GET /kuona-valuestream/valuestream/_search
{
  "sort" : [ { "timestamp" : {"order" : "asc"}}],
  "query": {
    "bool": {
      "filter": [
        { "term" : { "name" : "unique-name" }}
      ]
    }
  }
}


GET /kuona-valuestream/valuestream/_search
{
  "size": 0,
  "aggs": {
    "distinct_names": {
      "terms": {
        "field": "name",
        "size": 1000
      }
    }
  }
}

POST /kuona-valuestream/valuestream
{
  "id": "a99eeaac-b45a-498d-bbc1-671fb2606f80",
  "name": "unique-name",
  "timestamp": "2017-08-21T17:44:35Z",
  "artifact": {
    "id": "some-unique-identifier",
    "version": "1.0.1"
  },
  "lead_time": 91237842,
  "commits": [
    
  ],
  "build": {
    "timestamp": "2017-08-21T17:44:35Z",
    "duration": 234234,
    "builder": "Jenkins",
    "build_url": "http://jenkins.com/stage/job"
  },
  "deployments": [
    {
      "timestamp": "2017-08-21T17:44:35Z",
      "environment": {
        "name": "PROD"
      },
      "duration": 234523
    }
  ]
}

POST /kuona-valuestream/valuestream/742c8ffd-ef3a-4648-b125-d2e7f9e6cd52
{
  "id": "742c8ffd-ef3a-4648-b125-d2e7f9e6cd52",
  "name": "unique-name",
  "timestamp": "2017-08-21T17:46:00Z",
  "artifact": {
    "id": "some-unique-identifier",
    "version": "1.0.1"
  },
  "lead_time": 91237842,
  "commits": [
    
  ],
  "build": {
    "timestamp": "2017-08-21T17:46:00Z",
    "duration": 234234,
    "builder": "Jenkins",
    "build_url": "http://jenkins.com/stage/job"
  },
  "deployments": [
    {
      "timestamp": "2017-08-21T17:46:00Z",
      "environment": {
        "name": "PROD"
      },
      "duration": 234523
    }
  ]
}

POST /kuona-valuestream/valuestream/742c8ffd-ef3a-4648-b125-d2e7f9e6cd53
{
  "id": "742c8ffd-ef3a-4648-b125-d2e7f9e6cd53",
  "name": "unique-other-name",
  "timestamp": "2017-08-22T17:46:00Z",
  "artifact": {
    "id": "some-unique-identifier",
    "version": "1.0.1"
  },
  "lead_time": 91237842,
  "commits": [
    
  ],
  "build": {
    "timestamp": "2017-08-22T17:46:00Z",
    "duration": 234234,
    "builder": "Jenkins",
    "build_url": "http://jenkins.com/stage/job"
  },
  "deployments": [
    {
      "timestamp": "2017-08-22T17:46:00Z",
      "environment": {
        "name": "PROD"
      },
      "duration": 234523
    }
  ]
}
