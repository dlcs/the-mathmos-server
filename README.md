# The Mathmos

The Mathmos is a Web Search Service compliant with the [IIIF Content Search API](http://iiif.io/api/search/1.0/). We can index Annotations provided by the Elucidates Server using Pygar. Elucidate can put messages into an AWS SQS Queue whenever an Annotation is created, updated or deleted.  We can index Image Text provided by the Starsky Server using Barbarella.  Starsky can put messages into an AWS SQS Queue which contain the text associated with an image. 

## Getting Started

### Prerequisites
```
Java 8+
```
```
Apache Tomcat 8+
```
```
Elasticsearch 2.4.1
``` 
The Mathmos Server and its dependencies are written in pure Java, and is designed to work with Elasticsearch.

### Building
The Mathmos Server has a number of dependencies that must be built first:
* [the-mathmos-parent](the-mathmos-parent/)
	  * Parent Maven project that defines dependency library version numbers and common dependencies amongst all The Mathmos projects/
* [barbarella](barbarella)
	* Jar that  consumes AWS SQS Messages from a Starsky Server indexing them on Elasticsearch.
* [pygar](pygar)
	 * Jar that consumes AWS SQS Messages from an Elucidate Server, creating, updating and deleting them from an index on Elasticsearch.
Each dependency and the Mathmos Server itself can be built using Maven:
```
mvn clean package install -U
  
```
The building of both [pygar](pygar) and [barbarella](barbarella) will create zip files.  These contains a config folder containing the applicationContext.xml, aws-consumer.properties and log4j.xml files. A lib folder with the snapshot of the pygar/barbarella jar and all jar dependencies. Also included is a run-indexer.sh which is a .bash script to run pygar/barbarella.

## Built With

* [Spring Framework](https://projects.spring.io/spring-framework/)
* [Jackson](http://wiki.fasterxml.com/JacksonHome) 
* [JSONLD-JAVA](https://github.com/jsonld-java/jsonld-java)

## Contributing

Please read [CONTRIBUTING.md](CONTRIBUTING.md) for details on our code of conduct, and the process for submitting pull requests to us.

## Versioning

We use [SemVer](http://semver.org/) for versioning. For the versions available, see the [tags on this repository](https://github.com/digirati-co-uk/digirati-annotation-server/tags). 

## License

This project is licensed under the MIT License - see the [LICENSE.md](LICENSE) file for details


> Written with [StackEdit](https://stackedit.io/).