package com.github.vitaliibaranetskyi.library.dao.impl.mysql.util;

import org.junit.Assert;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class TestDisjoint {
    @Test
    public void testBothSetsShouldBeEmpty() {
        final List<Integer> old = Arrays.asList(1, 2, 3);
        final List<Integer> newList = Arrays.asList(1, 2, 3);
        Disjoint<Integer> d = new Disjoint<>(old, newList);
        Assert.assertEquals(new ArrayList<Integer>(), d.getToAdd());
        Assert.assertEquals(new ArrayList<Integer>(), d.getToDelete());
    }

    @Test
    public void testToDeleteSet() {
        final List<Integer> old = Arrays.asList(1, 2, 3);
        final List<Integer> newList = Arrays.asList(1);
        final List<Integer> expected = Arrays.asList(2, 3);

        Disjoint<Integer> d = new Disjoint<>(old, newList);
        Assert.assertEquals(expected, d.getToDelete());
    }

    @Test
    public void testToAddSet() {
        final List<Integer> old = Arrays.asList(1);
        final List<Integer> newList = Arrays.asList(1, 2, 3);
        final List<Integer> expected = Arrays.asList(2, 3);

        Disjoint<Integer> d = new Disjoint<>(old, newList);
        Assert.assertEquals(expected, d.getToAdd());
    }

    @Test
    public void testBothNotNull() {
        final List<Integer> old = Arrays.asList(1, 3);
        final List<Integer> newList = Arrays.asList(1, 2);

        Disjoint<Integer> d = new Disjoint<>(old, newList);
        Assert.assertEquals(Arrays.asList(3), d.getToDelete());
        Assert.assertEquals(Arrays.asList(2), d.getToAdd());
    }
}
