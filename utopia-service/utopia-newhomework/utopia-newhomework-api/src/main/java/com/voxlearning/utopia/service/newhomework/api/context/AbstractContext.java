/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IClassLoader;
import lombok.Getter;
import lombok.Setter;
import lombok.SneakyThrows;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/6
 */
abstract public class AbstractContext<T extends AbstractContext> implements ITemplateContext {
    private static final long serialVersionUID = 8255051007677060797L;

    private static final Class<?> stringUtilsClass;

    static {
        String className = "com.voxlearning.alps.util.StringUtils";
        Class<?> theClass;
        try {
            theClass = IClassLoader.defaultClassLoader().loadClass(className);
        } catch (ClassNotFoundException e) {
            theClass = null;
        }
        stringUtilsClass = theClass;
    }

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
    @SneakyThrows({NoSuchMethodException.class, IllegalAccessException.class, InvocationTargetException.class})
    public T errorResponse(String message, Object... parameters) {
        this.successful = false;
        if (stringUtilsClass == null) {
            this.message = message;
        } else {
            Method method = stringUtilsClass.getMethod("formatMessage", String.class, Object[].class);
            this.message = (String) method.invoke(null, message, parameters);
        }
        if (this.message == null)
            this.message = "";
        return (T) this;
    }

    public T successResponse() {
        return successResponse("");
    }

    @SuppressWarnings("unchecked")
    @SneakyThrows({NoSuchMethodException.class, IllegalAccessException.class, InvocationTargetException.class})
    public T successResponse(String message, Object... parameters) {
        this.successful = true;
        if (stringUtilsClass == null) {
            this.message = message;
        } else {
            Method method = stringUtilsClass.getMethod("formatMessage", String.class, Object[].class);
            this.message = (String) method.invoke(null, message, parameters);
        }
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
        if (this.message != null && this.message.length() > 0) {
            message.setInfo(this.message);
        }
        if (this.errorCode != null && this.errorCode.length() > 0) {
            message.setErrorCode(this.errorCode);
        }
        return message;
    }
}
