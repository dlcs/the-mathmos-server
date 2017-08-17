# The Mathmos

The Mathmos is a Web Search Service compliant with the [IIIF Content Search API](http://iiif.io/api/search/1.0/). We can index Annotations provided by the Elucidate Server using Pygar. Elucidate can put messages into an AWS SQS Queue whenever an Annotation is created, updated or deleted.  We can index Image Text provided by the Starsky Server using Barbarella.  Starsky can put messages into an AWS SQS Queue which contain the text associated with an image.



## Getting Started

### Prerequisites
```
Java 8+
```
```
Apache Tomcat 8+
```

```
Elasticsearch 5.4.0
```
```
Springdata for Elasticsearch 3.0.0.M4
```

The Mathmos Server and its dependencies are written in pure Java, and is designed to work with Elasticsearch.

### Building
The Mathmos Server has a dependency that must be built first:
* [the-mathmos-parent](the-mathmos-parent/)
    * Parent Maven project that defines dependency library version numbers and common dependencies
      Each dependency and the Mathmos Server itself can be built using Maven:
```
mvn clean package install -U

```


### Configuration
The location of the coordinate service and the cluster nodes need to be provided to the JVM as a parameter:

-Dtext.server.coordinate.url="http://wherever.the.coordinate.server.exists"
-Dcluster.nodes="localhost:9300"

### API
Please read [API.md](API.md) for details on how Mathmmos works.

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
