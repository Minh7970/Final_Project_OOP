package com.elearning.service;

import com.elearning.model.User;
import com.elearning.repository.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.*;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Collection;
import java.util.List;

@Service
@RequiredArgsConstructor
public class CustomUserDetailsService implements UserDetailsService {

    private final UserRepository userRepository;

    @Override
    @Transactional(readOnly = true)
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        User user = userRepository.findByUsername(username)
                .orElseThrow(() -> new UsernameNotFoundException(
                        "Không tìm thấy tài khoản: " + username));

        if (!user.getEnabled()) {
            throw new UsernameNotFoundException("Tài khoản đã bị vô hiệu hóa: " + username);
        }

        return new org.springframework.security.core.userdetails.User(
                user.getUsername(),
                user.getPassword(),
                user.getEnabled(),
                true, true, true,
                buildAuthorities(user.getRole())
        );
    }

    private Collection<? extends GrantedAuthority> buildAuthorities(User.Role role) {
        return List.of(new SimpleGrantedAuthority("ROLE_" + role.name()));
    }
}
