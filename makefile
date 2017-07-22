
LEIN_BUILD=lein midje
LEIN_INSTALL=lein install

all:	kuona-collector-lib	\
	code-collector		\
	dashboard		\
	environment-service	\
	git-collector		\
	http-collector		\
	jenkins-collector	\
	kuona-maven-analyser	\
	snapshot-collector	\
	test-service


kuona-collector-lib:
	figlet collector-lib
	cd kuona-collector-lib; $(LEIN_BUILD)
	cd kuona-collector-lib; $(LEIN_INSTALL)

code-collector:
	figlet code-collector
	cd code-collector; $(LEIN_BUILD)
	cd code-collector; $(LEIN_INSTALL)

dashboard:
	figlet dashboard
	cd dashboard; grunt build

environment-service:
	figlet backend
	cd environment-service; $(LEIN_BUILD)
	cd environment-service; $(LEIN_INSTALL)

git-collector:
	figlet git collector
	cd git-collector; $(LEIN_BUILD)
	cd git-collector; $(LEIN_INSTALL)

http-collector:
	figlet http collector
	cd http-collector; $(LEIN_BUILD)
	cd http-collector; $(LEIN_INSTALL)

jenkins-collector:
	figlet jenkins collector
	cd jenkins-collector; $(LEIN_BUILD)
	cd jenkins-collector; $(LEIN_INSTALL)

kuona-maven-analyser:
	figlet maven-collector
	cd kuona-maven-analyser; mvn install

snapshot-collector:
	figlet snapshot collector
	cd snapshot-collector; $(LEIN_BUILD)
	cd snapshot-collector; $(LEIN_INSTALL)

test-service:
	figlet test-service
	cd test-service; $(LEIN_BUILD)
	cd test-service; $(LEIN_INSTALL)

