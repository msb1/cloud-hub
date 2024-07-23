package com.toptech.hubdataflow.service;

import com.toptech.hubdataflow.broker.ReactorKafka;
import com.toptech.hubdataflow.model.BrokerConfig;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

@Service
public class HubService {

    private static final Logger logger = LoggerFactory.getLogger(HubService.class);

    @Autowired
    private ReactorKafka reactorKafka;

    public Mono<BrokerConfig> getBrokerConfig() {
        String hubname = reactorKafka.getHubname();
        String bootstrapServers = reactorKafka.getBootstrapServers();
        // String modelKey = reactorKafka.getModelKey();
        String modelKey = "SVC";
        String dataTopic = reactorKafka.getDataTopic();
        // String modelTopic = reactorKafka.getModelTopic();
        String modelTopic = "assorted";
        String processedTopic = reactorKafka.getProcessedTopic();
        BrokerConfig brokerConfig = new BrokerConfig(hubname, bootstrapServers, modelKey, dataTopic, modelTopic, processedTopic);
        return Mono.just(brokerConfig);
    }

//    public Mono<Void> updateModelParams(BrokerConfig brokerConfig) {
//        // set topics and model key
//        reactorKafka.setModelTopic(brokerConfig.getModelTopic());
//        reactorKafka.setModelKey(brokerConfig.getModelKey());
//        return Mono.empty();
//    }
}

