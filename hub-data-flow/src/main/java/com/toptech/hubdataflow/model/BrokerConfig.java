package com.toptech.hubdataflow.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class BrokerConfig {
    private String hubname;
    private String bootstrapServers;
    private String modelKey;
    private String dataTopic;
    private String modelTopic;
    private String processedTopic;
}
