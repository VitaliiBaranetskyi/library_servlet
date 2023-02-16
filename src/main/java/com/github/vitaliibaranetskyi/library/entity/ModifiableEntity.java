package com.github.vitaliibaranetskyi.library.entity;

import java.text.SimpleDateFormat;
import java.util.Calendar;

/**
 * Parent for all modifiable entities
 */
public class ModifiableEntity extends Entity {
    protected Calendar modified;

    protected ModifiableEntity(long id, Calendar modified) {
        super(id);
        this.modified = modified;
    }

    public Calendar getModified() {
        return modified;
    }

    public void setModified(Calendar modified) {
        this.modified = modified;
    }
    public static String format(Calendar modified) {
        return modified == null ? "null" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(modified.getTime());
    }
}
