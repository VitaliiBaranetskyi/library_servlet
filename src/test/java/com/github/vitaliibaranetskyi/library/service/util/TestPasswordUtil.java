package com.github.vitaliibaranetskyi.library.service.util;

import org.junit.Assert;
import org.junit.Test;

import java.security.NoSuchAlgorithmException;
import java.security.spec.InvalidKeySpecException;

public class TestPasswordUtil {
    @Test
    public void testGenHashIsTheSameOnSamePassAndSalt() throws InvalidKeySpecException, NoSuchAlgorithmException {
        final String pass = "123";
        final String salt = "123";
        String hash1 = PasswordUtil.genHash(pass, salt);
        String hash2 = PasswordUtil.genHash(pass, salt);
        Assert.assertEquals(hash1, hash2);
    }

    @Test
    public void testGenHashIsNotTheSameOnDifferentPassAndSameSalt() throws InvalidKeySpecException,
            NoSuchAlgorithmException {
        final String salt = "123";
        final String hash1 = PasswordUtil.genHash("123", salt);
        final String hash2 = PasswordUtil.genHash("321", salt);
        Assert.assertNotEquals(hash1, hash2);
    }

    @Test
    public void testGenSaltProduceRandomSalt() throws NoSuchAlgorithmException {
        Assert.assertNotEquals(PasswordUtil.genSalt(), PasswordUtil.genSalt());
    }
}
