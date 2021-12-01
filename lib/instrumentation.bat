@echo off

set CLASSPATH=..
rem set CLASSPATH=%CLASSPATH%;core/datasource/model
set CLASSPATH=%CLASSPATH%;activejdbc-2.3.1-j8.jar
set CLASSPATH=%CLASSPATH%;jackson-annotations-2.9.0.jar
set CLASSPATH=%CLASSPATH%;jackson-core-2.9.9.jar
set CLASSPATH=%CLASSPATH%;jackson-databind-2.9.9.1.jar
set CLASSPATH=%CLASSPATH%;javalite-common-2.3.jar
set CLASSPATH=%CLASSPATH%;activejdbc-instrumentation-2.3.jar
set CLASSPATH=%CLASSPATH%;javassist-3.18.2-GA.jar
set CLASSPATH=%CLASSPATH%;slf4j-api-1.7.25.jar
set CLASSPATH=%CLASSPATH%;slf4j-simple-1.7.25.jar
set CLASSPATH=%CLASSPATH%;mysql-connector-java-8.0.20.jar

java -classpath %CLASSPATH% -DoutputDirectory=.. org.javalite.instrumentation.Main