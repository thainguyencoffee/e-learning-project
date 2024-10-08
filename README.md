# E-Learning Project

[![Backend CI with Gradle](https://github.com/thainguyencoffee/e-learning-project/actions/workflows/commit-stage.yaml/badge.svg)](https://github.com/thainguyencoffee/e-l/actions/workflows/commit-stage.yaml)

## How to build

### Prerequisites
[Git](https://docs.github.com/en/get-started/getting-started-with-git/set-up-git) and the [JDK17 build](https://www.oracle.com/java/technologies/downloads/#java17).

Be sure that your JAVA_HOME environment variable points to the jdk-17 folder extracted from the JDK download.

### Check out sources
```bash
https://github.com/thainguyencoffee/e-learning-project.git
```

### Compile and test; build all JARs, build image container, start docker compose
1. Without angular (recommend when you want to develop frontend)
```bash
./build.sh without-angular
```
2. With angular (recommend when you want to deploy)
```bash
./build.sh
```

3. If you want to build image
```bash
./build.sh native
```