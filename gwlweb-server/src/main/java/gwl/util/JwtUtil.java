package gwl.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import io.jsonwebtoken.security.Keys;

import javax.crypto.SecretKey;

import java.nio.charset.StandardCharsets;
import java.util.Date;

public class JwtUtil {// 使用 Keys.secretKeyFor 生成安全密钥
    // private static final SecretKey SECRET_KEY =
    // Keys.secretKeyFor(SignatureAlgorithm.HS256);
    private static final SecretKey SECRET_KEY = Keys.hmacShaKeyFor(
            "my-test-secret-my-test-secret-my-test-secret".getBytes(StandardCharsets.UTF_8));

    public static String generateToken(Long id) {
        return Jwts.builder()
                .claim("id", id)
                .setIssuedAt(new Date())
                //测试用，暂时永不过期
                //.setExpiration(new Date(System.currentTimeMillis() + 7 * 24 * 60 * 60 * 1000L)) // 一周
                .signWith(SignatureAlgorithm.HS256, SECRET_KEY)
                .compact();
    }

    public static long parseToken(String token) {
        Claims claims = Jwts.parser()
                .setSigningKey(SECRET_KEY)
                .parseClaimsJws(token)
                .getBody();
        return claims.get("id", Long.class);
    }
}
