package com.toptech.hubresource.controller;

import com.toptech.hubresource.config.JWTUtil;
import com.toptech.hubresource.model.AuthRequest;
import com.toptech.hubresource.model.AuthResponse;
import com.toptech.hubresource.model.Message;
import com.toptech.hubresource.model.User;
import com.toptech.hubresource.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RestController;
import reactor.core.publisher.Mono;

@RestController
public class HubResourceController {

    private static final Logger logger = LoggerFactory.getLogger(HubResourceController.class);

    @Autowired
    private JWTUtil jwtUtil;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    @Autowired
    private UserService userService;

    @GetMapping(value = "/")
    public Mono<ResponseEntity<?>> init() {
        logger.info("INITIALIZE connection between React and Spring Security...");
        return Mono.just(ResponseEntity.ok(new Message("Init Spring Security...")));
    }

    @PostMapping(value = "/login")
    public Mono<ResponseEntity<?>> login(@RequestBody AuthRequest ar) {
        logger.info("Inside login..." + ar.toString());

        return userService.findByUsername(ar.getUsername()).map((authUser) -> {
            logger.info(authUser.toString());
            if (passwordEncoder.matches(ar.getPassword(), authUser.getPassword())) {
                // user is authorized - send appropriate response
                logger.info("USER is authorized..");
                return ResponseEntity.ok(new AuthResponse(authUser.getUsername(), jwtUtil.generateToken(authUser)));
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).build();
            }
        }).defaultIfEmpty(ResponseEntity.status(HttpStatus.UNAUTHORIZED).build());
    }

    @PostMapping(value = "/signout")
    public Mono<ResponseEntity<?>> signout(@RequestBody Message token) {
        logger.info("Inside signout..." + token.toString());
        String msg = "User " + jwtUtil.getUsernameFromToken(token.getContent()) + " is logged out...";
        logger.info(msg);
        return Mono.just(ResponseEntity.ok(new Message(msg)));
    }

    @PostMapping(value = "/register")
    public Mono<ResponseEntity<?>> register(@RequestBody User user) {
        logger.info("Inside register - User: " + user.toString());
        userService.createUser(user).subscribe();
        String msg = "User " + user.getUsername() + " is registered...";
        logger.info(msg);
        return Mono.just(ResponseEntity.ok(new Message(msg)));
    }

}


