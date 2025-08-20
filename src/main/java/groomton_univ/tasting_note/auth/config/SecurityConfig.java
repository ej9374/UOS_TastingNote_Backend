package groomton_univ.tasting_note.auth.config;

import groomton_univ.tasting_note.auth.JwtAuthenticationFilter;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;

    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
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
                                .requestMatchers("/api/v1/auth/kakao", "/api/v1/auth/register").permitAll()
                                .anyRequest().authenticated() // 카카오 로그인 경로는 누구나 접근 허용
                )

                // 6. 우리가 직접 만든 JwtAuthenticationFilter를 Spring Security 필터 체인에 추가
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}