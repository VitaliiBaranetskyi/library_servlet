package com.github.vitaliibaranetskyi.library.jstltag;

import com.github.vitaliibaranetskyi.library.entity.Entity;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import javax.servlet.jsp.tagext.TagSupport;
import java.util.List;

/**
 * Finds entity in provided list by entity's id
 */
public class GetEntityByID extends TagSupport {
    private static final Logger logger = LogManager.getLogger(GetEntityByID.class);
    private long lookUpID;
    private String var;
    private List<Entity> value;

    public void setLookUpID(long lookUpID) {
        this.lookUpID = lookUpID;
    }

    public void setVar(String var) {
        this.var = var;
    }

    public void setValue(List<Entity> value) {
        this.value = value;
    }

    @Override
    public int doStartTag() {
        logger.trace("init: var={}, lookUpID={}, value={}", var, lookUpID, value);

        for (Entity e: value) {
            if (e.getId() == lookUpID) {
                logger.debug("Entity was found");
                logger.trace("e={}", e);
                pageContext.setAttribute(var, e);
                return SKIP_BODY;
            }
        }

        return SKIP_BODY;
    }
}
