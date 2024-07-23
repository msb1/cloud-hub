package com.toptech.hubdiscovery;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.netflix.eureka.server.EnableEurekaServer;

@EnableEurekaServer
@SpringBootApplication
public class HubDiscoveryApplication {

	public static void main(String[] args) {
		SpringApplication.run(HubDiscoveryApplication.class, args);
	}

}
