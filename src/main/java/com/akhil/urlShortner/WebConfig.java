package com.akhil.urlShortner;

import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import com.akhil.urlShortner.redis.RateLimiter;

@Configuration
public class WebConfig implements WebMvcConfigurer {
    private final RateLimiter interceptor;

    public WebConfig(RateLimiter interceptor) {
        this.interceptor = interceptor;
    }

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // Only guard the URL shortening API
        registry.addInterceptor(interceptor).addPathPatterns("/api/urls/**","/**"); 
    }
}
