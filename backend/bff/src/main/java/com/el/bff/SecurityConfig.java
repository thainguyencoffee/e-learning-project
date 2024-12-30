package com.el.bff;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpStatus;
import org.springframework.security.config.Customizer;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.oauth2.client.oidc.web.server.logout.OidcClientInitiatedServerLogoutSuccessHandler;
import org.springframework.security.oauth2.client.registration.ReactiveClientRegistrationRepository;
import org.springframework.security.oauth2.client.web.server.ServerOAuth2AuthorizedClientRepository;
import org.springframework.security.oauth2.client.web.server.WebSessionServerOAuth2AuthorizedClientRepository;
import org.springframework.security.web.server.SecurityWebFilterChain;
import org.springframework.security.web.server.authentication.HttpStatusServerEntryPoint;
import org.springframework.security.web.server.authentication.logout.ServerLogoutSuccessHandler;
import org.springframework.security.web.server.csrf.CookieServerCsrfTokenRepository;
import org.springframework.security.web.server.csrf.CsrfToken;
import org.springframework.security.web.server.csrf.XorServerCsrfTokenRequestAttributeHandler;
import org.springframework.web.server.WebFilter;
import reactor.core.publisher.Mono;

@Configuration
@EnableWebFluxSecurity
public class SecurityConfig {

    @Bean
    ServerOAuth2AuthorizedClientRepository authorizedClientRepository() {
        return new WebSessionServerOAuth2AuthorizedClientRepository();
    }

    @Bean
    SecurityWebFilterChain filterChain(ServerHttpSecurity http, ReactiveClientRegistrationRepository clientRegistrationRepository) {
        http.oauth2Login(Customizer.withDefaults());

        http.logout(logout -> logout.logoutSuccessHandler(oidcLogoutSuccessHandler(clientRegistrationRepository)));

        http.csrf(csrf -> {
            csrf.csrfTokenRepository(CookieServerCsrfTokenRepository.withHttpOnlyFalse());
            csrf.csrfTokenRequestHandler(new XorServerCsrfTokenRequestAttributeHandler()::handle);
        });

        http.exceptionHandling(exceptionHandling -> exceptionHandling
                .authenticationEntryPoint(new HttpStatusServerEntryPoint(HttpStatus.UNAUTHORIZED)));

        http.authorizeExchange(exchange -> {
            exchange.pathMatchers("/greeting").permitAll();
            exchange.pathMatchers("/", "/*.css", "/*.js", "/favicon.ico", "/assets/**").permitAll();
            exchange.pathMatchers("/actuator/**").permitAll();
            exchange.pathMatchers("/login-options").permitAll();
            exchange.anyExchange().authenticated();
        });

        return http.build();
    }

    private ServerLogoutSuccessHandler oidcLogoutSuccessHandler(ReactiveClientRegistrationRepository clientRegistrationRepository) {
        var oidcLogoutSuccessHandler = new OidcClientInitiatedServerLogoutSuccessHandler(clientRegistrationRepository);
        oidcLogoutSuccessHandler.setPostLogoutRedirectUri("{baseUrl}");
        return oidcLogoutSuccessHandler;
    }

//    @Bean
//    WebFilter csrfWebFilter() {
//        // Required because of https://github.com/spring-projects/spring-security/issues/5766
//        return (exchange, chain) -> {
//            exchange.getResponse().beforeCommit(() -> Mono.defer(() -> {
//                Mono<CsrfToken> csrfToken = exchange.getAttribute(CsrfToken.class.getName());
//                return csrfToken != null ? csrfToken.then() : Mono.empty();
//            }));
//            return chain.filter(exchange);
//        };
//    }

    @Bean
    WebFilter csrfCookieWebFilter() {
        return (exchange, chain) -> {
            exchange.getAttributeOrDefault(CsrfToken.class.getName(), Mono.empty()).subscribe();
            return chain.filter(exchange);
        };
    }

}
