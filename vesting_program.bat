@echo off
set SCRIPT_DIR=%~dp0
set JAR=%SCRIPT_DIR%target\vesting-program-1.0.0.jar

if not exist "%JAR%" (
    echo Building project ^(first run^)... 1>&2
    call "%SCRIPT_DIR%mvnw.cmd" -q clean package -DskipTests || exit /b 1
)

java -jar "%JAR%" %*
