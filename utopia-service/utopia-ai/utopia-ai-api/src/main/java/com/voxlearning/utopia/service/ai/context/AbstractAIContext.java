package com.voxlearning.utopia.service.ai.context;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Summer on 2018/3/29
 */
public class AbstractAIContext<T extends AbstractAIContext> implements Serializable {

    private static final long serialVersionUID = -7277628200195879257L;

    @Getter
    @Setter
    private boolean successful = true;
    @Getter
    @Setter
    private boolean duplicated;
    @Getter
    @Setter
    private String message = "";
    @Getter
    @Setter
    private String errorCode = "";
    // for those who want to terminate the processes, but keep successful true
    @Getter
    @Setter
    private boolean terminateTask = false;
    @Getter
    @Setter
    private String templateClassName;
    @Getter
    private final Map<String, String> additions = new LinkedHashMap<>();

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
}
