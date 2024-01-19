# logging-and-distributed-tracing

This is a proof of concept project demonstrating logging with traceIds and spanIds across SpringBoot microservices and integrating it with Jaeger for visualization and analysis. All the applications are deployed using Docker and Kubernetes(Service Mesh).


# High-Level Design

![High Level Design](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/HighLevelDesign.drawio.png)


# Demo Projects Initial Setup

- Four demo applications called `demo-ms-one`, `demo-ms-two`, `demo-ms-three` and `demo-ms-four` are created using `springboot 3.2.1`.

- The applications `demo-ms-one` and `demo-ms-three` are created using springboot **webflux dependency** from https://start.spring.io/ as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/1-SpringWebflux-Demo-Project-Created-By-Spring-Initializer.png)

- The applications `demo-ms-two` and `demo-ms-four` are created using springboot **web dependency** from *https://start.spring.io/* as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/2-SpringWeb-Demo-Project-Created-By-Spring-Initializer.png)


# Configuring Other Dependencies

- Include the following BOMs in the `pom.xml` files of all four demo projects so that we can get the other dependencies corresponding to the BOMs that are required and compatible without mentioning the specific dependency versions everywhere.
    ```XML
    <dependencyManagement>
        <dependencies>
            <dependency>
                <groupId>io.opentelemetry</groupId>
                <artifactId>opentelemetry-bom</artifactId>
                <version>1.34.1</version>
                <type>pom</type>
                <scope>import</scope>
            </dependency>

            <dependency>
                <groupId>io.micrometer</groupId>
                <artifactId>micrometer-bom</artifactId>
                <version>1.12.2</version>
                <type>pom</type>
            </dependency>
        </dependencies>
    </dependencyManagement>
    ```

- Next, add the following dependencies in the `pom.xml` files of all four demo projects for traceId/spanId generation, propagation, and instrumentation.
	- The `micrometer-tracing-bridge-otel` dependency provides a micrometer bridge/facade to OpenTelemetry tracing. It also transitively pulls all the required OpenTelemetry SDKs required for the span tracing, propagation, and instrumentation(setup/config).
	     ```XML
		    <dependency>
		 	<groupId>io.micrometer</groupId>
      			<artifactId>micrometer-tracing-bridge-otel</artifactId>
		    </dependency>
	    ```

	- The `opentelemetry-exporter-otlp` dependency provides the logic for exporting/reporting to any OpenTelemetry protocol (OTLP) compliant log collector (in the demo we are using Jaeger).
	     ```XML
		    <dependency>
		 	<groupId>io.opentelemetry</groupId>
      			<artifactId>opentelemetry-exporter-otlp</artifactId>
		    </dependency>
	    ```


- > **Note:** Including the `spring-boot-starter-actuator` dependency as part of the **[Demo Projects Initial Setup
](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/tree/main?tab=readme-ov-file#demo-projects-initial-setup)** is required by the log tracing demo applications.

# Log Sampling Configuration
- By default, springboot sets the sampling rate to `0.1` (i.e., 10%) to reduce the log data collected and reported to the OTLP log collector (e.g., Jaeger). When a span is not sampled, it adds no overhead (a noop).
- For this demo application we will use the sampling rate as `1.0` (i.e., 100%) so that all the spans will be exported to OTLP log collector (e.g., Jaeger) as follows in the `application.yaml`:
    ```YAML
    management:
        tracing:
            sampling:
                probability: 1.0
    ```

# HTTP Log Exporter Configuration

- By default, the springboot apps are configured to use the HTTP based log exporter with OTLP log collectors. Therefore, we have to just include the following in `application.yaml`:
    ```YAML
    management:
        otlp:
            tracing:
                endpoint: ${JAEGER_COLLECTOR_URL:http://jaeger:4318/v1/traces}
    ```


# gRPC Log Exporter Configuration

- To Do...(Application properties and @Configuration class details)

# Containerizing the Application
- The four demo apps are using spring boot, maven and Java 21 therefore we will create a two stage dockerfile as follows to containerize the demo apps.
    - The example below is for `demo-ms-one` app which exposes port `8081` :

    ```Dockerfile
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

    CMD java -jar /app/demo-ms-one.jar

    EXPOSE 8081
    ```
- Similarly the Dockerfiles are written for the other the demo applications with:
    - `demo-ms-one` exposes port `8081`
    - `demo-ms-two` exposes port `8082`
    - `demo-ms-three` exposes port `8083`
    - `demo-ms-four` exposes port `8084`

- Commands to build and push the container images to Dockerhub:
    - `demo-ms-one` application:

        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-one:latest
        ```
        ```shell
        docker push docker push sriramponangi/logging-tracing.demo-ms-one:latest
        ```
    - `demo-ms-two` application:
    
        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-two:latest
        ```
        ```shell
        docker push docker push sriramponangi/logging-tracing.demo-ms-two:latest
        ```
    - `demo-ms-three` application:
    
        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-three:latest
        ```
        ```shell
        docker push docker push sriramponangi/logging-tracing.demo-ms-three:latest
        ```
    - `demo-ms-four` application:
    
        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-four:latest
        ```
        ```shell
        docker push docker push sriramponangi/logging-tracing.demo-ms-four:latest
        ```

# CI/CD with GitHub Actions
- To Do...(yaml file config creation)

# Deploying the Application

- **Deploying the application using docker commands:**
    - Create a common network to link all the docker containers.
        ```shell
        docker network create demoapps
        ```
    - We have to first start the jaeger applications before all the demo microservice apps so that it is ready to collect logs sent by them. 
        ```shell
        ## make sure to expose only the ports you use in your deployment scenario!
        docker run -d --name jaeger \
        -e COLLECTOR_OTLP_ENABLED=true \
        -e COLLECTOR_ZIPKIN_HOST_PORT=:9411 \
        -p 5775:5775/udp \
        -p 6831:6831/udp \
        -p 6832:6832/udp \
        -p 5778:5778 \
        -p 16686:16686 \
        -p 14250:14250 \
        -p 14268:14268 \
        -p 14269:14269 \
        -p 4317:4317 \
        -p 4318:4318 \
        -p 9411:9411 \
        --network demoapps \
        jaegertracing/all-in-one:1.53
        ```    
        - Navigate to **http://localhost:16686** to access the Jaeger UI.
        - The details on port mappings and different ways to configure the Jaeger app are given here:
            - **https://www.jaegertracing.io/docs/1.6/getting-started/**
            - **https://www.jaegertracing.io/docs/1.53/deployment/**
    
    - Then start the four demo microservice applications in the following sequence:
        - `demo-ms-one` application:

            ```shell
            # Note - JAEGER_COLLECTOR_URL environment variable in
            # demo-ms-one should be HTTP API (http://jaeger:4318/v1/traces)
            docker run --rm --name demo-ms-one \
            -p 8081:8081 \
            -e JAEGER_COLLECTOR_URL=http://jaeger:4318/v1/traces \
            --network demoapps \
            sriramponangi/logging-tracing.demo-ms-one:latest
            ```

        - `demo-ms-two` application:

            ```shell
            # Note - JAEGER_COLLECTOR_URL environment variable in
            # demo-ms-one should be HTTP API (http://jaeger:4318/v1/traces)
            docker run --rm --name demo-ms-two \
            -p 8082:8082 \
            -e JAEGER_COLLECTOR_URL=http://jaeger:4318/v1/traces \
            --network demoapps \
            sriramponangi/logging-tracing.demo-ms-two:latest
            ```

        - `demo-ms-three` application:

            ```shell
            # Note - JAEGER_COLLECTOR_URL environment variable in
            # demo-ms-one should be gRPC API (http://jaeger:4317/api/traces)
            docker run --rm --name demo-ms-three \
            -p 8083:8083 \
            -e JAEGER_COLLECTOR_URL=http://jaeger:4317/api/traces \
            --network demoapps \
            sriramponangi/logging-tracing.demo-ms-three:latest
            ```

        - `demo-ms-four` application:

            ```shell
            # Note - JAEGER_COLLECTOR_URL environment variable in
            # demo-ms-one should be gRPC API (http://jaeger:4317/api/traces)
            docker run --rm --name demo-ms-four \
            -p 8084:8084 \
            -e JAEGER_COLLECTOR_URL=http://jaeger:4317/api/traces \
            --network demoapps \
            sriramponangi/logging-tracing.demo-ms-four:latest
            ```

- To Do...(Docker Compose and Kubernetes (ServiceMesh) Manifest)
