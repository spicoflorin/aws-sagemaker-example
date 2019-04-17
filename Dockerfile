FROM anapsix/alpine-java
RUN mkdir /work
ADD target/aws-sagemaker-pmml.jar /work/app.jar
RUN sh -c 'touch /work/app.jar'
EXPOSE 8080
WORKDIR /work
ENTRYPOINT ["java", "-Djava.security.egd=file:/dev/./urandom", "-Dapp.port=${app.port}", "-jar","/work/app.jar"]
LABEL maintainer "Florin Pico"