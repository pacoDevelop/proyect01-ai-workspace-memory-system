package com.jrecruiter.userservice.infrastructure.config;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.oauth2.jwt.JwtDecoder;
import org.springframework.security.oauth2.jwt.NimbusJwtDecoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Spring Security Configuration
 * JWT + OAuth2 setup.
 * 
 * @author GitHub Copilot / TASK-015
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {
    
    /**
     * Security filter chain for API endpoints
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        http
            .cors()
            .and()
            .csrf().disable()
            .sessionManagement().sessionCreationPolicy(SessionCreationPolicy.STATELESS)
            .and()
            .authorizeRequests()
                // Public endpoints
                .antMatchers(HttpMethod.POST, "/api/auth/login", "/api/auth/register").permitAll()
                .antMatchers(HttpMethod.POST, "/api/candidates/register").permitAll()
                .antMatchers("/actuator/health").permitAll()
                
                // Protected endpoints
                .antMatchers(HttpMethod.GET, "/api/candidates/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/candidates/**").authenticated()
                .antMatchers(HttpMethod.PUT, "/api/candidates/**").authenticated()
                .antMatchers(HttpMethod.DELETE, "/api/candidates/**").authenticated()
                .antMatchers(HttpMethod.GET, "/api/applications/**").authenticated()
                .antMatchers(HttpMethod.POST, "/api/applications/**").authenticated()
                
                // All other requests require authentication
                .anyRequest().authenticated()
            .and()
            .oauth2ResourceServer()
                .jwt();
        
        return http.build();
    }
    
    /**
     * Password encoder
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }
    
    /**
     * CORS configuration
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();
        configuration.setAllowedOrigins(Arrays.asList("http://localhost:3000", "http://localhost:4200"));
        configuration.setAllowedMethods(Arrays.asList("GET", "POST", "PUT", "DELETE", "OPTIONS"));
        configuration.setAllowedHeaders(Arrays.asList("*"));
        configuration.setAllowCredentials(true);
        
        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);
        return source;
    }
}
