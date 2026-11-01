@echo off
set JAVA_HOME=C:\Program Files\Java\jdk-22
set JAVACMD=%JAVA_HOME%\bin\java.exe
set WRAPPER_JAR=.mvn\wrapper\maven-wrapper.jar
set APP_BASE=%~dp0
set APP_BASE=%APP_BASE:~0,-1%
"%JAVACMD%" -classpath %WRAPPER_JAR% "-Dmaven.multiModuleProjectDirectory=%APP_BASE%" org.apache.maven.wrapper.MavenWrapperMain %*
