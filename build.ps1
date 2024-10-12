$WITHOUT_ANGULAR = $false
if ($args -contains "without-angular") {
    $WITHOUT_ANGULAR = $true
}

Write-Host @"
 _____ _                       _           _
| ____| |      _ __  _ __ ___ (_) ___  ___| |_
|  _| | |     | '_ \| '__/ _ \| |/ _ \/ __| __|
| |___| |___  | |_) | | | (_) | |  __/ (__| |_ _
|_____|_____| | .__/|_|  \___// |\___|\___|\__(_)
              |_|           |__/
"@

Write-Host "* To build Spring Boot native images, run with the `native` argument: `.\build.ps1 native` (images will take much longer to build). *"
Write-Host "* To build without Angular, run with the `without-angular` argument: `.\build.ps1 without-angular`.                                 *"
Write-Host "* This build script tries to auto-detect ARM64 (Apple Silicon) to build the appropriate Spring Boot Docker images.                        *"

if ($WITHOUT_ANGULAR) {
    Write-Host "Without Angular."
} else {
    Write-Host "With Angular."
}

# Detect if we are on ARM64 architecture
$GRADLE_PROFILES = @()
if ([System.Environment]::Is64BitOperatingSystem) {
    $GRADLE_PROFILES += "arm64"
}
if ($args -contains "native") {
    $GRADLE_PROFILES += "native"
}

if ($GRADLE_PROFILES.Length -eq 0) {
    $GRADLE_PROFILE_ARG = ""
} else {
    $GRADLE_PROFILE_ARG = "-P" + ($GRADLE_PROFILES -join ",")
}

$host = $env:COMPUTERNAME.ToLower()

# Build the Spring Boot backend
cd "backend"
Write-Host "***********************"
Write-Host "Running ./gradlew clean build"
Write-Host "***********************"
Start-Process -NoNewWindow -Wait -FilePath "gradlew.bat" -ArgumentList "clean", "build"

Write-Host ""
Write-Host "*****************************************************************************************************************************************"
Write-Host "Running ./gradlew :lms:bootBuildImage --imageName=el/lms $GRADLE_PROFILE_ARG"
Write-Host "*****************************************************************************************************************************************"
Start-Process -NoNewWindow -Wait -FilePath "gradlew.bat" -ArgumentList ":lms:bootBuildImage", "--imageName=el/lms", $GRADLE_PROFILE_ARG

Write-Host ""
Write-Host "*****************************************************************************************************************************************"
Write-Host "Running ./gradlew :bff:bootBuildImage --imageName=el/bff $GRADLE_PROFILE_ARG"
Write-Host "*****************************************************************************************************************************************"
Start-Process -NoNewWindow -Wait -FilePath "gradlew.bat" -ArgumentList ":bff:bootBuildImage", "--imageName=el/bff", $GRADLE_PROFILE_ARG

cd ".."

# Replace the placeholders in the docker-compose file
Copy-Item -Force "compose.yml" "compose-$host.yml"
(Get-Content "compose-$host.yml").replace("LOCALHOST_NAME", $host) | Set-Content "compose-$host.yml"

# Keycloak configuration
Copy-Item -Force "keycloak101-realm.json" "keycloak/import/keycloak101-realm.json"
(Get-Content "keycloak/import/keycloak101-realm.json").replace("LOCALHOST_NAME", $host) | Set-Content "keycloak/import/keycloak101-realm.json"

# Angular UI
cd "angular-ui"
(Get-Content "src/app/app.config.ts").replace("LOCALHOST_NAME", $host) | Set-Content "src/app/app.config.ts"

if ($WITHOUT_ANGULAR) {
    Write-Host "Skipping Angular building."
} else {
    npm install
    npm run build
}
cd ".."

# Nginx reverse proxy
cd "nginx-reverse-proxy"
Remove-Item "nginx.conf" -Force

if ($WITHOUT_ANGULAR) {
    Copy-Item -Force "../nginx-local.conf" "nginx.conf"
} else {
    Copy-Item -Force "../nginx.conf" "nginx.conf"
}

(Get-Content "nginx.conf").replace("LOCALHOST_NAME", $host) | Set-Content "nginx.conf"
if ($WITHOUT_ANGULAR) {
    (Get-Content "nginx.conf").replace("4201", "4200") | Set-Content "nginx.conf"
}
cd ".."

# Docker build and compose
if ($WITHOUT_ANGULAR) {
    docker build -t el/nginx-reverse-proxy ./nginx-reverse-proxy
    docker-compose -f "compose-$host.yml" up -d nginx-reverse-proxy bff lms
} else {
    docker build -t el/nginx-reverse-proxy ./nginx-reverse-proxy
    docker build -t el/angular-ui ./angular-ui
    docker-compose -f "compose-$host.yml" up -d
}

Write-Host ""
Write-Host "Open the following in a new private navigation window."
Write-Host "Keycloak as admin / admin:secret"
Write-Host "http://$host:7080/auth/admin/master/console/#/keycloak101"

Write-Host "Frontends"
Write-Host "Please use the URL below to access Angular:"
Write-Host "http://$host:7080/angular-ui/"

if ($WITHOUT_ANGULAR) {
    cd "angular-ui"
    npm install
    ng serve --host 0.0.0.0 --port 4200
}
