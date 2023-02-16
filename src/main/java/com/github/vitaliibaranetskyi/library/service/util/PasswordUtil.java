package com.github.vitaliibaranetskyi.library.service.util;

import javax.crypto.SecretKeyFactory;
import javax.crypto.spec.PBEKeySpec;
import java.security.NoSuchAlgorithmException;
import java.security.SecureRandom;
import java.security.spec.InvalidKeySpecException;

/**
 * Use pepper and salt to generate hash of passwords to be stored in DB
 */
public class PasswordUtil {
    private static final String PEPPER = "ys2hkBB-cswA2H@kxMXc";
    private static final String SALT_ALGORITHM = "SHA1PRNG";
    private static final String HASH_ALGORITHM = "PBKDF2WithHmacSHA1";
    private static final int SALT_LENGTH = 20;
    private static final int HASH_ITERATION = 1000;
    private static final int HASH_LENGTH = 512;

    private PasswordUtil() {}

    public static String genHash(String pass, String salt) throws InvalidKeySpecException, NoSuchAlgorithmException {
        char[] chars = (pass + PEPPER).toCharArray();
        PBEKeySpec spec = new PBEKeySpec(chars, salt.getBytes(), HASH_ITERATION, HASH_LENGTH);
        SecretKeyFactory skf = SecretKeyFactory.getInstance(HASH_ALGORITHM);
        byte[] hash = skf.generateSecret(spec).getEncoded();
        return new String(hash);
    }

    public static String genSalt() throws NoSuchAlgorithmException {
        SecureRandom sr = SecureRandom.getInstance(SALT_ALGORITHM);
        byte[] salt = new byte[SALT_LENGTH];
        sr.nextBytes(salt);
        return new String(salt);
    }
}
