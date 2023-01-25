# AMQ Example using AMQP Protocol

Red Hat® AMQ—based on open source communities like Apache ActiveMQ and Apache Kafka—is a flexible messaging platform that delivers information reliably, enabling real-time integration and connecting the Internet of Things (IoT).

## Requirements

This example uses:
- Podman 4.2.0
- Red Hat AMQ Broker 7.X
- VSCode 1.74.1

## Setting up the Broker with Podman

1. Login to registry.redhat.io with podman using your credentials.

```
podman login registry.redhat.io
```

2. Create a podman container from registry.redhat.io/amq7/amq-broker image.

```
podman run -d -e AMQ_USER=admin -e AMQ_PASSWORD=admin -p8161:8161 -p5672:5672 -p61616:61616 -m=8g --cpus=1 --name activemq registry.redhat.io/amq7/amq-broker
```
The previous command will:
- Create a container from the image specified
- Create our AMQ Broker with its credentials
- Expose admin, AMQP and JMS ports
- Set limits to the container available resources (1CPU and 8GB RAM)
- Set the name of our container to *activemq*

## Setting up our Source Code examples

1. Clone the https://github.com/amqphub/amqp-10-jms-spring-boot.git repo using the Git Hub Extension in VSCode.

![Git-Clone](https://user-images.githubusercontent.com/60272316/214438928-463d874a-d232-4422-92c9-783a2e00cb94.png)

## Analizing and modifying the Source Code

We will be working with the /amqp-10-jms-spring-boot/amqp-10-jms-spring-boot-examples/amqp-10-jms-spring-boot-hello-world project. However, feel free to take a look to the other examples. Take a quick look to the HelloWorldMessageProducer.java 

This application implements a specific method to connect to the queue "example" and sends the received text message to that specific queue. Lets spice up our application and add a for loop that generates 100 text messages instead of 1.

1. Add the following code inside main method run.
```java
public void run(String... strings) throws Exception {
        for (int i = 0; i < 100; i++) {
            final String messageText = "Hello World " + i;
            LOG.info("============= Sending: " + messageText);
            sendMessage(getPayload());
        }
    }
```
With this modification we are able to stress our broker a little bit more by controlling the amount of messages sent by the proucer.

2. Now lets add some variables that will help us know how long did it take to send all the messages. Our run method will be as follows.
```java
public void run(String... strings) throws Exception {
    DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
    LocalDateTime nowI = LocalDateTime.now();
    for (int i = 0; i < 100; i++) {
        final String messageText = "Hello World " + i;
        LOG.info("============= Sending: " + messageText);
        sendMessage(messageText);
    }
    LocalDateTime nowF = LocalDateTime.now();
    System.out.println("Inicial " + dtf.format(nowI));
    System.out.println("Final " + dtf.format(nowF));
}
```
Do not forget to add the necessary imports for both LocalDateTime and DateTimeFormatter.
```java
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
```
Now that we have modified our Porducer App we can test that it is working correctly.

3. Open a New Terminal inside VSCode and run the following command to start our HelloWorld application.
```
mvn spring-boot:run 
```
**Note: you need Java 17 to run the prevoius command. If you have a different version just left click in your code an select "Run Java".**

AMQ Broker is amazing! 100 messages in less than a second! You can test and play with the values to stress the broker a bit more.
```java
... omitted output ...
2023-01-24T17:42:15.754-06:00  INFO 68243 --- [           main] o.a.s.b.j.e.HelloWorldMessageProducer    : ============= Sending: Hello World 99
Inicial 24/01/2023 17:42:15
Final 24/01/2023 17:42:15
2023-01-24T17:42:15.759-06:00  INFO 68243 --- [ntContainer#0-1] o.a.s.b.j.e.HelloWorldMessageConsumer    : ============= Received: Hello World 99
```
Remember that for this to work correctly our *activemq* container should be running behind scenes. You can always verify be running ```podman ps -a```.

Lets take our HelloWorld App to another level. Instead of receiving a plain text message, we are going to change it so that it can receive a json format message as *String* and parse it to a proper JSON format and retreive a specific property.

This use case is very common when trying to work with APIs and DataBases, for example.

4. Add the following method to the HelloWorldMessageProducer.java application.
```java
public String getPayload() {
        String payload ="{\"name\":\"Pankaj Kumar\",\"age\":32}";
        return payload;
}
```

This is just another way of declaring a String with a json format.

5. Additionally, modify the run method so that instead of sending the Hello World message, it sends our json as as a String. In this example we'll be sending just one message.
```java
public void run(String... strings) throws Exception {

           ... omitted code ...

    for (int i = 0; i < 1; i++) {
        final String messageText = "Hello World " + i;
        LOG.info("============= Sending: " + messageText);
        //Calls our brand new method getPayload() instead of sending messageText
        sendMessage(getPayload());
    }
           ... omitted code ...
}
```

Now it is time to prepare our consumer to received that String and convert it to a propre JSON Objetc.

6. Create a method called getProperties() so that it can received the String, parse it to JSON and retreive a specific property.
```java
public void getProperties(String message) throws ParseException {
    JSONParser parser = new JSONParser();
    Object obj = parser.parse(message);
    JSONObject jsonMessage = (JSONObject)obj;
    LOG.info(jsonMessage.toJSONString());
    LOG.info((String) jsonMessage.get("name"));
}
```

7. Call our new function in main method called processMsg() and throw the Exception.
```java
public void processMsg(String message) {
    LOG.info("============= Received: " + message);
    getProperties(message);
}
```

**Note that there are going to be some errors, don't worry. We just need to add a dependency to our *pom.xml* file.**

8. Open the pom.xml file and add the following dependency inside the *<*dependencies*>* element.
```java
<dependency>
  <groupId>com.googlecode.json-simple</groupId>
  <artifactId>json-simple</artifactId>
  <version>1.1</version>
</dependency>
```

9. Go back to our HelloWorldMessageConsumer.java and finish by adding the necessary imports.
```java
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
```

10. Finally, run the app again and see the magic.
```java

...output omitted...

2023-01-24T19:11:53.404-06:00  INFO 98385 --- [           main] o.a.s.b.j.e.HelloWorldMessageProducer    : ============= Sending: Hello World 0
Inicial 24/01/2023 19:11:53
Final 24/01/2023 19:11:53
2023-01-24T19:11:53.521-06:00  INFO 98385 --- [ntContainer#0-1] o.a.s.b.j.e.HelloWorldMessageConsumer    : ============= Received: {"name":"Pankaj Kumar","age":32}
2023-01-24T19:11:53.527-06:00  INFO 98385 --- [ntContainer#0-1] o.a.s.b.j.e.HelloWorldMessageConsumer    : {"name":"Pankaj Kumar","age":32}
2023-01-24T19:11:53.527-06:00  INFO 98385 --- [ntContainer#0-1] o.a.s.b.j.e.HelloWorldMessageConsumer    : Pankaj Kumar
```

## Exploring the AMQ Web Console

1. Visit the web console on http://localhost:8161/

2. Select the *Management console* option.

3. Enter the credentials used to initialize our broker (*user: admin, password: admin*)

![AMQ-Login](https://user-images.githubusercontent.com/60272316/214457514-705c69f4-5c94-48c6-849a-efe2ff087764.png)

By using the web console while running our previous app, we will be able to obtain important information like Queues, Addresses, Metrics, and more.

![AMQ-webConsole](https://user-images.githubusercontent.com/60272316/214458632-1d6b8c7c-13fb-44d2-9d52-5bad04db0778.png)

![Runtime-Metrics](https://user-images.githubusercontent.com/60272316/214458637-57541be2-5492-432e-96d7-b716b6d936f3.png)
