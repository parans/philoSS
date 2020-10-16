FROM openjdk:8u131-jdk-alpine
ARG JAR_FILE
RUN echo $JAR_FILE
ENV QUERY_PATH=""
EXPOSE 22/tcp
EXPOSE 80/tcp
COPY ${JAR_FILE} app.jar
CMD java -jar app.jar $QUERY_PATH
