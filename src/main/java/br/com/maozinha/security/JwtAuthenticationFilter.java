package br.com.maozinha.security;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.web.authentication.WebAuthenticationDetailsSource;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;

import java.io.IOException;
import java.util.Objects;

@Component
public class JwtAuthenticationFilter
        extends OncePerRequestFilter {

    private final JwtService jwtService;
    private final AuthenticatedUserDetailsService userDetailsService;

    public JwtAuthenticationFilter(
            JwtService jwtService,
            AuthenticatedUserDetailsService userDetailsService
    ) {

        this.jwtService = Objects.requireNonNull(
                jwtService,
                "jwtService não pode ser nulo"
        );

        this.userDetailsService = Objects.requireNonNull(
                userDetailsService,
                "userDetailsService não pode ser nulo"
        );
    }

    @Override
    protected void doFilterInternal(
            HttpServletRequest request,
            HttpServletResponse response,
            FilterChain filterChain
    ) throws ServletException, IOException {

        String authorizationHeader =
                request.getHeader(
                        "Authorization"
                );

        if (authorizationHeader == null
                || !authorizationHeader.startsWith(
                "Bearer "
        )) {

            filterChain.doFilter(
                    request,
                    response
            );

            return;
        }

        String token =
                authorizationHeader.substring(
                        7
                );

        try {

            String email =
                    jwtService.extractUsername(
                            token
                    );

            if (email != null
                    && SecurityContextHolder
                    .getContext()
                    .getAuthentication()
                    == null) {

                UserDetails userDetails =
                        userDetailsService
                                .loadUserByUsername(
                                        email
                                );

                if (jwtService.isTokenValid(
                        token
                )) {

                    UsernamePasswordAuthenticationToken authentication =
                            new UsernamePasswordAuthenticationToken(
                                    userDetails,
                                    null,
                                    userDetails.getAuthorities()
                            );

                    authentication.setDetails(
                            new WebAuthenticationDetailsSource()
                                    .buildDetails(
                                            request
                                    )
                    );

                    SecurityContextHolder
                            .getContext()
                            .setAuthentication(
                                    authentication
                            );
                }
            }

        } catch (Exception exception) {

            SecurityContextHolder.clearContext();
        }

        filterChain.doFilter(
                request,
                response
        );
    }
}