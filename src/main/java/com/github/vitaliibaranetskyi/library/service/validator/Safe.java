package com.github.vitaliibaranetskyi.library.service.validator;

import com.github.vitaliibaranetskyi.library.exception.ServiceException;

/**
 * Wrapper used to take info from HTTP request/session/context safely
 * @param <K> request/session/context
 */
public abstract class Safe<K> {
    protected K value;
    protected String param;
    public abstract Safe<K> get(String s);

    /**
     * For string conversion you must use notEmpty first
     * @param converter converts requested parameter from type K to type V
     * @param <V> type of expected result
     * @return result of type V
     * @throws ServiceException in case of inconsistency of requested parameter
     */
    public <V> V convert(TypeConverter<K, V> converter) throws ServiceException {
        try {
            return converter.process(value);
        } catch (IllegalArgumentException e) {
            throw new ServiceException("error.parameter.wrong.type", param);
        }
    }

    public Safe<K> notNull() throws ServiceException {
        if (value == null) {
            throw new ServiceException("error.parameter.is.empty", param);
        }
        return this;
    }

    protected void setParam(String param) {
        this.param = param;
    }
}