####################################
# STAGE 1: Build Application
####################################
FROM amazoncorretto:21.0.1-al2023-headless AS builder

WORKDIR /app

COPY . .

RUN ls -lrtha && ./mvnw -U clean install && ls -lrtha ./target && \
    mv ./target/demo-ms-one*.jar ./target/demo-ms-one.jar && ls -lrtha ./target


####################################
# STAGE 2: Setup Executable Image
####################################
FROM amazoncorretto:21.0.1-al2023-headless

# RUN adduser -u 7788 -D appuser
# nobody is an existing user with least permissions
USER nobody

WORKDIR /app

COPY --chown=nobody:nobody --from=builder /app/target/demo-ms-one.jar /app/demo-ms-one.jar

RUN pwd && ls -lrtha / && ls -lrtha /app

CMD java -jar /app/demo-ms-one.jar --server.port=8081

EXPOSE 8081