# Running OGC TeamEngine with ETS for WFS 2.0 on Docker

## Prerequists

### Install Docker

Check the official [Docker documentation](https://docs.docker.com/engine/) for information how to
  install Docker on your operating system. And then install Docker and supporting tools.

### Build ets-resources::
    
    % git clone https://github.com/opengeospatial/ets-resources.git
    % cd ets-resources
    % git checkout tags/16.0.23
    % mvn clean install
    
### Build the ETS for WFS 2.0::
    
    % git clone https://github.com/opengeospatial/ets-wfs20.git
    % cd ets-wfs20
    % git checkout tags/1.22
    % mvn clean install

### Build the TeamEninge::
    
    % git clone https://github.com/opengeospatial/teamengine.git
    % cd teamengine
    % git checkout tags/4.6
    % mvn clean install -Dets-resources-version=16.0.23 -Pogc.cite


## Build the Docker image from Dockerfile
The Dockerfile is located in the ```teamengine/teamengine-docker/src/main/config/docker/Dockerfile``` directory. 
To build the Docker image run in directory ```teamengine/teamengine-docker``` the Maven goals:

    % mvn clean package docker:build

This will build a new docker image from scratch. It may take a while the first time since Docker will download some
base images from docker hub.

## Running TeamEngine inside Docker container 
The following docker command starts the TeamEngine inside the Docker container with the name ```teamengine``` on port 8088:

    % docker run -p 8088:8080 --name teamengine --rm opengis/teamengine

## Accessing the TeamEngine web interface
Use a browser of your choice and open the URL:

http://container-ip:8088/teamengine

If your are running Docker on Windows or OS X with docker-machine check the IP with ```docker-machine ip```. 

http://192.168.99.100:8088/teamengine
