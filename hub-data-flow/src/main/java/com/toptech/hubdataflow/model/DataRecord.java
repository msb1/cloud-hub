package com.toptech.hubdataflow.model;

import lombok.Data;
import java.util.Map;

@Data
public class DataRecord {
    private String uuid;
    private String currentTime;
    private String name;
    private Map<String, Integer> categories;
    private Map<String, Double> sensors;
    private int result;
}
