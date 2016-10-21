# Running TEAM Engine on dynamic infrastructure platforms

This module generates images for a variety of dynamic infrastructure platforms, such as:

* VirtualBox
* Amazon EC2
* Docker

The TEAM Engine [virtualization guide](http://opengeospatial.github.io/teamengine/virt-guide.html)
describes how to use [Packer](https://www.packer.io/) to create machine and container images.
It is also possible to use a Dockerfile to build an image, as described below.


## Prerequisites

### Install Docker

Check the official [Docker documentation](https://docs.docker.com/engine/) for information how to 
install Docker on your operating system. And then install Docker and supporting tools.

### Dependencies 

You may build the following projects first:

#### Build ets-resources:
    
    % git clone https://github.com/opengeospatial/ets-resources.git
    % cd ets-resources
    % git checkout tags/16.06.08
    % mvn clean install
    
#### Build the ETS for WFS 2.0:
    
    % git clone https://github.com/opengeospatial/ets-wfs20.git
    % cd ets-wfs20
    % git checkout tags/1.25
    % mvn clean install

#### Build the TEAM Engine:

    % git clone https://github.com/opengeospatial/teamengine.git
    % cd teamengine
    % git checkout tags/4.9
    % mvn clean install -Dets-resources-version=16.06.08 -Pogc.cite

## Build the Docker image
The Dockerfile is located in the ```src/main/config/docker``` directory. To build the Docker image execute the Maven goals:

    % mvn clean package docker:build

This will build a new docker image from scratch. It may take a while the first time since Docker will download some
base images from [docker hub](https://hub.docker.com).

Check if the docker image has been built successfully with:

    % docker images

## Running TEAM Engine inside a Docker container 
The following docker command starts the TEAM Engine inside the Docker container with the name ```teamengine``` on port 8088
with the previously built Docker image named ```opengis/teamengine```:

    % docker run -p 8088:8080 --name teamengine --rm opengis/teamengine

## Accessing the TEAM Engine web interface
Use a browser of your choice and open the URL:

http://container-ip:8088/teamengine

If your are running Docker on Windows or OS X with docker-machine check the IP with ```docker-machine ip```.
On Linux it is most likely localhost/127.0.0.1 on Windows and macOS it might be a IP like:

http://192.168.99.100:8088/teamengine
