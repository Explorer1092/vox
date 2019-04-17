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

package com.voxlearning.utopia.service.surl;

import com.voxlearning.alps.config.runtime.ProductProperties;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import lombok.Cleanup;
import org.slf4j.Logger;

import javax.inject.Named;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Properties;

/**
 * Created by xin.xin on 9/25/15.
 */
@Named
public class Configure {
    private static final Logger logger = LoggerFactory.getLogger(Configure.class);

    private static final Properties properties;

    static {
        properties = new Properties();
        try {
            @Cleanup InputStream io = Configure.class.getClassLoader().getResourceAsStream("config/surl.properties");
            properties.load(io);
            logger.info("load surl.properties success.");
        } catch (IOException e) {
            throw new IllegalStateException("failed to load surl.properties.");
        }
    }

    public static int getServerPort() {
        String sp = ProductProperties.getProperty("product.config.surl.server.port", null);
        if (sp == null) {
            logger.warn("No product.config.surl.server.port defined, use default 8080");
            return 8080;
        } else {
            return Integer.parseInt(sp);
        }
    }

    public static int getThresholdCount() {
        Integer count = Integer.parseInt(properties.getProperty("surl.access.control.threshold.count", null));
        if (count == null) {
            logger.warn("No surl.access.control.threshold.count defined,use default 50");
            return 50;
        } else {
            return count;
        }
    }

    public static List<String> getWhiteDomainList() {
        List<String> domains = getDomainList("product.config.surl.whitedomain");
        if (CollectionUtils.isEmpty(domains)) {
            domains.add("17zuoye.com");
            domains.add("17zuoye.net");
            domains.add("17xueba.com");
        }
        return domains;
    }

    public static List<String> getBlockDomainList() {
        List<String> domains = getDomainList("product.config.surl.blockdomain");
        if (CollectionUtils.isEmpty(domains)) {
            domains.add("17zyw.cn");
            domains.add("d.test.17zuoye.net");
        }

        return domains;
    }

    public static String getSignatureKey() {
        String key = ProductProperties.getProperty("product.config.surl.sign.key");
        if (StringUtils.isBlank(key)) {
            throw new RuntimeException("short url signature key must not be blank");
        }
        return key;
    }

    private static List<String> getDomainList(String key) {
        String domains = ProductProperties.getProperty(key);
        if (StringUtils.isBlank(domains)) {
            return new ArrayList<>();
        }

        return Arrays.asList(domains.split(","));
    }
}
