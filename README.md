#AMQ Example

podman run -d -e AMQ_USER=admin -e AMQ_PASSWORD=admin -p8161:8161 -p5672:5672 -p61616:61616 -m=8g --cpus=1 --name activemq registry.redhat.io/amq7/amq-broker

mvn spring-boot:run

