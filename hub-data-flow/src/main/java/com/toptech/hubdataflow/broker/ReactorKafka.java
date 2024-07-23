package com.toptech.hubdataflow.broker;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.MapperFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.toptech.hubdataflow.model.DataRecord;
import org.apache.kafka.clients.consumer.ConsumerConfig;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import reactor.core.publisher.*;
import reactor.core.scheduler.Scheduler;
import reactor.core.scheduler.Schedulers;
import reactor.kafka.receiver.KafkaReceiver;
import reactor.kafka.receiver.ReceiverOptions;
import reactor.kafka.receiver.ReceiverRecord;
import reactor.kafka.sender.KafkaSender;
import reactor.kafka.sender.SenderOptions;
import reactor.kafka.sender.SenderRecord;
import reactor.kafka.sender.SenderResult;

import java.util.*;

@Service
public class ReactorKafka {

    private static final Logger logger = LoggerFactory.getLogger(ReactorKafka.class);

    private final Map<String, Object> consumerProps;
    private final Map<String, Object> producerProps;
    private KafkaReceiver<String, String> receiver;
    private KafkaSender<String, String> sender;
    private final ObjectMapper mapper;
    private final FluxProcessor<Object, Object> processor;
    private final FluxSink<Object> sink;
    private final String bootStrapServers;
    private String dataTopic;
    private String processedTopic;
    private String modelTopic;
    private String hubname;
    private final boolean dbStore;
    private final boolean sendDataToModel;
    private long dataEvents;
    private long modelEvents;
    private long processedEvents;
    private final int maxSensorPtsToPlot = 50;
    private final List<String> producerMessages;
    private final List<String> consumerMessages;
    private final Map<String, List<Double>> sensorDataLists;
    private final List<String> sensorTimePts;

    final private Scheduler scheduler = Schedulers.newSingle("toptech", true);

    public ReactorKafka(@Value("${consumer.key.deserializer}") String keyDeSer,
                        @Value("${consumer.value.deserializer}") String valDeSer,
                        @Value("${consumer.group.id}") String groupId,
                        @Value("${consumer.bootstrap.servers}") String consumerServers,
                        @Value("${consumer.auto.offset.reset}") String autoReset,
                        @Value("${producer.acks}") String acks,
                        @Value("${producer.bootstrap.servers}") String producerServers,
                        @Value("${producer.client.id}") String clientId,
                        @Value("${producer.key.serializer}") String keySer,
                        @Value("${producer.value.serializer}") String valSer,
                        @Value("${producer.model.topic}") String modelTopic,
                        @Value("${producer.max.in.flight.requests.per.connection}") String inFlightReq,
                        @Value("${consumer.data.topic}") String dataTopic,
                        @Value("${consumer.processed.topic}") String processedTopic,
                        @Value("${consumer.db.store.data}") boolean dbStore,
                        @Value("${consumer.process.data}") boolean sendDataToModel,
                        @Value("${epd.hubname}") String hubname) {
        // inject consumer properties
        this.consumerProps = new HashMap<>();
        consumerProps.put(ConsumerConfig.KEY_DESERIALIZER_CLASS_CONFIG, keyDeSer);
        consumerProps.put(ConsumerConfig.VALUE_DESERIALIZER_CLASS_CONFIG, valDeSer);
        consumerProps.put(ConsumerConfig.GROUP_ID_CONFIG, groupId);
        consumerProps.put(ConsumerConfig.BOOTSTRAP_SERVERS_CONFIG, consumerServers);
        consumerProps.put(ConsumerConfig.AUTO_OFFSET_RESET_CONFIG, autoReset);
        // inject producer properties
        this.producerProps = new HashMap<>();
        producerProps.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, keySer);
        producerProps.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, valSer);
        producerProps.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, producerServers);
        producerProps.put(ProducerConfig.MAX_IN_FLIGHT_REQUESTS_PER_CONNECTION, inFlightReq);
        producerProps.put(ProducerConfig.CLIENT_ID_CONFIG, clientId);
        producerProps.put(ProducerConfig.ACKS_CONFIG, acks);
        // inject topics
        this.dataTopic = dataTopic;
        this.processedTopic = processedTopic;
        this.modelTopic = modelTopic;
        this.hubname = hubname;
        this.dbStore = dbStore;
        this.sendDataToModel = sendDataToModel;
        this.bootStrapServers = producerServers;
        // init parameters
        this.dataEvents = 0;
        this.modelEvents = 0;
        this.processedEvents = 0;
        this.processor = DirectProcessor.create().serialize();
        this.sink = processor.sink();
        // init message lists
        this.producerMessages = new ArrayList<>();
        this.consumerMessages = new ArrayList<>();
        this.sensorDataLists = new HashMap<>();
        this.sensorTimePts = new ArrayList<>();
        // init Jackson mapper
        this.mapper = new ObjectMapper();
        this.mapper.configure(MapperFeature.ACCEPT_CASE_INSENSITIVE_PROPERTIES, true);
    }

    public void init() {
        // init Kafka sender
        SenderOptions<String, String> senderOptions = SenderOptions.create(producerProps);
        this.sender = KafkaSender.create(senderOptions);
        // init Kafka receiver
        ReceiverOptions<String, String> receiverOptions = ReceiverOptions.create(consumerProps);
        this.receiver = KafkaReceiver
                .create(receiverOptions.subscription(Arrays.asList(this.dataTopic, this.processedTopic))
                        .addAssignListener(partitions -> logger.debug("onPartitionsAssigned {}", partitions))
                        .addRevokeListener(partitions -> logger.debug("onPartitionsRevoked {}", partitions)));
    }

    public String getDataTopic() {
        return this.dataTopic;
    }

    public String getModelTopic() {
        return this.modelTopic;
    }

    public String getProcessedTopic() {
        return this.processedTopic;
    }

    public String getHubname() {
        return this.hubname;
    }

    public String getBootstrapServers() {
        return this.bootStrapServers;
    }

    public void setDataTopic(String dataTopic) {
        this.dataTopic = dataTopic;
    }

    public void setModelTopic(String modelTopic) {
        this.modelTopic = modelTopic;
    }

    public void setProcessedTopic(String processedTopic) {
        this.processedTopic = processedTopic;
    }

    public void setHubname(String hubname) {
        this.hubname = hubname;
    }

    public Flux<String> getMessageFlux() {
        return this.processor.map(Object::toString);
    }

    private void updateSink(String sinkVal) {
        this.sink.next(sinkVal);
    }

    private void setStatus() {
        String statusString = String.format("status;{\"dataEvents\" : %s, \"modelEvents\" : %s, \"processedEvents\" : %s}",
                dataEvents, modelEvents, processedEvents);
        updateSink(statusString);
    }

    public Mono<SenderResult<String>> sendMessage(String topic, String key, String msg) {
        // update modelEvents if actual result is positive
        if (msg.charAt(msg.length() - 2) == '1') {
            modelEvents++;
        }
        String meta = topic + modelEvents;
        SenderRecord<String, String, String> record = SenderRecord.create(new ProducerRecord<>(topic, key, msg), meta);
        this.updateSink("producer;" + updateProducerMessages(record.value()));
        this.setStatus();
        this.updateSensorCat(Objects.requireNonNull(deserialize(record.value())));
        // send message to reactor kafka sender (producer)
        return sender.send(Mono.just(record)).next()
                .doOnError(e -> logger.error("Reactor Kafka Send failed for CorrelationMetaData: " + meta, e))
                .doOnNext(r -> logger.info("Sender: topic={} -- offset={} -- key={} -- value={}",
                        topic, r.recordMetadata().offset(), record.key(), record.value()));
    }

    public Flux<ReceiverRecord<String, String>> reactorReceive() {
        return receiver
                .receive().publishOn(scheduler)
                .concatMap(record -> {
                    if (record.topic().equals(dataTopic)) {
                        dataEvents++;
                        DataRecord dataRecord = deserialize(record.value());
                        assert dataRecord != null;
                        if (sendDataToModel) {
                            sendMessage(modelTopic, dataRecord.getName(), record.value()).subscribe();
                        }
                        if (dbStore) {
                            storeInDB(modelTopic, dataRecord.getName(), record.value());
                        }
                    } else if (record.topic().equals(processedTopic)) {
                        // update processed events if predicted result is positive
                        if (record.value().charAt(record.value().length() - 2) == '1') {
                            processedEvents++;
                        }
                        if (dbStore) {
                            storeInDB(this.processedTopic, record.key(), record.value());
                        }
                        this.updateSink("consumer;" + updateConsumerMessages(record.value()));
                    }
                    this.setStatus();
                    return Mono.just(record);
                });
    }

    private void storeInDB(String topic, String key, String value) {
        // TODO: change to reactive Mongo -- parameter should be Mono<SomeClass>
        // TODO: flatmap -- insert if dataTopic; update if processedTopic
        logger.info("Receiver: topic={} -- key={} -- value={}", topic, key, value);
    }

    private String updateProducerMessages(String msg) {
        String processedMsg = msg.substring(1, msg.length() - 1).replaceAll(",", ", ");
        this.producerMessages.add(processedMsg);
        if (this.producerMessages.size() > 5) {
            this.producerMessages.remove(0);
        }
        StringBuilder sb = new StringBuilder();
        for (String s : this.producerMessages) {
            sb.append("<p>").append(s).append("</p>");
        }
        return sb.toString();
    }

    private String updateConsumerMessages(String msg) {
        String processedMsg = msg.substring(1, msg.length() - 1).replaceAll(",", ", ");
        this.consumerMessages.add(processedMsg);
        if (this.consumerMessages.size() > 5) {
            this.consumerMessages.remove(0);
        }
        StringBuilder sb = new StringBuilder();
        for (String s : this.consumerMessages) {
            sb.append("<p>").append(s).append("</p>");
        }
        return sb.toString();
    }

    private DataRecord deserialize(String jsonString) {
        try {
            return this.mapper.readValue(jsonString, DataRecord.class);
        } catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
            return null;
        }
    }

    private void updateSensorCat(DataRecord dataRecord) {
        Map<String, Double> sensors = dataRecord.getSensors();
        sensors.forEach((key, value) -> {
            if (sensorDataLists.containsKey(key)) {
                sensorDataLists.get(key).add(sensors.get(key));
                if (sensorDataLists.get(key).size() > maxSensorPtsToPlot) {
                    sensorDataLists.get(key).remove(0);
                }
            } else {
                sensorDataLists.put(key, new ArrayList<>(Collections.singletonList(sensors.get(key))));
            }
        });
        sensorTimePts.add(dataRecord.getCurrentTime());
        if (sensorTimePts.size() > maxSensorPtsToPlot) {
            sensorTimePts.remove(0);
        }
        try {
            String timeDataString = "time;" + this.mapper.writeValueAsString(sensorTimePts);
            this.updateSink(timeDataString);
            String sensorDataString = "sensors;" + this.mapper.writeValueAsString(sensorDataLists);
            this.updateSink(sensorDataString);
            String catDataString = "cats;" + this.mapper.writeValueAsString(dataRecord.getCategories());
            this.updateSink(catDataString);
        } catch (JsonProcessingException ex) {
            logger.error(ex.getMessage());
        }
    }

//    public void close() {
//        if (this.sender != null) {
//            this.sender.close();
//        }
//        if (this.scheduler != null) {
//            this.scheduler.dispose();
//        }
//    }
}

