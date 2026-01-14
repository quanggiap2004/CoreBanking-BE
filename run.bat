@echo off
SET JAVA_HOME=E:\JDK
SET PATH=%JAVA_HOME%\bin;%PATH%
SET MAVEN_OPTS=-Djava.net.preferIPv4Stack=true
echo JAVA_HOME set to: %JAVA_HOME%
echo Starting Spring Boot application...
.\mvnw.cmd spring-boot:run
