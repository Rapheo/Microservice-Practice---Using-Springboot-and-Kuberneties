package com.MicroservicePractice.OrderService.external.intercept;

import com.MicroservicePractice.OrderService.service.TokenService;
import feign.RequestInterceptor;
import feign.RequestTemplate;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

@Configuration
public class OAuthRequestInterceptor implements RequestInterceptor {

    private final TokenService tokenService;

    public OAuthRequestInterceptor(TokenService tokenService) {
        this.tokenService = tokenService;
    }

    @Override
    public void apply(RequestTemplate requestTemplate) {
        String token = tokenService.extractToken();

        if(token != null){
            requestTemplate.header("Authorization", "Bearer " + token);
        }
    }
}
