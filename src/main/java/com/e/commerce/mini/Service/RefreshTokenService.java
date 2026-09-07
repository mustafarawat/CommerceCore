package com.e.commerce.mini.Service;

import com.e.commerce.mini.models.RefreshToken;
import com.e.commerce.mini.models.User;

public interface RefreshTokenService {

    RefreshToken createRefreshToken(User user);

    RefreshToken verifyRefreshToken(String token);

    void revokeRefreshToken(String token);

    void revokeAllUserTokens(User user);
}