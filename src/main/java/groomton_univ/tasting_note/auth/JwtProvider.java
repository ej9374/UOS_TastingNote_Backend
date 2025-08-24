package groomton_univ.tasting_note.auth;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.JwtException;
import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.security.Keys;
import jakarta.annotation.PostConstruct;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.crypto.SecretKey;
import java.nio.charset.StandardCharsets;
import java.util.Date;

@Component
public class JwtProvider {
    @Value("${jwt.access-secret}")
    private String jwtKeyString;

    private SecretKey jwtKey;

    @PostConstruct
    public void init() {
        if (jwtKeyString == null) {
            throw new IllegalArgumentException("jwtKeyString 값이 없습니다.");
        }
        this.jwtKey = Keys.hmacShaKeyFor(jwtKeyString.getBytes(StandardCharsets.UTF_8));
    }

    public String createToken(Long kakaoId) {
        String token = Jwts.builder()
                .subject(kakaoId.toString())
                .issuedAt(new Date())
                .expiration(new Date(System.currentTimeMillis() + 1000L * 60 * 60))
                .signWith(jwtKey)
                .compact();
        return token;
    }

    public boolean validateToken(String token){
        try {
            Jwts.parser().verifyWith(jwtKey).build().parseClaimsJws(token);
            return true;
        } catch (JwtException | IllegalArgumentException e){
            return false; // 파싱/서명/만료/형식 오류 모두 false
        }
    }

    public String extractKakaoId(String token) {
        Jws<Claims> claims = Jwts.parser()
                .verifyWith(jwtKey)
                .build()
                .parseClaimsJws(token);
        return claims.getBody().getSubject();
    }
}
