package com.example.timesheet.security;

import com.example.timesheet.service.UserService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.springframework.lang.NonNull;

import java.io.IOException;

@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
    private final JwtService jwtService;
    private final UserService userService;

    @Override
    protected void doFilterInternal(
            @NonNull HttpServletRequest request,
            @NonNull HttpServletResponse response,
            @NonNull FilterChain filterChain
    ) throws ServletException, IOException {
        final String authHeader = request.getHeader("Authorization");
    
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            System.out.println(" No valid Authorization header found.");
            filterChain.doFilter(request, response);
            return;
        }
    
        final String jwt = authHeader.substring(7);
        System.out.println("🔹 Extracted JWT: " + jwt);
    
        final String userEmail = jwtService.extractUsername(jwt);
        if (userEmail == null) {
            System.out.println(" Invalid JWT: Could not extract username.");
            filterChain.doFilter(request, response);
            return;
        }
    
        System.out.println("🔹 Extracted Email from JWT: " + userEmail);
        if (SecurityContextHolder.getContext().getAuthentication() == null) {
            UserDetails userDetails = userService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                System.out.println(" JWT is valid. Setting security context.");
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails, null, userDetails.getAuthorities()
                );
                authToken.setDetails(new WebAuthenticationDetailsSource().buildDetails(request));
                SecurityContextHolder.getContext().setAuthentication(authToken);
            } else {
                System.out.println(" JWT is invalid!");
            }
        }
    
        filterChain.doFilter(request, response);
    }
    
}