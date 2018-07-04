## Spring Integration Java DSL

The Java DSL for Spring Integration is essentially a facade for Spring Integration.

The DSL provides a simple way to embed Spring Integration Message Flows into your application using the fluent Builder
pattern together with existing Java and Annotation configurations from Spring Framework and Spring Integration as well.
Another useful tool to simplify configuration is Java 8 Lambdas.

The DSL is presented by the IntegrationFlows Factory for the IntegrationFlowBuilder.
This produces the IntegrationFlow component, which should be registered as a Spring bean (@Bean).
The builder pattern is used to express arbitrarily complex structures as a hierarchy of methods that may accept Lambdas as arguments.

The IntegrationFlowBuilder just collects integration components (MessageChannels, AbstractEndpoints etc.) in the
IntegrationFlow bean for further parsing and registration of concrete beans in the application context by IntegrationFlowBeanPostProcessor.

The Java DSL uses Spring Integration classes directly and bypasses any XML generation and parsing.

However, the DSL offers more than syntactic sugar on top of XML. One of its most compelling features is the ability to define
inline Lambdas to implement endpoint logic, eliminating the need for external classes to implement custom logic.
In some sense, Spring Integration's support for the Spring Expression Language (SpEL) and inline scripting address this,
but Java Lambdas are easier and much more powerful.

### Testing

For running and testing application you should perform following command:
````
    $ cd spring-integration
    $ ./gradlew clean build
````

