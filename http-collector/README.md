# http-collector

A command line utility for assessing the health of HTTP services that
expose a *status* endpoint and optionally an *info* endpoint for
details of the running service.

As well as testing basic connectivity http-collector also reads keyed
values from any json content returned from the query. This alows a
service to advertise that it is running but that there may be a
problem with its own connectivity or health.

## Building

http-collector is written in clojure and uses lein for development

Download dependencies with:

	lein deps

Test the app with:

	lein midje

During development I like to run

	lein midje :autotest

It avoids a startup time and gives really fast feedback

## Usage ##

http-collector is still in development run

	lein run

to get the latest details of command line switches and then

	lein run -- [options]

For production build an uberjar

	lein uberjar

java -jar uberjar [options]

