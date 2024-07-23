package com.toptech.hubresource.config;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;

import javax.crypto.spec.SecretKeySpec;
import java.io.Serializable;
import java.nio.charset.StandardCharsets;
import java.security.Key;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;


@Component
public class JWTUtil implements Serializable {

    private static final long serialVersionUID = 9074879532101790462L;
    private static Logger logger = LoggerFactory.getLogger(JWTUtil.class);
    private Key jwk;
    private static final long EXPIRATION_TIME = 3600;
    // private Key key = Keys.secretKeyFor(SignatureAlgorithm.HS256);

    public JWTUtil(@Value("${security.jwk.k}") String jwkString) {
        byte[] jwkBytes = jwkString.getBytes(StandardCharsets.UTF_8);
        // rebuild key using SecretKeySpec
        jwk = new SecretKeySpec(jwkBytes, SignatureAlgorithm.HS512.getJcaName());
    }

    public Claims getAllClaimsFromToken(String token) {
        return Jwts.parser().setSigningKey(this.jwk).parseClaimsJws(token).getBody();
    }

    public String getUsernameFromToken(String token) {
        return getAllClaimsFromToken(token).getSubject();
    }

    public Date getExpirationDateFromToken(String token) {
        return getAllClaimsFromToken(token).getExpiration();
    }

    private Boolean isTokenExpired(String token) {
        long millis = System.currentTimeMillis();
        final Date expiration = getExpirationDateFromToken(token);
        return expiration.before(new Date(millis));
    }

    public String generateToken(UserDetails userDetails) {
        Map<String, Object> claims = new HashMap<>();
        claims.put("role", userDetails.getAuthorities());
        String token = doGenerateToken(claims, userDetails.getUsername());
        // logger.info("Claims: " + claims.toString());
        // logger.info("Token: " + token);
        // logger.info("Username from Token: " + getUsernameFromToken(token));
        return token;
    }

    private String doGenerateToken(Map<String, Object> claims, String username) {

        long millis = System.currentTimeMillis();
        Date issueDate=new Date(millis);
        Date expirationDate = new Date(millis + EXPIRATION_TIME * 1000);

        return Jwts.builder()
                .setClaims(claims)
                .setSubject(username)
                .setIssuedAt(issueDate)
                .setExpiration(expirationDate)
                .signWith(this.jwk)
                .compact();
    }

    public Boolean validateToken(String token) {
        return !isTokenExpired(token);
    }

}

