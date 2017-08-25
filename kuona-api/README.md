# Kuona API

FIXME

## Prerequisites

You will need [Leiningen][] 2.0.0 or above installed.

[leiningen]: https://github.com/technomancy/leiningen

## Testing

	lein midje
or
	lein midje :autotest

Autotest is a great feature. Every code change is immediately loaded and tests run - really fast feedback.

## Running

To start a web server for the application, run:

	lein run -- -c kuona-properties.json
or
	lein uberjar
	java -jar target/environment-service-0.0.1-standalone.jar -c kuona-properties.json

For local development, the [kuona-project](https://github.com/kuona/kuona-project) with a dev-config. You can use the following command to run the service for development

  lein run -- -c ../dev-config.json

Copyright © 2016 Kuona.io
