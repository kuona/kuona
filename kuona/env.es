GET /

DELETE /kuona

PUT /kuona-env/comment/1
{
  "timestamp": "2016-01-01",
  "user": "@gbrooks",
  "entry": "Sone text to describe what happened",
  "useability": "available"
}

  

PUT /kuona-env
{
  "mappings" : {
    "environments" : {
      "properties" : {
        "environments" : {
          "properties" : {
            "name" : {
              "type" : "string"
            }
          }
        },
        "journal" : {
          "properties" : {
            "useability" : {
              "type" : "string",
	      "index": "not_analyzed"
            },
            "entry" : {
              "type" : "string"
            },
            "timestamp" : {
              "type" : "long"
            },
            "user" : {
              "type" : "string",
	      "index": "not_analyzed"
            }
          }
        },
        "name" : {
          "type" : "string",
	  "index": "not_analyzed"
        },
        "status" : {
          "type" : "string",
	  "index": "not_analyzed"
        },
        "tags" : {
          "type" : "string",
	  "index": "not_analyzed"
        }
      }
    },
    "journal" : {
      "properties" : {
        "useability" : {
          "type" : "string",
	  "index": "not_analyzed"
        },
        "entry" : {
          "type" : "string"
        },
        "timestamp" : {
          "type" : "date"
        },
        "user" : {
          "type" : "string",
	  "index": "not_analyzed"
        },
	"tags" : {
          "type" : "string",
	  "index": "not_analyzed"
        }
      }
    }
  }
}




PUT /kuona/environments/dev
{ "name": "DEV" }
  
PUT /kuona/environments/QA
{ "name": "QA" }

PUT /kuona-env/environments/PERF
{ "name": "PERF",
  "status": "up",
  "journal": [
    { "timestamp": 3,
      "user": "@gbrooks",
      "entry": "Sone text to describe what happened",
      "useability": "available"
    },
    { "timestamp": 2,
      "user": "@gbrooks",
      "entry": "Sone text to describe what happened",
      "useability": "available"
    },
    { "timestamp": 1,
      "user": "@gbrooks",
      "entry": "Sone text to describe what happened",
      "useability": "available"
    }
  ],
  "tags": [
    "qa",
    "shared",
    "team-rubicon",
    "blue-green"
  ]
}

PUT /kuona/journal/1
{ "timestamp": 1,
  "user": "@gbrooks",
  "entry": "Sone text to describe what happened",
  "useability": "available",
  "tags": [
    "qa",
    "shared",
    "team-rubicon",
    "blue-green"
  ]
}

PUT /kuona/journal/2
{ "timestamp": "2016-08-16",
  "user": "@gbrooks",
  "entry": "Sone text to describe what happened",
  "useability": "available",
  "tags": [
    "qa",
    "shared",
    "team-rubicon",
    "blue-green"
  ]
}


GET /kuona/journal/_search

GET /kuona/environments/PERF

GET /kuona/environments/_search

GET /kuona/environments/_search?q=tags:foo

GET /_mapping/kuona,environments?pretty

GET /_all/_mapping

GET /kuona-env/comments/_search?pretty

GET /kuona-env/comments/_search?q=tags:ENVIRONMENT,TEST1&pretty
