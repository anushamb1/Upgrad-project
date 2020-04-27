package com.upgrad.quora.service.business;

import com.upgrad.quora.service.entity.UserAuthTokenEntity;
import com.upgrad.quora.service.exception.AuthorizationFailedException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;


@Service
public class AuthTokenService {
    @Autowired
    private AuthenticationService authenticationService;

    public UserAuthTokenEntity getUserAuthTokenEntity(String authorization) throws AuthorizationFailedException {
        String[] bearerToken = authorization.split("Bearer ");
        if (bearerToken.length < 2) {
            throw new AuthorizationFailedException("ATHR-001", "User has not signed in");
        }

        return authenticationService.authenticateByAccessToken(bearerToken[1]);
    }
}
