package com.github.vitaliibaranetskyi.library.entity.impl;

import org.junit.Assert;
import org.junit.Test;

public class TestUser {
    @Test
    public void testBuilderBuildDefaultUserRole() {
        User.Builder builder = new User.Builder();
        User user = builder.build();
        Assert.assertEquals(user.getRole(), User.Role.USER);
    }

    @Test
    public void testBuilderBuildDefaultUserState() {
        User.Builder builder = new User.Builder();
        User user = builder.build();
        Assert.assertEquals(user.getState(), User.State.VALID);
    }

    @Test
    public void testBuilderBuildDefaultUserID() {
        User.Builder builder = new User.Builder();
        User user = builder.build();
        Assert.assertEquals(user.getId(), -1);
    }

    @Test
    public void testBuilderBuildCustomUserState() {
        User.Builder builder = new User.Builder().setState(User.State.UNKNOWN);
        User user = builder.build();
        Assert.assertEquals(user.getState(), User.State.UNKNOWN);
    }

    @Test
    public void testBuilderBuildCustomUserRole() {
        User.Builder builder = new User.Builder().setRole(User.Role.UNKNOWN);
        User user = builder.build();
        Assert.assertEquals(user.getRole(), User.Role.UNKNOWN);
    }

    @Test
    public void testBuilderBuildUserRoleOfString() {
        User.Builder builder = new User.Builder().setRole("ADMIN");
        User user = builder.build();
        Assert.assertEquals(user.getRole(), User.Role.ADMIN);
    }

    @Test
    public void testBuilderBuildUserRoleOfNullString() {
        String role = null;
        User.Builder builder = new User.Builder().setRole(role);
        User user = builder.build();
        Assert.assertEquals(user.getRole(), User.Role.USER);
    }

    @Test(expected = IllegalArgumentException.class)
    public void testBuilderBuildUserRoleOfStringNonARoleName() {
        String role = "kasdfk";
        User.Builder builder = new User.Builder().setRole(role);
        User user = builder.build();
        Assert.assertEquals(user.getRole(), User.Role.UNKNOWN);
    }
}
