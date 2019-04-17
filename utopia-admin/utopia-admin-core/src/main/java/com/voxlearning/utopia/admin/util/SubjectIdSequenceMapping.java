/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 *  Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *
 *  NOTICE: All information contained herein is, and remains the property of
 *  Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 *  and technical concepts contained herein are proprietary to Vox Learning
 *  Technology, Inc. and its suppliers and may be covered by patents, patents
 *  in process, and are protected by trade secret or copyright law. Dissemination
 *  of this information or reproduction of this material is strictly forbidden
 *  unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.admin.util;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Created by Shuai Huan on 2014/5/27.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SubjectIdSequenceMapping {
    UNKNOWN(0, "未知"),
    JUNIOR_CHINESE(201, "cnj"),
    MIDDLE_CHINESE(301, "cnm"),
    JUNIOR_ENGLISH(203, "enj"),
    MIDDLE_ENGLISH(303, "enm"),
    JUNIOR_MATH(202, "mathj"),
    MIDDLE_MATH(302, "mathm"),
    JUNIOR_PHYSICAL(204, "physicsj"),
    MIDDLE_PHYSICAL(304, "physicsm"),
    JUNIOR_CHEMISTRY(205, "chemistryj"),
    MIDDLE_CHEMISTRY(305, "chemistrym");
    @Getter
    private final int key;
    @Getter private final String sequenceName;

    private static final Map<Integer, SubjectIdSequenceMapping> mappings;

    static {
        mappings = new LinkedHashMap<>();
        for (SubjectIdSequenceMapping mapping : values()) {
            mappings.put(mapping.getKey(), mapping);
        }
    }

    public static SubjectIdSequenceMapping of(Integer value) {
        SubjectIdSequenceMapping mapping = mappings.get(value);
        return mapping == null ? UNKNOWN : mapping;
    }

    public static String getSequenceName(int key) {
        SubjectIdSequenceMapping mapping = of(key);
        return mapping.getSequenceName();
    }
}
