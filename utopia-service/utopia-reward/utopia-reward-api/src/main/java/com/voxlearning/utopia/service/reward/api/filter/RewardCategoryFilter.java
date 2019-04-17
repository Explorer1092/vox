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

package com.voxlearning.utopia.service.reward.api.filter;

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.reward.constant.RewardProductType;
import com.voxlearning.utopia.service.reward.entity.RewardCategory;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.stream.Collectors;

public class RewardCategoryFilter {

    private static final EnumSet<UserType> SUPPORTED;

    static {
        SUPPORTED = EnumSet.of(UserType.STUDENT, UserType.TEACHER, UserType.RESEARCH_STAFF);
    }

    public static List<RewardCategory> filter(final List<RewardCategory> src,
                                              final RewardProductType productType,
                                              final UserType userType) {
        if (userType != null && !SUPPORTED.contains(userType)) {
            return Collections.emptyList();
        }
        List<RewardCategory> categories = src;
        if (productType != null) {
            categories = categories.stream()
                    .filter(e -> StringUtils.equals(productType.name(), e.getProductType()))
                    .collect(Collectors.toList());
        }
        if (userType == null) {
            return categories;
        }
        return filter(categories, userType);
    }

    public static List<RewardCategory> filter(final List<RewardCategory> src,
                                              final UserType userType) {
        if (userType != null && !SUPPORTED.contains(userType)) {
            return Collections.emptyList();
        }
        List<RewardCategory> categories = src;
        if (userType == null) {
            return categories;
        }
        switch (userType) {
            case STUDENT:
                return categories.stream()
                        .filter(e -> SafeConverter.toBoolean(e.getStudentVisible()))
                        .collect(Collectors.toList());
            case TEACHER:
            case RESEARCH_STAFF:
                return categories.stream()
                        .filter(source -> SafeConverter.toBoolean(source.getTeacherVisible()))
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedOperationException();
        }
    }
}
