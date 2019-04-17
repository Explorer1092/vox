package com.voxlearning.utopia.business.api.context;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;


public class AbstractBusinessContext<T extends AbstractBusinessContext> implements Serializable {
    private static final long serialVersionUID = -3693981677351947210L;

    @Getter @Setter private boolean successful = true;
    @Getter @Setter private boolean duplicated;
    @Getter @Setter private String message = "";
    @Getter @Setter private String errorCode = "";
    // for those who want to terminate the processes, but keep successful true
    @Getter @Setter private boolean terminateTask = false;
    @Getter @Setter private String templateClassName;
    @Getter private final Map<String, String> additions = new LinkedHashMap<>();

    public T errorResponse() {
        return errorResponse("");
    }

    @SuppressWarnings("unchecked")
    public T errorResponse(String message, Object... parameters) {
        this.successful = false;
        this.message = StringUtils.formatMessage(message, parameters);
        if (this.message == null)
            this.message = "";
        return (T) this;
    }

    public T successResponse() {
        return successResponse("");
    }

    @SuppressWarnings("unchecked")
    public T successResponse(String message, Object... parameters) {
        this.successful = true;
        this.message = StringUtils.formatMessage(message, parameters);
        if (this.message == null)
            this.message = "";
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T withDuplicatedException() {
        this.duplicated = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T terminateTask() {
        this.terminateTask = true;
        return (T) this;
    }

    @SuppressWarnings("unchecked")
    public T clearAdditions() {
        this.additions.clear();
        return (T) this;
    }

    public synchronized String putIfAbsent(String key, String value) {
        if (additions.containsKey(key)) return additions.get(key);
        additions.put(key, value);
        return value;
    }

    public MapMessage transform() {
        MapMessage message = new MapMessage();
        message.setSuccess(successful);
        if (this.duplicated) {
            message.withDuplicatedException();
        }
        if (StringUtils.isNotEmpty(this.message)) {
            message.setInfo(this.message);
        }
        if (StringUtils.isNotEmpty(this.errorCode)) {
            message.setErrorCode(this.errorCode);
        }
        return message;
    }
}