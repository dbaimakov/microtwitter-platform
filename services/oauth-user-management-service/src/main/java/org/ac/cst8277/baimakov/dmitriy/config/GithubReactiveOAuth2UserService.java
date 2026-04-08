package org.ac.cst8277.baimakov.dmitriy.config;

import org.springframework.http.HttpHeaders;
import org.springframework.security.oauth2.client.userinfo.DefaultReactiveOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.ReactiveOAuth2UserService;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Mono;

import java.util.Comparator;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Component
public class GithubReactiveOAuth2UserService implements ReactiveOAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final DefaultReactiveOAuth2UserService delegate = new DefaultReactiveOAuth2UserService();
    private final WebClient webClient = WebClient.builder()
            .baseUrl("https://api.github.com")
            .defaultHeader(HttpHeaders.ACCEPT, "application/vnd.github+json")
            .build();

    @Override
    public Mono<OAuth2User> loadUser(OAuth2UserRequest userRequest) {
        return delegate.loadUser(userRequest)
                .flatMap(oAuth2User -> {
                    if (!"github".equalsIgnoreCase(userRequest.getClientRegistration().getRegistrationId())) {
                        return Mono.just(oAuth2User);
                    }

                    Map<String, Object> attributes = new LinkedHashMap<>(oAuth2User.getAttributes());
                    Object email = attributes.get("email");
                    if (email instanceof String value && StringUtils.hasText(value)) {
                        return Mono.just(rebuildUser(userRequest, oAuth2User, attributes));
                    }

                    return webClient.get()
                            .uri("/user/emails")
                            .headers(headers -> headers.setBearerAuth(userRequest.getAccessToken().getTokenValue()))
                            .retrieve()
                            .bodyToFlux(GitHubEmailResponse.class)
                            .collectList()
                            .map(this::extractBestEmail)
                            .defaultIfEmpty("")
                            .map(bestEmail -> {
                                if (StringUtils.hasText(bestEmail)) {
                                    attributes.put("email", bestEmail);
                                }
                                return rebuildUser(userRequest, oAuth2User, attributes);
                            });
                });
    }

    private OAuth2User rebuildUser(OAuth2UserRequest userRequest, OAuth2User originalUser, Map<String, Object> attributes) {
        String nameAttributeKey = userRequest.getClientRegistration()
                .getProviderDetails()
                .getUserInfoEndpoint()
                .getUserNameAttributeName();

        return new DefaultOAuth2User(originalUser.getAuthorities(), attributes, nameAttributeKey);
    }

    private String extractBestEmail(List<GitHubEmailResponse> emails) {
        return emails.stream()
                .filter(GitHubEmailResponse::verified)
                .sorted(Comparator.comparing(GitHubEmailResponse::primary).reversed())
                .map(GitHubEmailResponse::email)
                .filter(StringUtils::hasText)
                .findFirst()
                .orElse("");
    }

    private record GitHubEmailResponse(String email, boolean primary, boolean verified, String visibility) {
    }
}
