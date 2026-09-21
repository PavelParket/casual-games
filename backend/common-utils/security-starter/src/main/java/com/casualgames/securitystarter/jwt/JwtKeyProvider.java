package com.casualgames.securitystarter.jwt;

import javax.crypto.SecretKey;

public interface JwtKeyProvider {

    SecretKey getSigningKey();
}
