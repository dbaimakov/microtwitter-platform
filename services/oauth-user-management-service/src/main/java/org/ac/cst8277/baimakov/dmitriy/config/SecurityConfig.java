package org.ac.cst8277.baimakov.dmitriy.config;

import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.ServerAuthenticationSuccessHandler;

@Configuration
@EnableWebFluxSecurity
@EnableConfigurationProperties(AppSecurityProperties.class)
public class SecurityConfig {

    @Bean
    public SecurityWebFilterChain securityWebFilterChain(
            ServerHttpSecurity http,
            ServerAuthenticationSuccessHandler oAuth2LoginSuccessHandler
    ) {
        return http
                .csrf(ServerHttpSecurity.CsrfSpec::disable)
                .httpBasic(ServerHttpSecurity.HttpBasicSpec::disable)
                .formLogin(ServerHttpSecurity.FormLoginSpec::disable)
                .logout(ServerHttpSecurity.LogoutSpec::disable)
                .authorizeExchange(exchange -> exchange
                        .pathMatchers(
                                "/",
                                "/index.html",
                                "/favicon.ico",
                                "/oauth2/**",
                                "/login/oauth2/code/**",
                                "/api/v1/auth/login/github",
                                "/api/v1/auth/jwt/inspect",
                                "/actuator/health",
                                "/actuator/info"
                        ).permitAll()
                        .anyExchange().permitAll()
                )
                .oauth2Login(oauth2 -> oauth2.authenticationSuccessHandler(oAuth2LoginSuccessHandler))
                .build();
    }
}
