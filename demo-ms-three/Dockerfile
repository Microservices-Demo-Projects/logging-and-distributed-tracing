####################################
# STAGE 1: Build Application
####################################
FROM amazoncorretto:21.0.1-al2023-headless AS builder

WORKDIR /app

COPY . .

RUN ls -lrtha && ./mvnw -U clean install && ls -lrtha ./target && \
    mv ./target/demo-ms-three*.jar ./target/demo-ms-three.jar && ls -lrtha ./target

####################################
# STAGE 2: Setup Executable Image
####################################
FROM amazoncorretto:21.0.1-al2023-headless

# NOTE - 'nobody' is an existing user with the least privileges in the amazoncorretto image
USER nobody

WORKDIR /app

COPY --chown=nobody:nobody --from=builder /app/target/demo-ms-three.jar /app/demo-ms-three.jar

RUN pwd && ls -lrtha /app

CMD java -jar /app/demo-ms-three.jar --server.port=8083

EXPOSE 8083