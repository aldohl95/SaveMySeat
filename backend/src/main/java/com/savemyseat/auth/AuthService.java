package com.savemyseat.auth;


import com.savemyseat.auth.dto.AuthResponse;
import com.savemyseat.auth.dto.LoginRequest;
import com.savemyseat.auth.dto.RegisterRequest;
import com.savemyseat.auth.exception.EmailAlreadyExistsException;
import com.savemyseat.auth.exception.InvalidCredentialsException;
import com.savemyseat.user.Role;
import com.savemyseat.user.User;
import com.savemyseat.user.UserRepository;
import com.savemyseat.user.dto.UserResponse;
import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Timer;
import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;



@Transactional(readOnly = true)
@RequiredArgsConstructor
@Service
public class AuthService {

    private final UserRepository userRepository;
    private final PasswordEncoder passwordEncoder;
    private final JwtService jwtService;
    private final RefreshTokenService refreshTokenService;
    private final MeterRegistry registry;
    private final CurrentUserProvider currentUserProvider;

    @Transactional
    public UserResponse createUser(RegisterRequest dto){
        Timer.Sample sample = Timer.start(registry);
        try {
            String normalizedEmail = dto.email().toLowerCase();
            //keeping dto.email despite lowercase enforcement so it shows the
            // email used doesn't work
            if (userRepository.existsByEmail(normalizedEmail)) {
                throw new EmailAlreadyExistsException("User with email: " + dto.email() + " already exists");
            }
            String encodedPassword = passwordEncoder.encode(dto.password());

            User user = new User(
                    dto.firstName(),
                    dto.lastName(),
                    normalizedEmail,
                    encodedPassword,
                    Role.ATTENDEE

            );
            User saved = userRepository.save(user);
            registry.counter("users.created").increment();
            return toResponse(saved);
        }finally {
            sample.stop(registry.timer("user.creation.time"));
        }
    }
    @Transactional
    public AuthResponse login(LoginRequest dto){
        String normalizedEmail = dto.email().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .filter(u -> passwordEncoder.matches(dto.password(), u.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid " +
                        "Credentials"));
        String token = jwtService.generateToken(user);
        TokenPair refreshPair =
                refreshTokenService.createRefreshToken(user);
        return new AuthResponse(
                token,
                refreshPair.plainText(),
                "Bearer",
                jwtService.getExpirationSeconds(),
                toResponse(user)
        );
    }
    @Transactional
    public AuthResponse refresh(String refreshTokenPlainText){
        RefreshRotationResult result =
                refreshTokenService.rotate(refreshTokenPlainText);
        return new AuthResponse(
                jwtService.generateToken(result.user()),
                result.newRefreshToken(),
                "Bearer",
                jwtService.getExpirationSeconds(),
                toResponse(result.user())
        );

    }

    public UserResponse getCurrentUser(){
        return toResponse(currentUserProvider.getCurrentUser());
    }

    private UserResponse toResponse(User user){
        return new UserResponse(
                user.getId(),
                user.getFirstName(),
                user.getLastName(),
                user.getEmail(),
                user.getRole(),
                user.getCreatedAt(),
                user.getUpdatedAt()
        );
    }

}
