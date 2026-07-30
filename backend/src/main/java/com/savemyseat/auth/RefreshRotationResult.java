package com.savemyseat.auth;

import com.savemyseat.user.User;

public record RefreshRotationResult(String newRefreshToken, User user) {
}
