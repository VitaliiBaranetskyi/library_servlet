package com.github.vitaliibaranetskyi.library.dao.factory;

import org.junit.Assert;
import org.junit.Test;

public class DaoFactoryCreatorTest {
    @Test
    public void testGetDefaultFactory() {
        AbstractDaoFactory factory = DaoFactoryCreator.getDefaultFactory();
        Assert.assertNotNull(factory);
    }
}