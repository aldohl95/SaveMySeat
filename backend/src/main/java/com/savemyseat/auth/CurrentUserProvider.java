package com.savemyseat.auth;

import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CurrentUserProvider {

    private final UserRepository userRepository;

    public User getCurrentUser() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            throw new IllegalStateException("No authenticated user in security context");
        }

        String userId = auth.getPrincipal().toString();
        Long id = Long.parseLong(userId);

        return userRepository.findById(id)
                .orElseThrow(() -> new IllegalStateException(
                        "Authenticated user " + id + " not found in database"));
    }
}