package com.vallem.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;

import javax.crypto.spec.SecretKeySpec;
import java.security.Key;
import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Base64;
import java.util.Date;

public class AuthService {
    private static final String SECRET_ID = System.getenv("SECRET_ID");
    private static final String SECRET_KEY = "YyQ*Y''FQ>5]k;Y!MX!?8;LtfSV^u3S&(ZWmg:VHqg;Y+49t8d";

    private static Key getKey() {
        return new SecretKeySpec(SECRET_KEY.getBytes(), SignatureAlgorithm.HS256.getJcaName());
    }

    public static boolean isAuthorized(String token) {
        if (token == null || token.isBlank()) return false;

        if (token.startsWith("Bearer ")) token = token.split(" ")[1];
        try {
            return Jwts.parserBuilder()
                    .setSigningKey(getKey())
                    .build()
                    .parseClaimsJws(token)
                    .getBody()
                    .getSubject()
                    .equals(SECRET_ID);
        } catch (Exception e) {
            e.printStackTrace();
        }

        return false;
    }

    public static String generateToken(String subject) {
        if (!subject.equals(SECRET_ID)) return null;
        return Jwts.builder()
                .setSubject(subject)
                .setIssuedAt(new Date())
                .setExpiration(
                        Date.from(
                                LocalDateTime.now().plusDays(1L)
                                        .atZone(ZoneId.systemDefault())
                                        .toInstant()))
                .signWith(getKey())
                .compact();
    }
}
