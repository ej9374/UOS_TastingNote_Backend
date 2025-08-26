package groomton_univ.tasting_note.auth.config;

import groomton_univ.tasting_note.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configuration.WebSecurityCustomizer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.List;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
                .cors(cors -> {})

                // 1. CSRF 보호 비활성화 (람다식으로 수정)
                .csrf((csrf) -> csrf.disable())

                // 2. 기본 로그인 폼 비활성화 (람다식으로 수정)
                .formLogin((formLogin) -> formLogin.disable())

                // 3. HTTP 기본 인증 비활성화 (람다식으로 수정)
                .httpBasic((httpBasic) -> httpBasic.disable())

                // 4. 세션을 사용하지 않는 Stateless 서버로 설정
                .sessionManagement((sessionManagement) ->
                        sessionManagement.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // 5. 요청 경로별 접근 권한 설정
                .authorizeHttpRequests((authorizeRequests) ->
                        authorizeRequests
                                .requestMatchers(
                                        "/api/v1/auth/**",
                                        "/api/notes/note/**",
                                        "/api/notes",
                                        "/api/search/**",
                                        "/api/v1/user/tags",
                                        "/api/v1/user/nickname/check"
                                )
                                .permitAll()
                                .anyRequest().authenticated() // 카카오 로그인 경로는 누구나 접근 허용
                )

                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
  
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // 허용할 Origin(출처) 설정
        configuration.setAllowedOrigins(List.of(
                "http://localhost:3000",
                "http://127.0.0.1:3000",
                "https://uos-tasting-note-frontend.vercel.app"
        ));

        // 허용할 HTTP Method 설정
        configuration.setAllowedMethods(List.of("GET", "POST", "PUT", "PATCH", "DELETE", "OPTIONS"));

        // 허용할 Header 설정
        configuration.setAllowedHeaders(List.of("*"));

        // Credentials(자격 증명) 허용 여부 설정
        configuration.setAllowCredentials(true);


        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration); // 모든 경로에 대해 위 설정 적용
        return source;
    }
}