package com.akhil.urlShortner.redis;

import java.util.concurrent.TimeUnit;

import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.stereotype.Component;
import org.springframework.web.servlet.HandlerInterceptor;

import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;

@Component
public class RateLimiter implements HandlerInterceptor {
    private final StringRedisTemplate redisTemplate;
    private final int MAX_REQUESTS = 5; 
    private final int TIME_WINDOW = 60; 

    public RateLimiter(StringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String clientIp = request.getRemoteAddr(); 
        String key = "rate_limit:" + clientIp; 

        // 1. Atomically add 1 to the counter
        Long count = redisTemplate.opsForValue().increment(key); 

        // 2. If it's the first request, start the 60s clock
        if (count != null && count == 1) {
            redisTemplate.expire(key, TIME_WINDOW, TimeUnit.SECONDS); // 
        }

        // 3. Block if they exceed the limit
        if (count != null && count > MAX_REQUESTS) { // 
            response.setStatus(429); // [cite: 333]
            response.getWriter().write("Too many requests. Try again later.");
            return false; // BLOCK 
        }
        return true; // ALLOW 
    }
}
