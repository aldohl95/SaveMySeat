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

    @Transactional
    public UserResponse createUser(RegisterRequest dto){
        String normalizedEmail = dto.email().toLowerCase();
        //keeping dto.email despite lowercase enforcement so it shows the
        // email used doesn't work
        if(userRepository.existsByEmail(normalizedEmail)){
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
        return toResponse(userRepository.save(user));
    }

    public AuthResponse login(LoginRequest dto){
        String normalizedEmail = dto.email().toLowerCase();

        User user = userRepository.findByEmail(normalizedEmail)
                .filter(u -> passwordEncoder.matches(dto.password(), u.getPasswordHash()))
                .orElseThrow(() -> new InvalidCredentialsException("Invalid " +
                        "Credentials"));
        String token = jwtService.generateToken(user);

        return new AuthResponse(
                token,
                "Bearer",
                jwtService.getExpirationSeconds(),
                toResponse(user)
        );


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
