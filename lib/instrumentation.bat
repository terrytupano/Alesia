@echo off


@echo off

set CLAZZP=../lib
rem set CLAZZP=C:/Users/terry/Documents/java project/Alesia/lib
set OUTPUTDIR=C:/Users/terry/Documents/java project/Alesia/bin

echo %CLAZZP%
echo %OUTPUTDIR%

rem set CLAZZP=%CLAZZP%;core/datasource/model
set CLAZZP=%CLAZZP%;activejdbc-2.3.1-j8.jar
set CLAZZP=%CLAZZP%;jackson-annotations-2.9.0.jar
set CLAZZP=%CLAZZP%;jackson-core-2.9.9.jar
set CLAZZP=%CLAZZP%;jackson-databind-2.9.9.1.jar
set CLAZZP=%CLAZZP%;javalite-common-2.3.jar
set CLAZZP=%CLAZZP%;activejdbc-instrumentation-2.3.jar
set CLAZZP=%CLAZZP%;javassist-3.18.2-GA.jar
set CLAZZP=%CLAZZP%;slf4j-api-1.7.25.jar
set CLAZZP=%CLAZZP%;slf4j-simple-1.7.25.jar
set CLAZZP=%CLAZZP%;mysql-connector-java-8.0.20.jar

java -classpath %CLAZZP% -DoutputDirectory=%OUTPUTDIR% org.javalite.instrumentation.Main
