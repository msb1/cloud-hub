package com.toptech.hubdataflow;

import com.toptech.hubdataflow.broker.ReactorKafka;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class HubDataFlowApplication implements ApplicationRunner {

	private static final Logger logger = LoggerFactory.getLogger(HubDataFlowApplication.class);

	@Autowired
	private ReactorKafka reactorKafka;

	public static void main(String[] args) {
		logger.info("STARTING : HubDataFlow application...");
		SpringApplication.run(HubDataFlowApplication.class, args);
		logger.info("STOPPING : HubDataFlow application...");
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		reactorKafka.init();
		reactorKafka.reactorReceive().subscribe();
	}

}
