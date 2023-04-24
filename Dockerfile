FROM --platform=linux/x86-64 ivangudak096/dt-java-agents:latest
MAINTAINER dynatrace.com

ARG JAR_FILE=build/libs/*0.0.1-SNAPSHOT.jar
COPY ${JAR_FILE} /opt/app/app.jar
