# run the tests (invoke the shell script with the right options)
doit:
	./do-it.sh                                                              \
	    test-repo                                                           \
	    servlex.xml                                                         \
	    servlex                                                             \
	    cases                                                               \
	    webapps/filters/dist/filters-webapp-0.1.0.xaw           filters     \
	    webapps/filters-app/dist/filters-app-webapp-0.1.0.xaw   filters-app \
	    webapps/mapping/dist/mapping-webapp-0.2.0.xaw           mapping     \
	    webapps/xsltforms/dist/xsltforms-webapp-0.1.0.xaw       xsltforms   \
	    ../samples/hello-world/dist/hello-world-0.9.3dev.xaw    hello-world \
	    ../../http-client-java/build/expath-http-client-saxon-0.12.0dev.xar "" \
	    ../../zip/saxon/build/expath-zip-saxon-0.7.0.xar                    "" \
	    ../samples/webapp-manager/dist/webapp-manager-0.3.1.xaw manager

# deploy the H2O website (Servlex must run)
h2o:
	curl --request POST --data-binary \
	   "@../../../h2o/website/dist/h2o-website-0.6.0.xaw" \
	   "http://localhost:9090/servlex/~rest/deploy/h2o"
	mkdir h2o-website
	git clone https://gitlab.com/h2o/web-content.git h2o-website/web-content
	( cd h2o-website/web-content; git checkout sandbox )

# delete Servlex and re-install it on Tomcat
upgrade:
	curl -u servlex:servlex "http://localhost:9090/manager/text/undeploy?path=/servlex"
	curl -u servlex:servlex \
	    "http://localhost:9090/manager/text/deploy?path=/servlex&config=/Users/fgeorges/projects/expath/servlex/test/servlex.xml&war=/Users/fgeorges/projects/expath/servlex/servlex/dist/servlex.war"

.PHONY: doit h2o upgrade
