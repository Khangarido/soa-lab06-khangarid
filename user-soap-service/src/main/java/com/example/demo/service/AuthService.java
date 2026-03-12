package com.example.demo.service;

import java.util.Date;

import javax.crypto.SecretKey;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import com.example.demo.entity.UserAuth;
import com.example.demo.repository.UserAuthRepository;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.io.Decoders;
import io.jsonwebtoken.security.Keys;

// Batalgaajuulaltiin logic (Authentication business logic)
@Service
public class AuthService {

    private final UserAuthRepository userAuthRepository;

    @Value("${jwt.secret}")
    private String jwtSecret;

    @Value("${jwt.expiration-ms}")
    private long jwtExpirationMs;

    public AuthService(UserAuthRepository userAuthRepository) {
        this.userAuthRepository = userAuthRepository;
    }

    private SecretKey getSigningKey() {
        return Keys.hmacShaKeyFor(Decoders.BASE64.decode(jwtSecret));
    }

    // Hereglegch burtgeh
    public String registerUser(String username, String password) {
        if (userAuthRepository.findByUsername(username).isPresent()) {
            throw new RuntimeException("Hereglegchiin ner ashiglагdsan baina (Username already taken)");
        }
        UserAuth user = new UserAuth(username, password);
        userAuthRepository.save(user);
        return "Amjilttai burtgegdlee (Registered successfully)";
    }

    // Hereglegch nevtreh - JWT token uusgeh
    public String loginUser(String username, String password) {
        UserAuth user = userAuthRepository.findByUsername(username)
                .orElseThrow(() -> new RuntimeException("Hereglegch oldsongui (User not found)"));

        if (!user.getPassword().equals(password)) {
            throw new RuntimeException("Nuuts ug buruu baina (Invalid password)");
        }

        String token = generateJwtToken(username);
        user.setToken(token);
        userAuthRepository.save(user);
        return token;
    }

    // Token shalgah
    public boolean validateToken(String token) {
        try {
            Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token);
            return true;
        } catch (Exception e) {
            return false;
        }
    }

    public String getUsernameFromToken(String token) {
        return Jwts.parser()
                .verifyWith(getSigningKey())
                .build()
                .parseSignedClaims(token)
                .getPayload()
                .getSubject();
    }

    private String generateJwtToken(String username) {
        Date now = new Date();
        Date expiry = new Date(now.getTime() + jwtExpirationMs);

        return Jwts.builder()
                .subject(username)
                .issuedAt(now)
                .expiration(expiry)
                .signWith(getSigningKey())
                .compact();
    }
}
