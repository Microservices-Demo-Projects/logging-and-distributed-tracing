# logging-and-distributed-tracing

This is a proof of concept project demonstrating logging of traceIds and spanIds across SpringBoot microservices and integrating it with Jaeger for visualization and analysis. All the applications are deployed using Docker and Kubernetes(Service Mesh).


# High-Level Design

![High Level Design](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/HighLevelDesign.drawio.png)


# Demo Projects Initial Setup

- Four demo applications called `demo-ms-one`, `demo-ms-two`, `demo-ms-three` and `demo-ms-four` are created using `springboot 3.2.1`.

- The applications `demo-ms-one` and `demo-ms-three` are created using springboot **webflux dependency** from https://start.spring.io/ as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/1-SpringWebflux-Demo-Project-Created-By-Spring-Initializer.png)

- The applications `demo-ms-two` and `demo-ms-four` are created using springboot **web dependency** from *https://start.spring.io/* as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/2-SpringWeb-Demo-Project-Created-By-Spring-Initializer.png)


# Configuring Other Dependencies

- Include the following BOMs in the `pom.xml` files of all four demo projects so that we can get the other dependencies corresponding that are required and compatible for logging traceIds without mentioning the specific dependency versions everywhere.

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

	- The `opentelemetry-exporter-otlp` dependency provides the logic for exporting/reporting to any OpenTelemetry protocol (OTLP) compliant log collector (e.g., Jaeger).

	     ```XML
		    <dependency>
		 	<groupId>io.opentelemetry</groupId>
      			<artifactId>opentelemetry-exporter-otlp</artifactId>
		    </dependency>
	    ```


- > **Note:** Including the `spring-boot-starter-actuator` dependency as part of the **[Demo Projects Initial Setup
](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/tree/main?tab=readme-ov-file#demo-projects-initial-setup)** is required for logging the traceIds/SpanIds in the demo applications.

# Log Sampling Configuration
- By default, springboot sets the sampling rate to `0.1` (i.e., 10%) to reduce the log data collected and reported to the OTLP log collector (e.g., Jaeger). When a span is not sampled, it adds no overhead (a noop).
- For this demo application we will use the sampling rate as `1.0` (i.e., 100%) so that all the spans will be exported to OTLP log collector (e.g., Jaeger) as follows in the `application.yaml`:

    ```YAML
    management:
        tracing:
            sampling:
                probability: 1.0
    ```

# Log Exporter Configuration

## `HTTP` Log Exporter Configuration
- By default, the springboot apps are configured to use the HTTP based log exporter with OTLP log collectors. Therefore, we have to just include the following in `application.yaml`:

    ```YAML
    management:
        otlp:
            tracing:
                # The default HTTP protocol endpoint for Jaeger OTEL Collector
                endpoint: ${JAEGER_COLLECTOR_URL:http://localhost:4318/v1/traces}
    ```

## `gRPC` Log Exporter Configuration

- Instead of the `HTTP` based log exporter property `management.otlp.tracing.endpoint` from spring, we will create a custom property for `gRPC` based log exporter endpoint as follows in the `application.yaml`:

    ```YAML
    otel:
        collector:
            # This is gRPC protocol endpoint for Jaeger OTEL Collector
            url: http://localhost:4317/api/traces
    ```
- Then, we have to create a custom `@Configuration` class to create bean objects for `OtlpGrpcSpanExporter` and `TextMapPropagator` as follows:

    ```java
	package com.example.demo.ms.three.configs;

	import io.opentelemetry.context.propagation.TextMapPropagator;
	import io.opentelemetry.exporter.otlp.trace.OtlpGrpcSpanExporter;
	import io.opentelemetry.extension.trace.propagation.JaegerPropagator;
	import org.springframework.beans.factory.annotation.Value;
	import org.springframework.context.annotation.Bean;
	import org.springframework.context.annotation.Configuration;
	import org.springframework.http.HttpHeaders;
	
	@Configuration
	public class OtelConfiguration {
	
	    private final String otelCollectorUrl;
	
	    public OtelConfiguration(@Value("${otel.collector.url}") String otelCollectorUrl) {
		this.otelCollectorUrl = otelCollectorUrl;
	    }
	
	    @Bean
	    public TextMapPropagator jaegerPropagator() {
		return JaegerPropagator.getInstance();
	    }
	
	    /**
	     * This custom bean definition method is required if we want to use gRPC (typical port 4317)
	     * instead of the default HTTP protocol (typically on port 4318) for exporting the span.
	     * By default, the Spring's OtlpAutoConfiguration class will setup the OtlpHttpSpanExporter bean.
	     * @return OtlpGrpcSpanExporter
	     */
	    @Bean
	    public OtlpGrpcSpanExporter grpcSpanExporter(){
		return OtlpGrpcSpanExporter.builder()
			.addHeader(HttpHeaders.CONTENT_TYPE, "application/x-protobuf")
			.setEndpoint(otelCollectorUrl)
			.build();
	    }
	}
    ```

- Finally, for the reactive springboot applications that are created using **webflux dependency** (e.g., `demo-ms-one` and `demo-ms-three`) we have to configure a WebFilter so that we can add the traceId/SpaceId into the thread context. This is necessary because in a reactive application multiple threads can process a single request and therefore we have to inlcude the the correct traceId/SpaceId in the current thread context so that logger can use it in the MDC and make those values available in the generated logs. An example of the custom `WebFilter` class is as follows:
    ```java
	package com.example.demo.ms.three.configs;

	import io.micrometer.context.ContextSnapshot;
	import io.micrometer.observation.contextpropagation.ObservationThreadLocalAccessor;
	import org.springframework.stereotype.Component;
	import org.springframework.web.server.ServerWebExchange;
	import org.springframework.web.server.WebFilter;
	import org.springframework.web.server.WebFilterChain;
	import reactor.core.publisher.Mono;
	
	
	@Component
	public class RequestMonitorWebFilter implements WebFilter {
	    @Override
	    public Mono<Void> filter(ServerWebExchange exchange, WebFilterChain chain) {
	        return chain.filter(exchange)
	                // Preparing context for the Tracer Span used in TracerConfiguration
	                .contextWrite(context -> {
	                    ContextSnapshot.setThreadLocalsFrom(context, ObservationThreadLocalAccessor.KEY);
	                    return context;
	                });
	    }
	}
    ```

- > Note: Along with the sample code provided in the README.md also refer to the demo application's code.

# Containerizing the Application
- The four demo apps are using spring boot, maven and Java 21 therefore we will create a two stage dockerfile as follows to containerize the demo apps.

    - The example below is for `demo-ms-one` app which exposes port `8081`:
 
    	```Dockerfile
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
       ```
    - Similarly the Dockerfiles are written for the all demo applications as:
    	- `demo-ms-one` exposes port `8081`
    	- `demo-ms-two` exposes port `8082`
    	- `demo-ms-three` exposes port `8083`
    	- `demo-ms-four` exposes port `8084`

- Commands to build and push the container images to Dockerhub:
    - `cd ./demo-ms-one` application directory:

        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-one:latest .
        ```
        ```shell
        docker push sriramponangi/logging-tracing.demo-ms-one:latest
        ```
    - `cd ./demo-ms-two` application directory:
    
        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-two:latest .
        ```
        ```shell
        docker push sriramponangi/logging-tracing.demo-ms-two:latest
        ```
    - `cd ./demo-ms-three` application directory:
    
        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-three:latest .
        ```
        ```shell
        docker push sriramponangi/logging-tracing.demo-ms-three:latest
        ```
    - `cd ./demo-ms-four` application directory:
    
        ```shell
        docker build --progress plain -t sriramponangi/logging-tracing.demo-ms-four:latest .
        ```
        ```shell
        docker push sriramponangi/logging-tracing.demo-ms-four:latest
        ```

# Deploying the Application

## **Deploying the application using Docker commands:**

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
        -network demoapps \
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

## To Do: Kubernetes (ServiceMesh) Manifest Yaml
The following configuration can be used to deployed this demo project into an OpenShift servicemesh:
![Kubernetes Manifest YAML](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/main/kubernetes-manifest.yaml)

> Using the following command a load of 100 API requests to `/four/quote` is generated from inside the demo-ms-four pod:
```shell
n=0; while [[ $n -lt 100 ]]; do sleep 3 && date && echo -e "Execution $n" \
&& curl -X GET http://demo-ms-four:8084/four/quote ; n=$((n+1)); done
```

### Openshift ServiceMesh Deployment Results:
By deploying this Kubernetes manifest yaml of the four demo microservice applications we can see the following results:

#### Deployments created in OpenShift for the four demo microservices
![Deployments Created Screenshot](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/Results/1-OpenShift-Deployments.png)

#### Pods created in OpenShift for the four demo microservices
![Deployments Created Screenshot](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/Results/2-OpenShift-Pods.png)

#### Kiali workload graph in OpenShift - ServiceMesh
![Kiali Workload Graph Screenshot](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/Results/3-Kiali-Workload-Graph.png)

#### List of Registered traces in Jaeger
![Jaeger List of Traces Screenshot](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/Results/4-Jager-Overall-ms-4-traces.png)

#### Trace Details for the four demo microservices in Jaeger
![Jaeger List of Traces Screenshot](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/Results/5-Jager-Detailed-ms-4-traces.png)

# CI/CD with GitHub Actions
- The demo applications can also be built and their container image can be pushed into DockerHub using the CI pipelines/workflows created using GitHub Actions:

    -  Continuous Integration (CI) workflows are configured to be triggered manually by providing the following arguments:

    	- Argument - 1: `Container Image Tag` is a string and the default value is latest.

        - Argument - 2: `Push Container Image To DockerHub` is a boolean and the default value is false.
  
    - **Note:** If the CI workflow successfully builds the container image and if the argument `Push Container Image To DockerHub` is set to true then the CI workflow will push the following two images to the corresponding DockerHub repo:

      - First image is pushed with custom tag name provided as input argument to `Container Image Tag` while manually triggering the CI pipeline workflow.

      - Second image is pushed with the value of `project.version` in the application's pom.xml file.

    - The CI workflows will push the container images built to the following DockerHub repositories:
  
      - `CI - demo-ms-one` pushes to https://hub.docker.com/repository/docker/sriramponangi/logging-tracing.demo-ms-one/general
  
      - `CI - demo-ms-two` pushes to https://hub.docker.com/repository/docker/sriramponangi/logging-tracing.demo-ms-two/general
  
      - `CI - demo-ms-three` pushes to https://hub.docker.com/repository/docker/sriramponangi/logging-tracing.demo-ms-three/general
  
      - `CI - demo-ms-four` pushes to https://hub.docker.com/repository/docker/sriramponangi/logging-tracing.demo-ms-four/general
  
    - For more details on the GitHub Actions CI workflows refer to the yaml workflows here: https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/tree/main/.github/workflows

  
# References:
- **[Springboot Docs for micrometer-tracing](https://docs.spring.io/spring-boot/docs/current/reference/html/actuator.html#actuator.micrometer-tracing)**

- **[Common Springboot Application Properties
](https://docs.spring.io/spring-boot/docs/current-SNAPSHOT/reference/html/application-properties.html)**

    - For example refer: `management.otlp.tracing.*` properties.

- **[Configuring Webflux Context to Include TraceId/SpanId in logs](https://javed0863.medium.com/springboot3-debugging-and-tracing-requests-using-micrometer-in-webflux-7d954de82f25)**
  
    - > This configuraiton is required because one request in webflux APIs can be processed to by multiple threads and including the traceId/spanId in thread context (MDC) is required.
    


