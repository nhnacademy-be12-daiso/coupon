package com.nhnacademy.coupon.config;

import com.nhnacademy.coupon.resolver.UserHeaderResolver;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    private final UserHeaderResolver userHeaderResolver;

    public WebConfig(UserHeaderResolver userHeaderResolver) {
        this.userHeaderResolver = userHeaderResolver;
    }

    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(userHeaderResolver);
    }
}
