# kheo  
[![License](http://img.shields.io/:license-mit-blue.svg)](http://doge.mit-license.org)  
[![Issue Stats](http://issuestats.com/github/migibert/kheo/badge/pr)](http://issuestats.com/github/migibert/kheo)  
[![Issue Stats](http://issuestats.com/github/migibert/kheo/badge/issue)](http://issuestats.com/github/migibert/kheo)  
[![Circle CI](https://circleci.com/gh/migibert/kheo/tree/master.svg?style=shield)](https://circleci.com/gh/migibert/kheo)  

====
## Overview
Kheo is an agentless application dedicated to servers management, including softwares inventory, os and network informations. It performs connections in background in order to generate events that represent changes on servers (differences since the last connection).

In addition, it will discover routes between servers and display them as a graph in the web ui.

Kheo relies on SSH to communicate with the servers to manage so it does not need any specific configuration. As a constraint, it needs a key to contact remote hosts.

To register servers, you have many solutions:
- Using an inventory file just like Ansible does.
- Using the API
- Using the webapp

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
- AngularJS
- Dropwizard

## Build

### API Backend

Set the mongo host IP address in `kheo-api/config/kheo-api-dev.yml`. Basically, the IP of the docker host.

Then, build the API image
```
docker build -t kheo-api kheo-api
```

### Web frontend

Set the backend API IP in `kheo-web/config/environments/development.json`. Basically, the IP of the docker host.

Then, build the application
```
npm install
bower install
grunt replace:development
grunt build
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

The web frontend container linked to the `kheo-api` container
```
docker run -d -p 8000:80 -v ${PWD}/kheo-web/dist:/var/www/html --name kheo-web --link kheo-api:kheo-api dockerfile/nginx
```

To make it easier to use, there is a fig config file that let you start each layer inside a container:
```sudo fig up -d```

## Deployment
Kheo comes with sample Ansible playbooks that deploys components through your machines.

There are sample playbooks for those topologies:
- all-in-one: All Kheo components are deployed on one machine
- simple: Each component is deployed on a machine

You can execute the playbook following these steps:
```
sudo ansible-galaxy install bennojoy.mongo_mongod
sudo ansible-galaxy install jdauphant.nginx
sudo ansible-galaxy install ANXS.oracle-jdk
ansible-playbook -i inventory kheo.yml --private-key=key
```

## Testing
Cucumber is used to validate API behavior. CircleCI runs Cucumber tests at each build.

To run it in local, use the script `local.sh`