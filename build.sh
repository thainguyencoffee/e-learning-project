#!/bin/bash

WITHOUT_ANGULAR=false
if [[ " $@ " =~ [[:space:]]without-angular[[:space:]] ]]; then
  WITHOUT_ANGULAR=true
fi

echo "
 _____ _                       _           _
| ____| |      _ __  _ __ ___ (_) ___  ___| |_
|  _| | |     | '_ \| '__/ _ \| |/ _ \/ __| __|
| |___| |___  | |_) | | | (_) | |  __/ (__| |_ _
|_____|_____| | .__/|_|  \___// |\___|\___|\__(_)
              |_|           |__/
"
echo "* To build Spring Boot native images, run with the \"native\" argument: \"sh ./build.sh native\" (images will take much longer to build). *"
echo "* To build without Angular, run with the \"without-angular\" argument: \"sh ./build.sh without-angular\".                                 *"
echo "* This build script tries to auto-detect ARM64 (Apple Silicon) to build the appropriate Spring Boot Docker images.                        *"
if $WITHOUT_ANGULAR; then
  echo "Without Angular."
else
  echo "With Angular."
fi


if [[ "$OSTYPE" == "darwin"* ]]; then
  SED="sed -i '' -e"
else
  SED="sed -i -e"
fi

GRADLE_PROFILES=()
if [[ `uname -m` == "arm64" ]]; then
  GRADLE_PROFILES+=("arm64")
fi
if [[ " $@ " =~ [[:space:]]native[[:space:]] ]]; then
  GRADLE_PROFILES+=("native")
fi
if [ ${#GRADLE_PROFILES[@]} -eq 0 ]; then
  GRADLE_PROFILE_ARG=""
else
  GRADLE_PROFILE_ARG="-P$(IFS=, ; echo "${GRADLE_PROFILES[*]}")"
fi
host=$(echo $HOSTNAME | tr '[A-Z]' '[a-z]')

cd backend
echo "***********************"
echo "sh ./gradlew clean build"
echo "***********************"
echo ""
sh ./gradlew clean build

echo ""
echo "*****************************************************************************************************************************************"
echo "sh ./gradlew :lms:bootBuildImage --imageName=lms $GRADLE_PROFILE_ARG"
echo "*****************************************************************************************************************************************"
echo ""
sh ./gradlew :lms:bootBuildImage --imageName=lms $GRADLE_PROFILE_ARG

echo ""
echo "*****************************************************************************************************************************************"
echo "sh ./gradlew :bff:bootBuildImage --imageName=bff $GRADLE_PROFILE_ARG"
echo "*****************************************************************************************************************************************"
echo ""
sh ./gradlew :bff:bootBuildImage --imageName=bff $GRADLE_PROFILE_ARG

cd ..

rm -f "compose-${host}.yml"
cp compose.yml "compose-${host}.yml"
$SED "s/LOCALHOST_NAME/${host}/g" "compose-${host}.yml"
rm -f "compose-${host}.yml''"

rm -f "keycloak/import/keycloak101-realm.json"
mkdir -p keycloak/import
cp keycloak101-realm.json keycloak/import/keycloak101-realm.json
$SED "s/LOCALHOST_NAME/${host}/g" keycloak/import/keycloak101-realm.json
rm -f "keycloak/import/keycloak101-realm.json''"

cd angular-ui/
rm src/app/app.config.ts
cp ../angular-ui.app.config.ts src/app/app.config.ts
$SED "s/LOCALHOST_NAME/${host}/g" src/app/app.config.ts
rm -f "src/app/app.config.ts''"
if $WITHOUT_ANGULAR; then
  echo "Skipping angular building."
  echo ""
else
  npm i
  npm run build
fi
cd ..

cd nginx-reverse-proxy/
rm nginx.conf

if $WITHOUT_ANGULAR; then
  cp ../nginx-local.conf ./nginx.conf
else
  cp ../nginx.conf ./
fi

$SED "s/LOCALHOST_NAME/${host}/g" nginx.conf
if $WITHOUT_ANGULAR; then
  $SED "s/4201/4200/g" nginx.conf
fi
cd ..

if $WITHOUT_ANGULAR; then
  docker build -t nginx-reverse-proxy ./nginx-reverse-proxy
  docker compose -f compose-${host}.yml up -d nginx-reverse-proxy bff lms
else
  docker build -t nginx-reverse-proxy ./nginx-reverse-proxy
  docker build -t angular-ui ./angular-ui
  docker compose -f compose-${host}.yml up -d
fi

echo ""
echo "Open the following in a new private navigation window."

echo ""
echo "Keycloak as admin / admin:secret"
echo "http://${host}:7080/auth/admin/master/console/#/keycloak101"

echo ""
echo "Frontends"
echo "Please use the url below to access angular:"
echo http://${host}:7080/angular-ui/
if $WITHOUT_ANGULAR; then
  cd angular-ui/
  npm i
  ng serve --host 0.0.0.0 --port 4200
fi
