# E-Learning Project

[![Resource Server CI with Gradle](https://github.com/thainguyencoffee/e-learning-project/actions/workflows/commit-stage.yaml/badge.svg)](https://github.com/thainguyencoffee/e-l/actions/workflows/commit-stage.yaml)

### What you will need

* Git
* Java 17
* Docker version 1.6.0 or higher

### Get the Source Code

Clone the repository


```bash
git clone https://github.com/thainguyencoffee/e-learning-project.git
cd e-learning-project
```

### Run Docker Compose

You can change directory with `cd deployment/docker` and run the following command `docker-compose up -d`

### Run the Application

You can run the application by using the following command: `./gradlew bootRun` 
This project depends on the following services:
* Postgres
* Keycloak
* Edge Service
* Resource Server

To run successful application, you need to run them.

### Run angular application

After run backend application, you can run frontend application by using the following command `ng serve`

## Successful
You can access the application at `http://localhost:9000`