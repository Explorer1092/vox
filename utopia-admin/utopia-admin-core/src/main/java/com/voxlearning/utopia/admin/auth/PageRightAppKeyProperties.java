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

package com.voxlearning.utopia.admin.auth;

import org.apache.commons.io.IOUtils;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Properties;

/**
 * @author Longlong Yu
 * @since 下午10:36,13-11-22.
 */
public class PageRightAppKeyProperties {
    // 用户保存admin中各子系统name和对应的key，用于用户权限验证时生成appKey
    public static final Map<String, String> PAGE_RIGHT_APP_KEY_MAP;

    static {
        Properties prop = new Properties();
        String fn = "/config/pagerightappkey.properties";
        Resource resource = new ClassPathResource(fn);
        InputStream is = null;

        try {
            is = resource.getInputStream();
            prop.load(is);
        } catch (Exception ignored) {
            // ignored
        } finally {
            IOUtils.closeQuietly(is);
        }

        Map<String, String> map = new LinkedHashMap<>();

        if (!prop.isEmpty()) {
            for (Object propertyKey : prop.keySet()) {
                String strPropertyKey = (String) propertyKey;
                int strLength = strPropertyKey.length();
                if (strLength > 8 && strPropertyKey.substring(0, 8).equals("app_key.")) {
                    map.put(strPropertyKey.substring(8), (String) prop.get(propertyKey));
                }
            }
        }

        PAGE_RIGHT_APP_KEY_MAP = Collections.unmodifiableMap(map);
    }
}
