package com.toptech.hubgateway;

import io.jsonwebtoken.Claims;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.authentication.ReactiveAuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.stereotype.Component;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;


@Component
public class AuthenticationManager implements ReactiveAuthenticationManager {

    private static Logger logger = LoggerFactory.getLogger(AuthenticationManager.class);

    @Autowired
    private JWTUtil jwtUtil;

    @Override
    @SuppressWarnings("unchecked")
    public Mono<Authentication> authenticate(Authentication authentication) {
        String authToken = authentication.getCredentials().toString();
        String username;
        try {
            logger.info("Authorization Token: " + authToken);
            username = jwtUtil.getUsernameFromToken(authToken);
            logger.info("User from Token: " + username);
        } catch (Exception e) {
            logger.info("Exception: " + e.getMessage());
            logger.info("User from Token: NULL");
            username = null;
        }
        if (username != null && jwtUtil.validateToken(authToken)) {
            Claims claims = jwtUtil.getAllClaimsFromToken(authToken);
            logger.info("Claims: " + claims.toString());
            List<Map<String,String>> rolesMap = claims.get("role", List.class);
            logger.info("RolesMap: " + rolesMap.toString());
            List<Role> roles = new ArrayList<>();
            for (Map<String,String>rolemap : rolesMap) {
                roles.add(Role.valueOf(rolemap.get("authority")));
            }
            UsernamePasswordAuthenticationToken auth = new UsernamePasswordAuthenticationToken(
                    username,
                    null,
                    roles.stream().map(authority -> new SimpleGrantedAuthority(authority.name())).collect(Collectors.toList())
            );
            return Mono.just(auth);
        } else {
            logger.info("username is NULL or token is invalid");
            return Mono.empty();
        }
    }
}
