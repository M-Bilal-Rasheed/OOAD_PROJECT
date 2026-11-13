@REM ----------------------------------------------------------------------------
@REM Maven Start Up Batch script (Windows)
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set MAVEN_PROJECTBASEDIR=%~dp0
set MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%
set MVNW_REPOURL=https://repo.maven.apache.org/maven2

@REM Find Java
if defined JAVA_HOME (
  set JAVA_EXECUTABLE=%JAVA_HOME%\bin\java.exe
) else (
  set JAVA_EXECUTABLE=java
)

@REM Download maven-wrapper.jar if missing
set MAVEN_WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar
if not exist "%MAVEN_WRAPPER_JAR%" (
  echo Downloading Maven Wrapper...
  "%JAVA_EXECUTABLE%" -Dmaven.wagon.http.ssl.insecure=true ^
    -classpath "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" ^
    org.apache.maven.wrapper.MavenWrapperMain %* || ^
  powershell -Command "Invoke-WebRequest -Uri '%MVNW_REPOURL%/org/apache/maven/wrapper/maven-wrapper/3.2.0/maven-wrapper-3.2.0.jar' -OutFile '%MAVEN_WRAPPER_JAR%'"
)

"%JAVA_EXECUTABLE%" -classpath "%MAVEN_WRAPPER_JAR%" ^
  "-Dmaven.multiModuleProjectDirectory=%MAVEN_PROJECTBASEDIR%" ^
  org.apache.maven.wrapper.MavenWrapperMain %*
