package com.github.vitaliibaranetskyi.library.entity;

import java.io.Serializable;

/**
 * Common parent class to all entities
 */
public abstract class Entity implements Serializable {
    protected long id;

    protected Entity(long id) {
        this.id = id;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
