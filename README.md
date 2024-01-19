# logging-and-distributed-tracing

This is a proof of concept project demonstrating logging with traceIds and spanIds across SpringBoot microservices and integrating it with Jaeger for visualization and analysis. All the applications are deployed using Docker and Kubernetes(Service Mesh).


# High-Level Design

![High Level Design](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/HighLevelDesign.drawio.png)


# Demo Projects Initial Setup

- Four demo applications called `demo-ms-one`, `demo-ms-two`, `demo-ms-three` and `demo-ms-four` are created using `springboot 3.2.1`.

- The applications `demo-ms-one` and `demo-ms-three` are be created using springboot **webflux dependency** from https://start.spring.io/ as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/1-SpringWebflux-Demo-Project-Created-By-Spring-Initializer.png)

- The applications `demo-ms-two` and `demo-ms-four` are be created using springboot **web dependency** from https://start.spring.io/ as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/2-SpringWeb-Demo-Project-Created-By-Spring-Initializer.png)


# Configuring Other Dependencies

- Include the following BOMs in the `pom.xml` files of all the four demo projects so that we can get the other dependencies corresponding to the BOMs that are required and compatible without mentioning the specific dependency versions everywhere.
    ```xml
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

- Next, add the following dependencies in the `pom.xml` files of all the four demo projects for traceId/spanId generation, propagation and instrumentation.
    ```xml
        <!--This dependency provides micrometer bridge/facade to OpenTelemetry tracing.
		It also transitively pulls all the required OpenTelemetry SDKs required for the
		span tracing, propagation and instrumentation(setup/config).
		-->
		<dependency>
			<groupId>io.micrometer</groupId>
			<artifactId>micrometer-tracing-bridge-otel</artifactId>
		</dependency>

		<!--This dependency provides the logic for exporting/reporting to any
            OpenTelemetry protocol (OTLP) compliant log collector
            (in the demo we are using Jaeger)
        -->
		<dependency>
			<groupId>io.opentelemetry</groupId>
			<artifactId>opentelemetry-exporter-otlp</artifactId>
		</dependency>
    ```

- > **Note:** Including the `spring-boot-starter-actuator` dependency as part of the **[Demo Projects Initial Setup
](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/tree/main?tab=readme-ov-file#demo-projects-initial-setup)** is required for the log tracing.

# HTTP Log Exporter Configuration

- To Do...(Application properties and @Configuration class details)

# gRPC Log Exporter Configuration

- To Do...(Application properties and @Configuration class details)

# Containerizing the Application
- To Do...(DockerFile and Build commands)

# CI/CD with Github Actions
- To Do...(yaml file config creation)

# Deploying the Application
- To Do...(Docker Compose and Kubernets Manifest)