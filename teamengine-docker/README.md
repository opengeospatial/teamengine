# Running OGC TEAM Engine with ETS for WFS 2.0 on Docker

This module provides a Dockerfile for building a Docker image with OGC TEAM Engine, version 4.6 and ets-wfs20, version 1.22
pre-installed.

## Prerequisites

### Install Docker

Check the official [Docker documentation](https://docs.docker.com/engine/) for information how to
  install Docker on your operating system. And then install Docker and supporting tools.

### Dependencies 

You may build the following projects first:

#### Build ets-resources:
    
    % git clone https://github.com/opengeospatial/ets-resources.git
    % cd ets-resources
    % git checkout tags/16.02.23
    % mvn clean install
    
#### Build the ETS for WFS 2.0:
    
    % git clone https://github.com/opengeospatial/ets-wfs20.git
    % cd ets-wfs20
    % git checkout tags/1.22
    % mvn clean install

#### Build the TEAM Engine:
    
    % git clone https://github.com/opengeospatial/teamengine.git
    % cd teamengine
    % git checkout tags/4.6
    % mvn clean install -Dets-resources-version=16.02.23 -Pogc.cite

## Build the Docker image
The Dockerfile is located in the ```teamengine/teamengine-docker/src/main/config/docker/Dockerfile``` directory. 
To build the Docker image run in directory ```teamengine/teamengine-docker``` the Maven goals:

    % mvn clean package docker:build

This will build a new docker image from scratch. It may take a while the first time since Docker will download some
base images from [docker hub](https://hub.docker.com).

## Running TEAM Engine inside a Docker container 
The following docker command starts the TEAM Engine inside the Docker container with the name ```teamengine``` on port 8088:

    % docker run -p 8088:8080 --name teamengine --rm opengis/teamengine

## Accessing the TEAM Engine web interface
Use a browser of your choice and open the URL:

http://container-ip:8088/teamengine

If your are running Docker on Windows or OS X with docker-machine check the IP with ```docker-machine ip```.
On Linux it is most likely localhost/127.0.0.1 on Windows and OS X it might be a IP like:

http://192.168.99.100:8088/teamengine