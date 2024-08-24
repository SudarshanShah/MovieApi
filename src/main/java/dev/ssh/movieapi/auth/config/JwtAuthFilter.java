package dev.ssh.movieapi.auth.config;

import dev.ssh.movieapi.auth.services.JwtService;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.lang.NonNull;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;

// This class applies filter user requests to authenticate user
// before it can access secure endpoints
@Component
@RequiredArgsConstructor
public class JwtAuthFilter extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final UserDetailsService userDetailsService;

    @Override
    protected void doFilterInternal(@NonNull HttpServletRequest request,
                                    @NonNull HttpServletResponse response,
                                    @NonNull FilterChain filterChain)
            throws ServletException, IOException {

        // we need value of Authorization Header
        // It contains JWT
        final String authHeader = request.getHeader("Authorization");
        final String jwt;
        final String userEmail;

        // condition to check if token is NULL and
        // does not start with keyword "Bearer "
        // pass to next filter, and return
        System.out.println("Auth header = " + authHeader);
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            filterChain.doFilter(request, response);
            return;
        }

        // extract JWT
        jwt = authHeader.substring(7);
        // extract username from JWT
        userEmail = jwtService.extractUsername(jwt);
        System.out.println("userEmail = " + userEmail);
        /*
         * if username is not null and user not authenticated
         * get UserDetails from username
         * check if token is valid
         * set authToken using UserDetails object
         * update security context holder to make user authenticated
         * finally, pass on to the next filter
         */
        if (userEmail != null && SecurityContextHolder.getContext().getAuthentication() == null) {

            UserDetails userDetails = this.userDetailsService.loadUserByUsername(userEmail);
            if (jwtService.isTokenValid(jwt, userDetails)) {
                UsernamePasswordAuthenticationToken authToken = new UsernamePasswordAuthenticationToken(
                        userDetails,
                        null,
                        userDetails.getAuthorities()
                );

                authToken.setDetails(
                        new WebAuthenticationDetailsSource().buildDetails(request)
                );

                SecurityContextHolder.getContext().setAuthentication(authToken);
                System.out.println("Filter completed!");
            }
        }

        filterChain.doFilter(request, response);

    }
}
