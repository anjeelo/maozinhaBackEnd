package br.com.maozinha.security;

import br.com.maozinha.user.User;
import br.com.maozinha.user.UserRepository;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Objects;

@Service
public class AuthenticatedUserDetailsService
        implements UserDetailsService {

    private final UserRepository userRepository;

    public AuthenticatedUserDetailsService(
            UserRepository userRepository
    ) {
        this.userRepository = Objects.requireNonNull(
                userRepository,
                "userRepository não pode ser nulo"
        );
    }

    @Override
    public UserDetails loadUserByUsername(
            String email
    ) throws UsernameNotFoundException {

        User user = userRepository
                .findByEmailIgnoreCase(email)
                .orElseThrow(() ->
                        new UsernameNotFoundException(
                                "Usuário não encontrado."
                        )
                );

        return new AuthenticatedUser(user);
    }
}