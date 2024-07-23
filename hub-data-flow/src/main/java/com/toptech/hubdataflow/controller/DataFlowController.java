package com.toptech.hubdataflow.controller;

import com.toptech.hubdataflow.broker.ReactorKafka;
import com.toptech.hubdataflow.model.BrokerConfig;
import com.toptech.hubdataflow.model.Message;
import com.toptech.hubdataflow.service.HubService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class DataFlowController {

    private static final Logger logger = LoggerFactory.getLogger(DataFlowController.class);

    @Autowired
    private HubService hubService;

    @Autowired
    private ReactorKafka reactorKafka;


    @GetMapping(value = "/broker")
    public Mono<ResponseEntity<?>> getBrokerConfig() {
        logger.info("Inside getBrokerConfig...");
        return Mono.just(ResponseEntity.ok(hubService.getBrokerConfig()));
    }

    @PostMapping(value = "/broker")
    public Mono<ResponseEntity<?>> setBrokerConfig(@RequestBody BrokerConfig brokerConfig) {
        logger.info("Inside setBrokerConfig...");
        // hubService.updateModelParams(brokerConfig);
        return Mono.just(ResponseEntity.ok(new Message("Set Broker Config")));
    }


}


