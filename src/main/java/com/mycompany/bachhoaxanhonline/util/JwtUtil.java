package com.mycompany.bachhoaxanhonline.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import java.nio.charset.StandardCharsets;
import java.util.Date;
import javax.crypto.SecretKey;

public class JwtUtil {

    // 256-bit secret key for HMAC-SHA256 loaded from environment / .env
    private static final String SECRET_KEY_STRING = ConfigUtil.get(
            "JWT_SECRET", "BachHoaXanh_Super_Secure_JWT_Secret_Key_2026_Min_32_Chars!");
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(SECRET_KEY_STRING.getBytes(StandardCharsets.UTF_8));

    // Access Token validity: 1 hour (in ms)
    private static final long ACCESS_TOKEN_VALIDITY_MS = 60 * 60 * 1000L;

    // Refresh Token validity: 7 days (in ms)
    private static final long REFRESH_TOKEN_VALIDITY_MS = 7 * 24 * 60 * 60 * 1000L;

    public static String generateAccessToken(String userId, String ten, String role) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + ACCESS_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .subject(userId)
                .claim("ten", ten)
                .claim("role", role)
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public static String generateRefreshToken(String userId) {
        Date now = new Date();
        Date expiryDate = new Date(now.getTime() + REFRESH_TOKEN_VALIDITY_MS);

        return Jwts.builder()
                .subject(userId)
                .claim("type", "REFRESH")
                .issuedAt(now)
                .expiration(expiryDate)
                .signWith(SECRET_KEY)
                .compact();
    }

    public static Claims parseToken(String token) throws JwtException {
        return Jwts.parser()
                .verifyWith(SECRET_KEY)
                .build()
                .parseSignedClaims(token)
                .getPayload();
    }

    public static boolean validateToken(String token) {
        try {
            Claims claims = parseToken(token);
            return !claims.getExpiration().before(new Date());
        } catch (JwtException | IllegalArgumentException e) {
            return false;
        }
    }

    public static String getUserIdFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.getSubject();
    }

    public static String getRoleFromToken(String token) {
        Claims claims = parseToken(token);
        return claims.get("role", String.class);
    }
}
