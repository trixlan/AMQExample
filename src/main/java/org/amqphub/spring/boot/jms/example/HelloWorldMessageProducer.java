/*
 * Licensed to the Apache Software Foundation (ASF) under one or more
 * contributor license agreements.  See the NOTICE file distributed with
 * this work for additional information regarding copyright ownership.
 * The ASF licenses this file to You under the Apache License, Version 2.0
 * (the "License"); you may not use this file except in compliance with
 * the License.  You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.amqphub.spring.boot.jms.example;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.jms.core.JmsTemplate;
import org.springframework.stereotype.Component;

@Component
public class HelloWorldMessageProducer implements CommandLineRunner {

    private static final Logger LOG = LoggerFactory.getLogger(HelloWorldMessageProducer.class);

    @Autowired
    public JmsTemplate jmsTemplate;

    @Override
    public void run(String... strings) throws Exception {
        DateTimeFormatter dtf = DateTimeFormatter.ofPattern("dd/MM/yyyy HH:mm:ss");
        LocalDateTime nowI = LocalDateTime.now();
        for (int i = 0; i < 2; i++) {
            final String messageText = "Hello World " + i;
            LOG.info("============= Sending: " + messageText);
            sendMessage(getPayload());
        }
        LocalDateTime nowF = LocalDateTime.now();
        System.out.println("Inicial " + dtf.format(nowI));
        System.out.println("Final " + dtf.format(nowF));
    }

    public void sendMessage(String payload) {
        this.jmsTemplate.convertAndSend("example", payload);
    }

    public String getPayload() {
        String payload ="{\"name\":\"Pankaj Kumar\",\"age\":32}";
        return payload;
    }
}

