# ftpmock
Fake ftp, sftp, ftps server for testing

Uses org.junit.rules.TemporaryFolder to create home directories to java tmp dir (change with -Djava.io.tmpdir=/mytemp)

## Default Properties
 
	ftp.port=2121
	sftp.port=2222
	ftps.port=2323
	ftp.username=admin
	ftp.password=admin


## Run locally

	mvn clean install
	
	java -jar ftpmock-1.0.jar
	
	or to change some properties
	
	java  -Dftp.password=s3cr3t -Djava.io.tmpdir=/TMP -jar ftpmock-1.0.jar
	

## Run in OpenShift

	oc new-build --binary=true --name=ftpmock -i=redhat-openjdk18-openshift -e MAVEN_MIRROR_URL=<your maven mirror>

For image see:
	
	https://access.redhat.com/documentation/en-us/red_hat_jboss_middleware_for_openshift/3/html-single/red_hat_java_s2i_for_openshift/

Start build

	oc start-build ftpmock --from-dir=. --follow

Deploy app with DEBUG

	oc new-app ftpmock -e JAVA_DEBUG=true -e JAVA_DEBUG_PORT=9009


or for existing app

	oc env dc/ftpmock -e JAVA_DEBUG=true -e JAVA_DEBUG_PORT=9009
 
Open port for debugging, e.g.

	oc port-forward ftpmocks-1-1uymm 5005:9009
	
