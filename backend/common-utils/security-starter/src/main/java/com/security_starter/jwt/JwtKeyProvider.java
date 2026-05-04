package com.security_starter.jwt;

import javax.crypto.SecretKey;

public interface JwtKeyProvider {

    SecretKey getSigningKey();
}
