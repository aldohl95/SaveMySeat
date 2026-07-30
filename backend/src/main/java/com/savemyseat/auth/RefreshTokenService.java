package com.savemyseat.auth;

import com.savemyseat.auth.exception.RefreshTokenExpiredException;
import com.savemyseat.auth.exception.RefreshTokenNotFoundException;
import com.savemyseat.auth.exception.RefreshTokenReusedException;
import com.savemyseat.user.User;
import lombok.RequiredArgsConstructor;
import org.antlr.v4.runtime.Token;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.time.OffsetDateTime;
import java.time.ZoneOffset;
import java.util.Base64;

@Service
@Transactional
@RequiredArgsConstructor
public class RefreshTokenService {

    private final RefreshTokenRepository refreshTokenRepository;

    @Transactional
    public TokenPair createRefreshToken(User user){

        String token = generateOpaqueToken();
        String tokenHash = hashToken(token);

        RefreshToken refreshToken = new RefreshToken(
                user,
                tokenHash,
                OffsetDateTime.now(ZoneOffset.UTC).plusDays(7)
        );

        refreshTokenRepository.save(refreshToken);

        return new TokenPair(token, refreshToken);

    }

    @Transactional
    public RefreshRotationResult rotate(String newRefreshToken){
        String hashedToken = hashToken(newRefreshToken);
        RefreshToken token =
                refreshTokenRepository.findByTokenHash(hashedToken).orElseThrow(() -> new RefreshTokenNotFoundException("Token Not Found"));
        if(OffsetDateTime.now().isAfter(token.getExpiresAt())){
            throw new RefreshTokenExpiredException("Token Expired");
        }

        if(token.getUsedAt() != null){
            throw new RefreshTokenReusedException("Invalid Token");
        }
        token.setUsedAt(OffsetDateTime.now());
        TokenPair newPair = createRefreshToken(token.getUser());
        token.setReplacedBy(newPair.entity());
        refreshTokenRepository.save(token);

        return new RefreshRotationResult(newPair.plainText(),token.getUser());
    }

    private String generateOpaqueToken(){
        SecureRandom secureRandom = new SecureRandom();
        byte[] bytes = new byte[32];
        secureRandom.nextBytes(bytes);

        String plainTextToken = Base64.getUrlEncoder().withoutPadding().encodeToString(bytes);

        return plainTextToken;
    }

    private String hashToken(String token){
        try{
            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] hashBytes =
                    digest.digest(token.getBytes(StandardCharsets.UTF_8));
            return Base64.getUrlEncoder().withoutPadding().encodeToString(hashBytes);
        }catch(NoSuchAlgorithmException e){
            throw new IllegalStateException("SHA-256 not available", e);
        }
    }



}
