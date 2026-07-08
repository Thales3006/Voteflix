package com.thales.server.service;

import com.auth0.jwt.JWT;
import com.auth0.jwt.JWTVerifier;
import com.auth0.jwt.algorithms.Algorithm;
import com.auth0.jwt.exceptions.JWTVerificationException;
import com.auth0.jwt.interfaces.DecodedJWT;
import com.thales.common.model.ErrorStatus;
import com.thales.common.model.StatusException;

public class JwtService {

    private final Algorithm algorithm;
    private final JWTVerifier verifier;

    public JwtService(String secretKey) {
        this.algorithm = Algorithm.HMAC256(secretKey);
        this.verifier = JWT.require(algorithm).build();
    }

    public String generateToken(int id, String username) {
        return JWT.create()
            .withSubject(username)
            .withClaim("id", id)
            .withClaim("username", username)
            .sign(algorithm);
    }

    public int verifyAndGetUserId(String token) {
        try {
            DecodedJWT jwt = verifier.verify(token);
            return jwt.getClaim("id").asInt();
        } catch (JWTVerificationException e) {
            throw new StatusException(ErrorStatus.UNAUTHORIZED, "Invalid or expired token");
        }
    }
}
