package com.kneelawk.animeservlet.security;

import org.springframework.context.annotation.Bean;
import org.springframework.security.config.annotation.web.reactive.EnableWebFluxSecurity;
import org.springframework.security.config.web.server.ServerHttpSecurity;
import org.springframework.security.core.userdetails.MapReactiveUserDetailsService;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.server.SecurityWebFilterChain;

/**
 * Created by Kneelawk on 2/29/20.
 */
@EnableWebFluxSecurity
public class SecurityConfig {
    @Bean
    public SecurityWebFilterChain securityWebFilterChain(ServerHttpSecurity httpSecurity) {
        httpSecurity.authorizeExchange().pathMatchers("/upload", "/manage").authenticated();
        httpSecurity.formLogin().loginPage("/login");
        httpSecurity.logout().logoutUrl("/logout");
        httpSecurity.authorizeExchange().anyExchange().permitAll();

        return httpSecurity.build();
    }

    @Bean
    public MapReactiveUserDetailsService userDetailsService() {
        UserDetails user =
                User.withDefaultPasswordEncoder().username("user").password("password").roles("USER").build();

        return new MapReactiveUserDetailsService(user);
    }
}
