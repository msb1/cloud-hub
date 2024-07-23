package com.toptech.hubresource.service;

import com.toptech.hubresource.model.Role;
import com.toptech.hubresource.model.User;
import com.toptech.hubresource.respository.UserRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.ReactiveUserDetailsService;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.Arrays;

@Service
public class UserService implements ReactiveUserDetailsService {

    private static Logger logger = LoggerFactory.getLogger(UserService.class);

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private BCryptPasswordEncoder passwordEncoder;

    public Mono<User> createUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setEnabled(true);
        user.setRoles(Arrays.asList(Role.ROLE_USER));
        return userRepository.save(user);
    }

    public Mono<User> updateUser(User user) {
        String encodedPassword = passwordEncoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        user.setEmail(user.getEmail());
        user.setUsername(user.getUsername());
        user.setFirstname(user.getFirstname());
        user.setLastname(user.getLastname());
        user.setEnabled(true);
        return userRepository.save(user);
    }

    public Flux<User> getAllUsers() {
        return userRepository.findAll();
    }

    public Mono<User> findUser(String id) {
        return userRepository.findById(id);
    }

    public Mono<Void> deleteById(String id) {
        return userRepository.deleteById(id);
    }

    public Mono<Void> deleteByUser(String username) {
        return userRepository.deleteByUsername(username);
    }

    public Mono<Void> deleteAll() {return userRepository.deleteAll(); }

    public Mono<Boolean> validateUser(String username, String password) {
        Mono<User> user = userRepository.findByUsername(username);
        return user.map(u -> {
            return passwordEncoder.matches(password, u.getPassword());
        });
    }

    @Override
    public Mono<UserDetails> findByUsername(String username) {
        return userRepository
                .findByUsername(username)
                .switchIfEmpty(Mono.defer(() -> {
                    return Mono.empty();
                    // return Mono.error(new UsernameNotFoundException("User Not Found"));
                }))
                .map(user -> {
                    // logger.info("findByUserName: " + user.getAuthUser().toString());
                    return user.getAuthUser();
                });
    }
}
