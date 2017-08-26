# Snapshot Collector

The snapshot collector reads head revision code and build metrics for a repository and uploads a JSON document through the kuona-api.

The current list of repositories is read through the API. Each repository is then cloned up updated into a local workspace for analysis.

## Code analysis

The repository files are scanned using CLOC. Each file is assigned a language and scanned for text. Lines of code, comments and blank lines are recorded.

## Commit history

The repository commit history is collected and stored through the API

## Build systems

The repository is scanned for module build scripts (maven, groovy etc.)

For Maven projects the module dependencies and other details are captured. 

## Usage

Run:

  `lein run -- [options]`

Build and run:

  `lein uberjar`
  `java -jar kuona-snapshot-collector-0.0.1-standalone.jar [options]`

```
Options:
  -a, --api-url URL     http://dashboard.kuona.io      The URL of the back end Kuona API
  -f, --force                                          Force updates. Ignore collected snapshots and re-create. Use when additional data is collected as part of a snapshot
  -w, --workspace PATH  /Volumes/data-drive/workspace  workspace folder for git clones and source operations
  -h, --help                                           Display this message and exit
```

