package com.akhil.urlShortner.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

@Component
public class JwtFilter extends OncePerRequestFilter {

    private final JwtUtil jwtUtil;

    // Injecting the JwtUtil we created earlier
    public JwtFilter(JwtUtil jwtUtil) {
        this.jwtUtil = jwtUtil;
    }

    @Override
    protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain)
            throws ServletException, IOException {
        
        // 1. Get the Authorization header from the request
        final String authorizationHeader = request.getHeader("Authorization");

        String username = null;
        String jwt = null;

        // 2. Check if the header exists and starts with "Bearer "
        if (authorizationHeader != null && authorizationHeader.startsWith("Bearer ")) {
            jwt = authorizationHeader.substring(7);
            try {
                username = jwtUtil.extractUsername(jwt);
            } catch (Exception e) {
                // Token might be expired, malformed, or tampered with
                System.out.println("JWT Extraction Error: " + e.getMessage());
            }
        }

        // 3. If we found a username, but the security context is empty (not yet authenticated)
        if (username != null && SecurityContextHolder.getContext().getAuthentication() == null) {
            
            // 4. Validate the token
            if (jwtUtil.validateToken(jwt)) {
                
                String role = jwtUtil.extractRole(jwt);
                
                // 5. Create a UserDetails object that Spring Security understands
                UserDetails userDetails = User.builder()
                        .username(username)
                        .password("") // We don't need the password here, the token proves identity
                        .authorities(role) 
                        .build();

                // 6. Create the authentication token and put it in the Context
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities());
                
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                
                SecurityContextHolder.getContext().setAuthentication(authToken);
            }
        }
        
        // 7. Continue the filter chain (let the request proceed to the Controller)
        filterChain.doFilter(request, response);
    }
}