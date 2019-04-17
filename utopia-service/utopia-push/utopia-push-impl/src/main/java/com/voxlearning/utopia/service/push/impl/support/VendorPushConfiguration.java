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

package com.voxlearning.utopia.service.push.impl.support;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.runtime.RuntimeMode;
import lombok.Cleanup;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;

import java.io.InputStream;
import java.util.Properties;

/**
 *
 * Created by Shuai Huan on 2015/5/15.
 */
public class VendorPushConfiguration {

    private static final String CONFIG_PATH = "vendor.jpush.properties";

    private static final Properties properties;

    static {
        try {
            Resource resource = new ClassPathResource(CONFIG_PATH);
            @Cleanup InputStream inStream = resource.getInputStream();
            properties = new Properties();
            properties.load(inStream);
        } catch (Exception ex) {
            throw new RuntimeException("Failed to load JPush properties", ex);
        }
    }

    public static String getJPushAuthentication(String app) {
        if (StringUtils.isBlank(app)) {
            return null;
        }
        String propertyName = app + ".push.authentication.jg";

        String value = properties.getProperty(propertyName + "." + RuntimeMode.current().name());
        if (value != null) {
            return value;
        }
        return properties.getProperty(propertyName);
    }


    public static String getMiPushAuthentication(String app) {
        if (StringUtils.isBlank(app)) {
            return null;
        }
        String propertyName = app + ".push.authentication.mi";

        String value = properties.getProperty(propertyName + "." + RuntimeMode.current().name());
        if (value != null) {
            return value;
        }
        return properties.getProperty(propertyName);
    }

    public static String getUmengAndriodAppKey(String app){
        if (StringUtils.isBlank(app)) {
            return null;
        }
        String propertyName = app + ".push.umeng.andriod.appkey";

        String value = properties.getProperty(propertyName + "." + RuntimeMode.current().name());
        if (value != null) {
            return value;
        }
        return properties.getProperty(propertyName);
    }

    public static String getUmengIOSAppKey(String app){
        if (StringUtils.isBlank(app)) {
            return null;
        }
        String propertyName = app + ".push.umeng.ios.appkey";

        String value = properties.getProperty(propertyName + "." + RuntimeMode.current().name());
        if (value != null) {
            return value;
        }
        return properties.getProperty(propertyName);
    }

    public static String getUmengAndriodSecret(String app){
        if (StringUtils.isBlank(app)) {
            return null;
        }
        String propertyName = app + ".push.umeng.andriod.secret";

        String value = properties.getProperty(propertyName + "." + RuntimeMode.current().name());
        if (value != null) {
            return value;
        }
        return properties.getProperty(propertyName);
    }

    public static String getUmengIOSSecret(String app){
        if (StringUtils.isBlank(app)) {
            return null;
        }
        String propertyName = app + ".push.umeng.ios.secret";

        String value = properties.getProperty(propertyName + "." + RuntimeMode.current().name());
        if (value != null) {
            return value;
        }
        return properties.getProperty(propertyName);
    }

}
