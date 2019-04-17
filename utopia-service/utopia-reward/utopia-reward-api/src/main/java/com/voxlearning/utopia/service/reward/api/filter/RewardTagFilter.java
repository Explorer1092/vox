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
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.reward.constant.RewardTagLevel;
import com.voxlearning.utopia.service.reward.entity.RewardTag;

import java.util.Collections;
import java.util.EnumSet;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

public class RewardTagFilter {

    private static final EnumSet<UserType> SUPPORTED;

    static {
        SUPPORTED = EnumSet.of(UserType.STUDENT, UserType.TEACHER, UserType.RESEARCH_STAFF);
    }

    public static List<RewardTag> filter(final List<RewardTag> src,
                                         final RewardTagLevel tagLevel,
                                         final UserType userType) {
        if (userType != null && !SUPPORTED.contains(userType)) {
            return Collections.emptyList();
        }
        if (src == null) {
            return Collections.emptyList();
        }
        List<RewardTag> tags = src;
        if (tagLevel != null) {
            tags = tags.stream()
                    .filter(e -> StringUtils.equals(e.getTagLevel(), tagLevel.name()))
                    .collect(Collectors.toList());
        }
        if (userType == null) {
            return tags;
        }
        switch (userType) {
            case STUDENT:
                return tags.stream()
                        .filter(e -> SafeConverter.toBoolean(e.getStudentVisible()))
                        .collect(Collectors.toList());
            case RESEARCH_STAFF:
            case TEACHER:
                return tags.stream()
                        .filter(e -> SafeConverter.toBoolean(e.getTeacherVisible()))
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedOperationException();
        }
    }

    public static List<RewardTag> filter(final List<RewardTag> src, final List<Long> idList, final UserType userType) {
        if (CollectionUtils.isEmpty(idList)) {
            return Collections.emptyList();
        }
        if (src == null) {
            return Collections.emptyList();
        }

        Map<Long, Long> idMap = idList.stream().collect(Collectors.toMap(Function.identity(), Function.identity(), (o1, o2) -> o2));

        List<RewardTag> tags = src;
        tags = tags.stream()
                .filter(e -> idMap.containsKey(e.getId()))
                .collect(Collectors.toList());
        if (userType == null) {
            return tags;
        }
        switch (userType) {
            case STUDENT:
                return tags.stream()
                        .filter(e -> SafeConverter.toBoolean(e.getStudentVisible()))
                        .collect(Collectors.toList());
            case RESEARCH_STAFF:
            case TEACHER:
                return tags.stream()
                        .filter(e -> SafeConverter.toBoolean(e.getTeacherVisible()))
                        .collect(Collectors.toList());
            default:
                throw new UnsupportedOperationException();
        }
    }
}
