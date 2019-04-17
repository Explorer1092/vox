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

package com.voxlearning.washington.controller.open.v1.content;

import com.voxlearning.washington.controller.open.v1.content.impl.ChineseContentLoaderWrapper;
import com.voxlearning.washington.controller.open.v1.content.impl.EnglishContentLoaderWrapper;
import com.voxlearning.washington.controller.open.v1.content.impl.MathContentLoaderWrapper;

import javax.inject.Inject;
import javax.inject.Named;

import static com.voxlearning.alps.annotation.meta.Subject.*;

/**
 *
 * Created by Alex on 14-10-16.
 */
@Named
public class ContentLoaderWrapperFactory {
    @Inject private EnglishContentLoaderWrapper englishContentLoaderWrapper;
    @Inject private MathContentLoaderWrapper mathContentLoaderWrapper;
    @Inject private ChineseContentLoaderWrapper chineseContentLoaderWrapper;

    public AbstractContentLoaderWrapper getContentLoaderWrapper(String subject) {
        if (ENGLISH.name().toLowerCase().equals(subject)) {
            return englishContentLoaderWrapper;
        } else if (MATH.name().toLowerCase().equals(subject)) {
            return mathContentLoaderWrapper;
        } else if (CHINESE.name().toLowerCase().equals(subject)) {
            return chineseContentLoaderWrapper;
        }

        return null;
    }
}
