```bash
 _____ _                       _           _
| ____| |      _ __  _ __ ___ (_) ___  ___| |_
|  _| | |     | '_ \| '__/ _ \| |/ _ \/ __| __|
| |___| |___  | |_) | | | (_) | |  __/ (__| |_ _
|_____|_____| | .__/|_|  \___// |\___|\___|\__(_)
              |_|           |__/
```

![GitHub Actions Workflow Status](https://img.shields.io/github/actions/workflow/status/thnguyen101/e-learning-project/.github%2Fworkflows%2Fcommit-stage.yaml)
![GitHub License](https://img.shields.io/github/license/thnguyen101/e-learning-project)
![GitHub language count](https://img.shields.io/github/languages/count/thnguyen101/e-learning-project)
![GitHub top language](https://img.shields.io/github/languages/top/thnguyen101/e-learning-project)

## Introduction
### 🗄️ Project Structure

```sh
src
├── app
│   ├── browse-course                          # Module: Books
│   │   ├── page                               # Contains pages of the module
│   │   │   └── book-list                      # Page: Book List
│   │   │       ├── book-list.component.ts     # Component logic
│   │   │       ├── book-list.component.html   # Component template
│   │   │       └── book-list.component.css    # Component styles
│   │   ├── service                            # Service to call API
│   │   │   └── book.service.ts
│   │   └── model                              # Models for data transfer
│   │       └── book.ts
│   ├── common                                 # Shared common functionality
│   │   ├── components                         # Shared UI components
│   │   └── auth                               # Authentication-related files
│   ├── app.component.ts                       # Main app component
│   ├── app.component.html                     # Main app template
│   ├── app.component.css                      # Main app styles
│   ├── app.routes.ts                          # Routing configurations
├── favicon.ico
├── main.ts                                    # Entry point for the app
├── index.html                                 # Main HTML file
├── styles.css                                 # Global styles
└── assets                                     # Static assets (images, fonts, etc.)
```

## How to build
### Prerequisites
[Git](https://docs.github.com/en/get-started/getting-started-with-git/set-up-git) and the [JDK17 build](https://www.oracle.com/java/technologies/downloads/#java17).

Be sure that your JAVA_HOME environment variable points to the jdk-17 folder extracted from the JDK download.

### Check out sources
```bash
git clone https://github.com/thainguyencoffee/e-learning-project.git
cd e-learning-project
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