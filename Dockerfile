FROM anapsix/alpine-java
LABEL maintainer "Florin Pico"
RUN mkdir /work
ADD target/sgm-java-example-0.0.1-SNAPSHOT.jar /work/app.jar
RUN sh -c 'touch /work/app.jar'
EXPOSE 8080
WORKDIR /work
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dapp.port=${app.port}", "-jar","/work/app.jar"]

