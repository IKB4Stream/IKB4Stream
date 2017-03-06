IKB4Stream
=====================

# Introduction

*IKB4Stream* is a module of the smart waterleak detection system WAVES.

## About WAVES
> WAVES is a scalable and distributed platform for processing semantic streams based on open source frameworks.

> WAVES is the result of a research project that aims to provide smart water management system. Thanks to its abstract level, it could be easily applied on streaming sensor data in other domains.

More info on http://www.waves-rsp.org/

## IKB4Stream
IKB4Stream is a module inside WAVES. Its goal is to detect false positives among the leaks reported from the WAVES system water sensors.

# Team Members
* Project Manager: Vincent Heng
* Technical Experts: Valentin Michalak, Ludovic Carretti
* NLP Expert, Web Designer, Documentation: Mehmet Demir 
* NLP Expert: Sandy Allibert
* Business Analyst: Boubacar Dabo
* DevOps: Valentin Michalak
* Quality Expert: Ludovic Carretti
* Security, Performance: Kevin Mollenhauer
* Metrics Analyst, Communication, Documentation: Loïck Renée

# Installation Guide

First of all, download the file archive.

Unzip it.

```sh
   unzip PATH/FILE_NAME.zip
```

Move to`docker_images` directory.

```sh
   cd docker_images
```


Then, add execution flag on each script file.

```sh
   chmod +x setup.sh
   chmod +x docker_images/prepare_docker_images.sh
   chmod +x docker_images/build_docker_images.sh
```

Just before launch the project, change the conf.properties file.
You need to declare the container name as hostname.

```sh
   vim resources/conf.properties
```

Change the line:

`database.metrics.host = http://localhost:8086`

to:

`database.metrics.host = http://influx:8086`

and the line:

`database.host = mongodb://localhost:27017/`

to:

`database.host = mongodb://mongo:27017/`


Now, you can launch the entire project.

```sh
   ./setup.sh
```

# Run Guide

> TODO

# Developper Guide
### Resources
The directory **resources** contains all the config properties files. 

*  **anomaly.ttl** is an anomaly request for testing
* **config.properties** contains the general properties of the project
* **logback.properties** contains properties for logback

* The directory **communication** contains files properties for kafka and the web GUI. .
* The directory **datasource** contains files properties for each ProducerConnector. If the ProducerConnector is a mock (name endind by "Mock"), the directory also contains data mock
* The directory **scoreprocessor** contains files properties for each ScoreProcessor and their mock. 
* The directory **nlp_model** contains the binaries for the NLP functions (lemmatization, NER...) and a dictionnary for lemmatization.

### Sources
All sources are in the package ```com.waves_rsp.ikb4stream```.
#### Communication
This package contains all the connectors for clients applications : **Kafka Connector**  and the **Web Connector**
#### Consumer
This package contains consumers : **Database Reader** and the **Communication Manager**. It also contains the **Main** class for launching program. 
#### Core
This package contains all utilities classes.

* The sub package **communication** contains *Functional Interfaces* and define Objects for communicating with the database. 
* The sub package **metrics** contains classes wich are used for logs metrics in the Influx database. 
* The sub package **model** define Objects : *Event*, *LatLong* and *PropertyManager*
 * The sub package **util** contains the utilities : the Geocoder, the NLP functions, the Class Manager, the Jar Loader and Rules Reader. 

#### Datasource
This package contains all the **Producer Connectors** which search data from differents API (stream or batch). A Producer Connector implements the **IProducerConnector** interface.
#### Producer
This package contains the **Main** class for the producer module. 
>TODO :  complete

#### Scoring
This package contains all the **ScoreProcessor** apply to the events created by the Producer Connectors. In the sub package **event**, there is the default score processor. 
Other subpackage contains a specific Score Processor for a specific Producer Connector. 
All classes in this package implement the **IScoreProcessor** interface.

#### Test
This package contains all the unit Test (JUNIT). 
It takes up the architecture of the rest of the project.

# License

See the [LICENSE](LICENSE.md) file for license rights and limitations (MIT).
