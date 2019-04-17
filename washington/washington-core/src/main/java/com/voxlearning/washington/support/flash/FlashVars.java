/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.support.flash;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;

import javax.servlet.http.HttpServletRequest;
import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * FlashVars.java
 *
 * @author Lin Zhu
 * @author Xiaohai Zhang
 * @since 2011-8-24
 */
public class FlashVars implements Serializable {
    private static final long serialVersionUID = 3037512125902373029L;

    private Map<String, Object> vars = new HashMap<String, Object>();

    private HttpServletRequest request;

    public FlashVars(HttpServletRequest request) {
        this.request = request;
        // 添加默认值
//		vars.put("domain", getDomain());
//		vars.put("servName", getServName());
        // Flash调用外部图片地址
//		add("imageUrl", "resources/apps/flash/", true);
    }

    public String getDomain() {
        return request.getScheme() + "://" + request.getServerName() + ((request.getServerPort() != 80) ? ":" + request.getServerPort() : "");
    }

    public String getServName() {
        return request.getContextPath();
    }

    public String getRequestParam() {
        StringBuilder sb = new StringBuilder();
        for (String key : vars.keySet()) {
            sb.append(key).append("=").append(vars.get(key)).append("&");
        }
        String param = sb.toString();
        return param.substring(0, param.length() - 1);
    }

    public String getJsonParam() {
        return JsonUtils.toJson(vars);
    }

    public Map<String, Object> getVars() {
        return vars;
    }

    public void add(String key, Object value) {
        add(key, value, false);
    }

    public void add(String key, Object value, boolean isUri) {
        vars.put(key, (isUri) ? getDomain() + getServName() + "/" + value : value);
    }
}
