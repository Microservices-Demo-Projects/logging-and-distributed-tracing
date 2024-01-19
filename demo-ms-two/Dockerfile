####################################
# STAGE 1: Build Application
####################################
FROM amazoncorretto:21.0.1-al2023-headless AS builder

WORKDIR /app

COPY . .

RUN pwd && ls -lrtha && chmod 550 ./mvnw && ls -lrtha && \
    ./mvnw -U clean install && ls -lrtha && ls -lrtha ./target && \
    mv ./target/demo-ms-two*.jar ./target/demo-ms-two.jar && ls -lrtha ./target

####################################
# STAGE 2: Setup Executable Image
####################################
FROM amazoncorretto:21.0.1-al2023-headless

# NOTE - 'nobody' is an existing user with the least privileges in the amazoncorretto image
USER nobody

WORKDIR /app

COPY --chown=nobody:nobody --from=builder /app/target/demo-ms-two.jar /app/demo-ms-two.jar

RUN pwd && ls -lrtha /app

CMD java -jar /app/demo-ms-two.jar --server.port=8082

EXPOSE 8082