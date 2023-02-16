package com.github.vitaliibaranetskyi.library.dao.factory.impl;

import com.github.vitaliibaranetskyi.library.dao.factory.AbstractDaoFactory;
import com.github.vitaliibaranetskyi.library.dao.factory.DaoFactoryImpl;
import com.github.vitaliibaranetskyi.library.dao.factory.impl.db.MySQLDaoFactory;

/**
 * Abstract factory of DB factories
 */
public class DBDaoFactory implements AbstractDaoFactory {
    public DaoFactoryImpl newInstance() {
        return new MySQLDaoFactory();
    }
}
