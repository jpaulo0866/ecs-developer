package br.com.jschmidt.bucket_manager_bff.security;

import br.com.jschmidt.bucket_manager_bff.security.jwt.JwtAuthenticationFilter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

@Configuration
@EnableWebSecurity
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final boolean securityEnabled;

    public SecurityConfig(JwtAuthenticationFilter jwtAuthenticationFilter,
                          @Value("${security.enabled:true}") boolean securityEnabled) {
        this.jwtAuthenticationFilter = jwtAuthenticationFilter;
        this.securityEnabled = securityEnabled;
    }

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        if (!securityEnabled) {
            http.authorizeHttpRequests(authz -> authz.anyRequest().permitAll())
                .csrf(AbstractHttpConfigurer::disable);
            return http.build();
        }

        http
                .authorizeHttpRequests(authz -> authz
                        // Public endpoints
                        .requestMatchers("/", "/login", "/session", "/error", "/actuator/health").permitAll()
                        .requestMatchers("/auth/**").permitAll()
                        // Protected API endpoints
                        .requestMatchers("/api/**").authenticated()
                        .anyRequest().authenticated()
                )
                .oauth2Login(oauth2 -> oauth2
                        .loginPage("/login")
                        .defaultSuccessUrl("/session", true)
                        .failureUrl("/login?error=true")
                )
                .oauth2ResourceServer(oauth2 -> oauth2.jwt(jwt -> {}))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
                .csrf(AbstractHttpConfigurer::disable);

        return http.build();
    }

}
