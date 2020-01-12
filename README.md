# RDF Protege Desktop (v1.0.0-RDF-SNAPSHOT)

## This is a modified fork of [Protege Desktop](https://github.com/protegeproject/protege).
 
Protege Desktop is a free and open source ontology editor. 

The main difference between this Protege and the official one is that this fork has [ONT-API](https://github.com/owlcs/ont-api) at its core, while the original Protege Desktop uses [OWL-API (v4)](https://github.com/owlcs/owlapi/tree/version4).
This has the following two important consequences:
- Since ONT-API is an implementation of OWL-API v5 (not v4), no existing native Protege plugins are compatible with this fork.
- Since ONT-API is a RDF-centric OWL-API implementation (and the RDF-Graph is a main protagonist there), this opens up great opportunities to support all RDF-related things natively, including SPARQL, triple-stores, SHACL, etc. 
The approximate roadmap is as follows:

    * RDF view (a read-only triples-tree is added)
    * RDF editor (not yet implemented)
    * SPARQL tab (SELECT, ASK, DESCRIBE, CONSTRUCT, JSON + UPDATE)
    * Jena TDB support (not yet implemented)
    * RDB through [ONT-D2RQ](https://github.com/owlcs/ont-d2rq) (discussable) 
    * SPIN mapping through [ONT-MAP](https://github.com/owlcs/ont-map) (discussable)
    * etc  
        
Since most of these interesting things have not been done yet, right now this project is just a kind of demonstration and test-stand for ONT-API. But any contributions in these directions are welcome.

In order to distinguish with the official Protege, the artifact version is changed.
     
## Requirements
Java8

## License 
BSD 2-Clause License

## Installation
The project requires [ontapi-osgidistribution](https://github.com/sszuev/ontapi-osgidistribution) to be installed.
```
$ git clone https://github.com/sszuev/ontapi-osgidistribution.git
$ cd ontapi-osgidistribution
$ mvn clean install
$ cd ..
$ git clone https://github.com/sszuev/rdf-protege.git
$ cd rdf-protege
$ mvn clean package
``` 
## Run
With the default (i.e. when no profiles are selected) installation one of the following OS-dependent way can be used to run the editor:
```
$ cd ./protege-desktop/target/protege-${ver}-platform-independent/Protege-${ver}/
$ run.sh
```
or 
```
$ cd .\protege-desktop\target\protege-${ver}-platform-independent\Protege-${ver}\
$ run.bat
```
Notice that these ways may not work correctly in case there is no java8 in the environment PATH variable