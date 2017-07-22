# jenkins-collector

The Kuona Jenkins collector downloads build data from Jenkins CI
servers and stores a build summary into a local elasticsearch
instance.

From there Kibana or anonther visualisation tool can be used to
generate visuals build success, build times etc.

The collector is configured using a JSON format configuration file.

```json
{
  "jenkins": [
    {
      "url": "http://jenkins.example.com/",
      "credentials": {
        "username": "<username>",
        "password": "<api-key>"
      }
    }
  ]
}
```

## Building

The Jenkins collector is writting in clojure.

    lein uberjar

will build a standalone jar.

## Usage

Running as a developer

    lein run

or

    lein run - -c properties.json

By default the collector reads a `properties.json` file in the current
directory.

