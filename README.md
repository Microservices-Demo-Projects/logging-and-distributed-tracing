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

