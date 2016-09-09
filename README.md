# TDS_ExamService

This service is responsible for the exam portion of the the TDS system. 

## Requirements
* JDK 1.8
* Maven 3

## Build Steps

### Normal Build Actions

* **mvn clean install** 
	* standard build
	* runs unit tests and builds jars
* **mvn clean install -Dintegration-tests.skip=false**
	* Uses in memory database and stands up Spring container
	* Runs any integration tests that have *IntegrationTests* in the class name

#### Database migrations
The service (/service) project uses [flyway](https://flywaydb.org/) to do database migrations.  To run those during a build you will need to run the following command in the service directory:
  
**mvn install -Pdb-migration -Dflyway.url=[url] -Dflyway.user=[user] -Dflyway.password=[password]**

You can also use a text file containing the flyway properties.  More information can be found in Flyway's maven plugin documention.