@echo off
set APP_HOME=%~dp0
set CLASSPATH=%APP_HOME%/gradle/wrapper/gradle-wrapper.jar
set DEFAULT_JVM_OPTS="-Xmx64m" "-Xms64m"

if defined JAVA_HOME (
  set JAVA_EXE=%JAVA_HOME%/bin/java.exe
  if not exist "%JAVA_EXE%" (
    echo ERROR: JAVA_HOME is set to an invalid directory: %JAVA_HOME%
    exit /b 1
  )
) else (
  set JAVA_EXE=java
)

%JAVA_EXE% %DEFAULT_JVM_OPTS% -classpath "%CLASSPATH%" org.gradle.wrapper.GradleWrapperMain %*

