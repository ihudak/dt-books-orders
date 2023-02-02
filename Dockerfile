FROM --platform=linux/x86-64 openjdk:17
MAINTAINER dynatrace.com
ARG JAR_FILE=build/libs/*0.0.1-SNAPSHOT.jar
ENV JAVA_TOOL_OPTIONS -agentlib:jdwp=transport=dt_socket,server=y,suspend=n,address=*:5005
COPY ${JAR_FILE} app.jar
ENTRYPOINT ["java","-jar","/app.jar"]
EXPOSE 8080
EXPOSE 5005
