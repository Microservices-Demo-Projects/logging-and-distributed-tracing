####################################
# STAGE 1: Build Application
####################################
FROM amazoncorretto:21.0.1-al2023-headless AS builder

WORKDIR /app

COPY . .

RUN pwd && ls -lrtha && chmod 550 ./mvnw && ls -lrtha && \
    ./mvnw -U clean install && ls -lrtha && ls -lrtha ./target && \
    mv ./target/demo-ms-one*.jar ./target/demo-ms-one.jar && ls -lrtha ./target

####################################
# STAGE 2: Setup Executable Image
####################################
FROM amazoncorretto:21.0.1-al2023-headless

# NOTE - 'nobody' is an existing user with the least privileges in the amazoncorretto image
USER nobody

WORKDIR /app

COPY --chown=nobody:nobody --from=builder /app/target/demo-ms-one.jar /app/demo-ms-one.jar

RUN pwd && ls -lrtha /app

CMD java -jar /app/demo-ms-one.jar --server.port=8081

EXPOSE 8081