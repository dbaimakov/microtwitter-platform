package org.ac.cst8277.baimakov.dmitriy.config;

import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.LinkedHashSet;
import java.util.Set;

@ConfigurationProperties(prefix = "app.security")
public class AppSecurityProperties {

    private final Session session = new Session();
    private final OAuth oauth = new OAuth();
    private final Jwt jwt = new Jwt();

    public Session getSession() {
        return session;
    }

    public OAuth getOauth() {
        return oauth;
    }

    public Jwt getJwt() {
        return jwt;
    }

    public static class Session {
        private long ttlMinutes = 15;

        public long getTtlMinutes() {
            return ttlMinutes;
        }

        public void setTtlMinutes(long ttlMinutes) {
            this.ttlMinutes = ttlMinutes;
        }
    }

    public static class OAuth {
        private Set<String> defaultRoles = new LinkedHashSet<>(Set.of("PRODUCER", "SUBSCRIBER"));

        public Set<String> getDefaultRoles() {
            return defaultRoles;
        }

        public void setDefaultRoles(Set<String> defaultRoles) {
            this.defaultRoles = defaultRoles;
        }
    }

    public static class Jwt {
        private String issuer = "user-management-service";
        private String secret = "change-this-jwt-secret-to-a-long-random-value-for-production-use-1234567890";

        public String getIssuer() {
            return issuer;
        }

        public void setIssuer(String issuer) {
            this.issuer = issuer;
        }

        public String getSecret() {
            return secret;
        }

        public void setSecret(String secret) {
            this.secret = secret;
        }
    }
}
