/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.washington.data;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * A common result data structure.
 *
 * @author Lin Zhu
 * @author Xiaohai Zhang
 * @version 0.1
 * @serial
 * @since 2011-10-31
 */
public class OpenAuthContext implements Serializable {
    private static final long serialVersionUID = 918977514735362400L;

    private static final String CODE_KEY = "code";
    private static final String ERROR_KEY = "error";

    private Map<String, Object> fields = new LinkedHashMap<>();
    private Map<String, Object> params = new LinkedHashMap<>();

    private Long creatAt;

    public OpenAuthContext(Map<String, Object> params, String code, String error) {
        creatAt = System.currentTimeMillis();
        setParams(params);
        setCode(code);
        setError(error);
    }

    public void add(String key, Object value) {
        fields.put(key, value);
    }

    public Object get(String key) {
        return fields.get(key);
    }

    public void setCode(String code) {
        fields.put(CODE_KEY, code);
    }

    public void setError(String error) {
        fields.put(ERROR_KEY, error);
    }

    public Map<String, Object> getFields() {
        return fields;
    }

    public void setFields(Map<String, Object> fields) {
        this.fields = fields;
    }

    public Long getCreatAt() {
        return creatAt;
    }

    public void setCreatAt(Long creatAt) {
        this.creatAt = creatAt;
    }

    public Map<String, Object> getParams() {
        return params;
    }

    public void setParams(Map<String, Object> params) {
        this.params = params;
    }

}
