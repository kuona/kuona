![Kuona Project Logo]:(http://dashboard.kuona.io/favicon.png)
# kuona-project
Kuona is the [Shona](https://en.wikipedia.org/wiki/Shona_language) word that means "To see". This project is about providing valuable actionable insight into software delivery.

The Kuona-project is about deepening our understanding of how we can deliver value through software so that we can be more effective.

software gets delivered into production.

Kuona is a data collection and visualisation tool for software development. Check out http://kuona.io for background on the project.

Currently there are no packages for download but there is a hosted example for some public open source projects at http://dashboard.kuona.io
## Design - How it works
Kuona uses collectors: command line tools and daemons to collect data from software development systems like Git, Jenkins as well as data from environments.

These Collectors post data through the kuona-api RESTful interface into an [Elasticsearch](https://www.elastic.co/products/elasticsearch) backing store. Elasticsearch is particularly adept at handling both documents and time-series data. Each data type is given its own index.

 /dashboard contains a AngularJS web application. We are currently using GitHub repositories as sample data for collection and analysis. http://dashboard.kuona.io/repositories provide search and snapshot views of these projects.

## How you can help
* Provide feedback and bug reports by trying Kuona a try in your environment 
* Join the development [Google Group](https://groups.google.com/forum/#!forum/kuona-dev), ask questions and offer suggestions for features.

## Getting started
Software projects should be easy to join. Ideally getting up and running as a developer should be very simple and quick. Long winded manual installations are error prone and a waste of everyone's time. If you find problems with the developer setup please raise an issue so we can address it.

Currently the installation scripts have only been tested on OS X/macOS and rely on [homebrew](https://brew.sh).

### Prerequisites
Kuona runs on the Java VM. You will need to install Java 8 JDK to build and run the kuona tools.

Check out the [Brewfile](Brewfile) for a full list of dependencies.

OS X/macOS Setup

	git clone https://github.com/kuona/kuona-project.git
	cd kuona-project
	sh dev-setup

Once everything is setup use the build script to build all the components:

    ./build

## Code Navigation
The [Dashboard](http://dashboard.kuona.io) is in two parts.
* AngularJS web application /dashboard
* Web Service /kuona-api
* The API and other components depend on /kuona-core which delivers most of the functionality. The API and collector command line tools are designed to be simple wrappers of the core capacities.

Most components are written in [Clojure](https://clojure.org) but there are some small Java libraries that are built using maven.
Each collector has its own directory in /collectors
