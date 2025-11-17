package com.MicroservicePractice.OrderService.security;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.oauth2.server.resource.authentication.JwtAuthenticationConverter;
import org.springframework.security.oauth2.server.resource.authentication.JwtGrantedAuthoritiesConverter;
import org.springframework.security.web.SecurityFilterChain;

import java.util.Collection;
import java.util.stream.Collectors;

@Configuration
@EnableWebSecurity
@EnableMethodSecurity
public class WebSecurityConfig {

    @Bean
    public SecurityFilterChain SecurityFilterChain(HttpSecurity http) throws Exception {
        http
                .csrf(csrf -> csrf.disable())
                .authorizeHttpRequests(authorizeRequests -> authorizeRequests
                        .anyRequest().authenticated())
                .oauth2ResourceServer(
                        oAuth2 -> oAuth2.jwt(
                                jwt -> jwt.jwtAuthenticationConverter(converter())
                        ));

        return http.build();
    }

    public JwtAuthenticationConverter converter(){
        JwtAuthenticationConverter converter
                = new JwtAuthenticationConverter();

        converter.setJwtGrantedAuthoritiesConverter(jwt ->{
            JwtGrantedAuthoritiesConverter defaultConverter
                    = new JwtGrantedAuthoritiesConverter();
            Collection<GrantedAuthority> authorities
                    = defaultConverter.convert(jwt);

            Collection<GrantedAuthority> customAuthorities
                    = jwt.getClaimAsStringList("https://microservicePractice.com/roles")
                        .stream()
                    .map(role -> new SimpleGrantedAuthority("ROLE_" + role))
                    .collect(Collectors.toList());

            authorities.addAll(customAuthorities);
            return authorities;
        });

        return converter;
    }
}
