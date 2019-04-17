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

package com.voxlearning.utopia.agent.support;

import freemarker.template.Configuration;
import freemarker.template.Template;

import java.io.IOException;
import java.util.Locale;

@Deprecated
public class FreemarkerTemplateParser {
    private static final Configuration configuration;

    static {
        // keep compatibility with freemarker 2.3.0
        configuration = new Configuration(Configuration.VERSION_2_3_0);
        configuration.setClassForTemplateLoading(FreemarkerTemplateParser.class, "/");
        configuration.setTemplateUpdateDelay(0);
        configuration.setDateTimeFormat("yyyy-MM-dd HH:mm:ss");
        configuration.setDateFormat("yyyy/MM/dd");
        configuration.setTimeFormat("HH:mm:ss");
        configuration.setNumberFormat("0.######");
        configuration.setBooleanFormat("true,false");
        configuration.setWhitespaceStripping(true);
        configuration.setDefaultEncoding("UTF-8");
        configuration.setURLEscapingCharset("UTF-8");
        configuration.setLocale(Locale.CHINA);
        configuration.setTagSyntax(Configuration.AUTO_DETECT_TAG_SYNTAX);
        configuration.setLocalizedLookup(false);
    }

    public static Template parse(String name) {
        try {
            return configuration.getTemplate(name);
        } catch (IOException ex) {
            throw new RuntimeException(ex.getMessage(), ex);
        }
    }
}
