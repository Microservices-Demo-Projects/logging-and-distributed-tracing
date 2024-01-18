# logging-and-distributed-tracing

This is an application to demonstrate the logging and distributed tracing with traceIds/SpanIds in SpringBoot microservices logs and integrating it with Jaeger for the visualization and analysis. All the applications are deployed in Docker or Kubernetes(Service Mesh).


# High Level Design

![High Level Design](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/HighLevelDesign.drawio.png)


# Demo Projects Initial Setup

- Four demo applications called `demo-ms-one`, `demo-ms-two`, `demo-ms-three` and `demo-ms-four` are created.
- The applications `demo-ms-one` and `demo-ms-three` are be created using springboot **web dependency** from https://start.spring.io/ as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/1-SpringWeb-Demo-Project-Created-By-Spring-Initializer.png)

- The applications `demo-ms-four` and `demo-ms-two` are be created using springboot **webflux dependency** from https://start.spring.io/ as follows:

    ![Spring Web Project Created By Spring Initializer](https://github.com/Microservices-Demo-Projects/logging-and-distributed-tracing/blob/notes/Notes/2-SpringWebflux-Demo-Project-Created-By-Spring-Initializer.png)


# Configuring Projects