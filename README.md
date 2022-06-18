![Kuona Project Logo](http://dashboard.kuona.io/favicon.png)

# kuona-project

This repository is no longer maintained and has been set to read-only/archived

Kuona is the [Shona](https://en.wikipedia.org/wiki/Shona_language)
word that means "To see". This project is about providing valuable
actionable insight into software delivery.

The Kuona-project is about deepening our understanding of how we can
deliver value through software so that we can be more effective.

Kuona is a data collection and visualization tool for software
development. Check out http://kuona.io for background on the project.

Currently there are no packages for download but there is a hosted
example for some public open source projects at
http://dashboard.kuona.io

## Who is Kuona for?

### Kuona for development teams

Development teams often have various systems that support how they
deliver software:

* Story task management
* Version Control
* CI/CD build deploy systems
* Environment support

Each of these systems are information silos. Linkages between systems
are typically by convention. e.g. Story numbers in commit messages,
Build numbers injected into built artifacts. Tracabilty is often difficult.

Kuona is predicated on the idea that collecting and linking data from
these disparate systems teams gain greater insight into the
development process and can use this knowledge to improve.

The planned dashboard features will allow development teams to create
dashboards based on the areas they are tracking or looking to improve.

### Kuona for the enterprise

'How many versions of library X are currently used?' is often a
asked. This might be because of security vulnerabilities or it might
be that the organization is trying to upgrade the estate to a newer
version. In most cases this is not a trivial question to answer when
there are hundreds or thousands of components.

By gathering repository and dependency data this question is more
easily answered along with others.

## Design - How it works

Kuona uses collectors: command line tools and daemons to collect data
from software development systems like Git, Jenkins as well as data
from environments.

These Collectors post data through the kuona-api RESTful interface
into an [Elasticsearch](https://www.elastic.co/products/elasticsearch)
backing store. Elasticsearch is particularly adept at handling both
documents and time-series data. Each data type is given its own index.

/dashboard contains a AngularJS web application. We are currently
using GitHub repositories as sample data for collection and
analysis. http://dashboard.kuona.io/repositories provide search and
snapshot views of these projects.

## How you can help
* Provide feedback and bug reports by trying Kuona a try in your environment 
* Join the development [Google Group](https://groups.google.com/forum/#!forum/kuona-dev), ask questions and offer suggestions for features.

## Getting started

Software projects should be easy to join. Ideally getting up and
running as a developer should be very simple and quick. Long winded
manual installations are error prone and a waste of everyone's
time. If you find problems with the developer setup please raise an
issue so we can address it.

Currently the installation scripts have only been tested on OS X/macOS
and rely on [homebrew](https://brew.sh).

### Prerequisites

Kuona runs on the Java VM. You will need to install Java 8 JDK to
build and run the kuona tools.

Check out the [Brewfile](Brewfile) for a full list of dependencies.

OS X/macOS Setup

	git clone https://github.com/kuona/kuona-project.git
	cd kuona-project
	sh dev-setup

Once everything is setup use the build script to build all the
components:

    ./build

Sometimes elasticsearch may not have started. If you have used brew to install the dependencies then use:

    brew services elasticsearch restart

to get things rolling.

## Code Navigation

The [Dashboard](http://dashboard.kuona.io) is in two parts.

* AngularJS web application kuona-project/dashboard
* Web Service kuona-project/kuona-api

The API and other components depend on kuona-project/kuona-core which
delivers most of the functionality. The API and collector command line
tools are designed to be simple wrappers of the core capacities.

The public dashboard http://dashboard.kuona.io demonstrates the latest
capabilities by reading public source code repositories and other
systems.

Most components are written in [Clojure](https://clojure.org) but
there are some small Java libraries that are built using maven.

Each collector has its own directory in /collectors

## License

Kuona is licensed under the Apache License 2.0
