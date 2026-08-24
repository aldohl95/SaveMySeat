package com.savemyseat.auth;

import com.savemyseat.auth.dto.AuthResponse;
import com.savemyseat.auth.dto.LoginRequest;
import com.savemyseat.auth.dto.RefreshRequest;
import com.savemyseat.auth.dto.RegisterRequest;
import com.savemyseat.user.dto.UserResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequiredArgsConstructor
@RequestMapping("/api/auth")
@Tag(name = "Authentication", description = "Authentication management " +
        "endpoints")
public class AuthController {

    private final AuthService authService;

    @Operation(summary = "Creates a User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "User created " +
                    "successfully"),
            @ApiResponse(responseCode = "409", description = "Email already " +
                    "exists")
    })
    @PostMapping("/register")
    public ResponseEntity<UserResponse> createUser(@Valid @RequestBody RegisterRequest dto){
        UserResponse created = authService.createUser(dto);
        return ResponseEntity.status(HttpStatus.CREATED).body(created);
    }

    @Operation(summary = "Logs in a User")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "user logged in " +
                    "successfully"),
            @ApiResponse(responseCode = "401", description = "Invalid " +
                    "Credentials")
    })
    @PostMapping("/login")
    public ResponseEntity<AuthResponse> login(@Valid @RequestBody LoginRequest dto){
        return ResponseEntity.ok(authService.login(dto));
    }

    @Operation(summary = "Refreshes a Token")
    @ApiResponses({
            @ApiResponse(responseCode = "201", description = "Token refreshed" +
                    " successfully"),
            @ApiResponse(responseCode = "401", description = "Token not found"),
            @ApiResponse(responseCode = "401", description = "Token Expired"),
            @ApiResponse(responseCode = "401", description = "Invalid Token")
    })
    @PostMapping("/refresh")
    public ResponseEntity<AuthResponse> refresh(@Valid @RequestBody RefreshRequest dto) {
        return ResponseEntity.ok(authService.refresh(dto.refreshToken()));
    }

    @GetMapping("/me")
    public ResponseEntity<UserResponse> getCurrentUser(){
        return ResponseEntity.ok(authService.getCurrentUser());
    }

}
