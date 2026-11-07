package com.example.demo.security;

import jakarta.servlet.http.HttpServletResponse;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.http.MediaType;
import org.springframework.http.ProblemDetail;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import tools.jackson.databind.json.JsonMapper;

import java.io.IOException;
import java.nio.charset.StandardCharsets;

/**
 * Seguridad stateless con JWT.
 * Públicos: registro/login, catálogo de productos (espejo de solo lectura),
 * health/ping y Swagger. Todo lo demás exige Bearer token válido.
 */
@Configuration
@EnableWebSecurity
public class SecurityConfig {

    @Bean
    public PasswordEncoder passwordEncoder() {
        return new BCryptPasswordEncoder();
    }

    @Bean
    public SecurityFilterChain securityFilterChain(
            HttpSecurity http,
            JwtAuthenticationFilter jwtAuthenticationFilter,
            JsonMapper jsonMapper) throws Exception {

        http
                .csrf(csrf -> csrf.disable())
                .sessionManagement(session ->
                        session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/productos/**",
                                "/health",
                                "/ping",
                                "/error",
                                "/v3/api-docs/**",
                                "/swagger-ui/**",
                                "/swagger-ui.html",
                                "/webjars/**")
                        .permitAll()
                        .requestMatchers(HttpMethod.OPTIONS, "/**").permitAll()
                        .anyRequest().authenticated())
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) ->
                                escribirProblema(response, jsonMapper, HttpServletResponse.SC_UNAUTHORIZED,
                                        "No autenticado",
                                        "Se requiere un token Bearer válido para acceder a este recurso"))
                        .accessDeniedHandler((request, response, accessDeniedException) ->
                                escribirProblema(response, jsonMapper, HttpServletResponse.SC_FORBIDDEN,
                                        "Acceso denegado",
                                        "No tenés permisos para acceder a este recurso")))
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }

    private static void escribirProblema(
            HttpServletResponse response,
            JsonMapper jsonMapper,
            int status,
            String title,
            String detail) throws IOException {
        ProblemDetail problema = ProblemDetail.forStatusAndDetail(
                org.springframework.http.HttpStatusCode.valueOf(status), detail);
        problema.setTitle(title);
        response.setStatus(status);
        response.setCharacterEncoding(StandardCharsets.UTF_8.name());
        response.setContentType(MediaType.APPLICATION_PROBLEM_JSON_VALUE);
        jsonMapper.writeValue(response.getOutputStream(), problema);
    }
}
