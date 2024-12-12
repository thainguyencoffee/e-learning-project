```bash
 _____ _                       _           _
| ____| |      _ __  _ __ ___ (_) ___  ___| |_
|  _| | |     | '_ \| '__/ _ \| |/ _ \/ __| __|
| |___| |___  | |_) | | | (_) | |  __/ (__| |_ _
|_____|_____| | .__/|_|  \___// |\___|\___|\__(_)
              |_|           |__/
```

[![Backend CI with Gradle](https://github.com/thainguyencoffee/e-learning-project/actions/workflows/commit-stage.yaml/badge.svg)](https://github.com/thainguyencoffee/e-l/actions/workflows/commit-stage.yaml)

> I use Keycloak as an OpenID Provider. It as a user-service in charge of authentication and user data (roles, profiles, contact info, etc.)

## How to build

### Prerequisites
[Git](https://docs.github.com/en/get-started/getting-started-with-git/set-up-git) and the [JDK17 build](https://www.oracle.com/java/technologies/downloads/#java17).

Be sure that your JAVA_HOME environment variable points to the jdk-17 folder extracted from the JDK download.

### Check out sources
```bash
git clone https://github.com/thainguyencoffee/e-learning-project.git
cd e-learning-project
```

#### Development
1. Start docker compose

```bash
cd development 
docker compose up -d
cd ..
```

2. Build backend (bff & lms)

```bash
cd backend
./gradlew clean build
cd ..
```

3. Run bff

```bash
java -jar backend/bff/build/libs/bff-0.0.1-SNAPSHOT.jar
```

4. Run lms

```bash
java -jar backend/lms/build/libs/lms-0.0.1-SNAPSHOT.jar
```

4. Run Angular

```bash
cd angular-ui
ng serve
```

open browser and go to `http://localhost:4200`