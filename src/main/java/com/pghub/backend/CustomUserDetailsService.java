package com.pghub.backend;
import org.springframework.security.core.userdetails.User;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;


@Service
public class CustomUserDetailsService implements UserDetailsService {

    private final OwnerRepository ownerRepository;

    public CustomUserDetailsService(OwnerRepository ownerRepository) {
        this.ownerRepository = ownerRepository;
    }

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        Owner owner = ownerRepository.findByUsername(username)
            .orElseThrow(() -> new UsernameNotFoundException("Owner not found: " + username));

        return User.builder()
            .username(owner.getUsername())
            .password(owner.getPassword()) // Database password must be BCrypt encrypted
            .roles(owner.getRole())
            .build();
    }
}
