package com.lasystems.lagenda.security;

import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.CorsConfigurationSource;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;

import java.util.Arrays;

/**
 * Configuração de segurança da aplicação.
 *
 * Suporta dois tipos de autenticação:
 * 1. API Key (para N8N e integrações) - ATIVO
 * 2. JWT (para frontend - a implementar)
 */
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final ApiKeyAuthenticationFilter apiKeyAuthenticationFilter;

    @Bean
    public SecurityFilterChain filterChain(HttpSecurity http) throws Exception {
        return http
                // Desabilitar CSRF (não necessário para APIs stateless)
                .csrf(csrf -> csrf.disable())

                // Configurar CORS
                .cors(cors -> cors.configurationSource(corsConfigurationSource()))

                // Stateless - sem sessões
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS)
                )

                // Desabilitar HTTP Basic (evita a senha gerada no log)
                .httpBasic(httpBasic -> httpBasic.disable())

                // Desabilitar form login
                .formLogin(formLogin -> formLogin.disable())

                // Configurar autorização de endpoints
                .authorizeHttpRequests(auth -> auth
                        // Endpoints públicos (sem autenticação)
                        .requestMatchers("/health", "/actuator/**").permitAll()
                        .requestMatchers("/debug/**").permitAll() // REMOVER EM PRODUÇÃO!
                        .requestMatchers("/bootstrap/**").permitAll() // Bootstrap inicial

                        // Endpoints de API Keys (requer autenticação)
                        .requestMatchers("/api/keys/**").authenticated()

                        // Endpoints de appointments (requer API Key ou JWT)
                        .requestMatchers("/appointments/**").authenticated()

                        // Qualquer outra rota requer autenticação
                        .anyRequest().authenticated()
                )

                // Adicionar filtro de API Key ANTES do filtro padrão
                .addFilterBefore(
                        apiKeyAuthenticationFilter,
                        UsernamePasswordAuthenticationFilter.class
                )

                .build();
    }

    /**
     * Configuração de CORS para permitir requisições do frontend.
     */
    @Bean
    public CorsConfigurationSource corsConfigurationSource() {
        CorsConfiguration configuration = new CorsConfiguration();

        // Permitir origens do frontend (ajustar conforme necessário)
        configuration.setAllowedOrigins(Arrays.asList(
                "http://localhost:3000",  // React dev
                "http://localhost:5173",  // Vite dev
                "http://localhost:8081",  // Vue dev
                "https://seu-dominio.com" // Produção
        ));

        configuration.setAllowedMethods(Arrays.asList(
                "GET", "POST", "PUT", "DELETE", "PATCH", "OPTIONS"
        ));

        configuration.setAllowedHeaders(Arrays.asList(
                "Authorization",
                "Content-Type",
                "X-API-Key",
                "X-Requested-With",
                "Accept",
                "Origin",
                "Access-Control-Request-Method",
                "Access-Control-Request-Headers"
        ));

        configuration.setExposedHeaders(Arrays.asList(
                "Authorization",
                "X-Total-Count",
                "X-Page-Number",
                "X-Page-Size"
        ));

        configuration.setAllowCredentials(true);
        configuration.setMaxAge(3600L);

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", configuration);

        return source;
    }

    /**
     * Encoder de senhas BCrypt.
     * Usado para hash de API Keys e futuras senhas de usuários.
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder(12);
    }
}