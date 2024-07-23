package com.toptech.hubresource;

import com.toptech.hubresource.model.Role;
import com.toptech.hubresource.model.User;
import com.toptech.hubresource.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;

import java.util.Arrays;

@SpringBootApplication
public class HubResourceApplication implements ApplicationRunner {

	private static Logger logger = LoggerFactory.getLogger(HubResourceApplication.class);

	@Autowired
	private UserService userService;

	public static void main(String[] args) {
		logger.info("STARTING : HubResource application...");
		SpringApplication.run(HubResourceApplication.class, args);
		logger.info("STOPPING : HubResource application...");
	}

	@Override
	public void run(ApplicationArguments args) throws Exception {
		// create three dummy users (saved to MongoDB) for testing
		userService.deleteAll().subscribe();

		User user1 = new User();
		user1.setLastname("Waldo");
		user1.setFirstname("Barn");
		user1.setUsername("barnwaldo");
		user1.setEmail("barnwaldo@gmail.com");
		user1.setPassword("1234");
		user1.setRoles(Arrays.asList(Role.ROLE_ADMIN, Role.ROLE_USER));
		userService.createUser(user1).subscribe();
		logger.info(user1.toString());

		User user2 = new User();
		user2.setLastname("Rahbeck");
		user2.setFirstname("Johnson");
		user2.setUsername("jrchuck");
		user2.setEmail("jrchuck@yahoo.com");
		user2.setPassword("5678");
		user2.setRoles(Arrays.asList(Role.ROLE_ADMIN));
		userService.createUser(user2).subscribe();
		logger.info(user2.toString());

		User user3 = new User();
		user3.setLastname("Eps");
		user3.setFirstname("Nic");
		user3.setUsername("nice");
		user3.setEmail("nickei@msn.com");
		user3.setPassword("asdf");
		user3.setRoles(Arrays.asList(Role.ROLE_USER));
		userService.createUser(user3).subscribe();
		logger.info(user3.toString());
	}
}
