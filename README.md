# kheo
[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)
[![Circle CI](https://circleci.com/gh/kheo-ops/kheo-core/tree/master.svg?style=shield)](https://circleci.com/gh/kheo-ops/kheo-core)

## Overview
Kheo is an agentless application dedicated to servers management, including softwares inventory, os and network informations. It performs connections in background in order to generate events that represent changes on servers (differences since the last connection).

In addition, it will discover routes between servers and display them as a graph in the web ui.

Kheo relies on SSH to communicate with the servers to manage so it does not need any specific configuration. As a constraint, it needs a key to contact remote hosts.

Once your servers have been registered, you can obtain informations like:
- Network interfaces, IPs and routes
- OS type and version
- Users
- Resources (ram, disk, cpu, ...)
- Running processes
- Installed packages

Moreover, Kheo discovers your servers configuration at regular intervals and stores delta between configuration as events. You can select events you want to store and those that do not have interest for you.

## Underlying technologies
Kheo is build with the following technologies:
- MongoDB
- Dropwizard

## Build

### API Backend

Set the mongo host IP address in `kheo-api/config/kheo-api-dev.yml`. Basically, the IP of the docker host.

Then, build the API image
```
docker build -t kheo-api kheo-api
```

## Running

Run application layers in docker containers:

The database must be run first.
```
docker run -d -p 27017:27017 --name kheo-db mongo:latest
```

The backend container linked to the `kheo-db` container
```
docker run -d -p 8080:8080 -p 8081:8081 --name kheo-api --link kheo-db:kheo-db kheo-api
```

## Testing
Cucumber is used to validate API behavior. CircleCI runs Cucumber tests at each build.
