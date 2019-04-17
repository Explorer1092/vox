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

package com.voxlearning.utopia.service.psr.impl.dao;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.content.api.entity.PracticeType;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

@Named
public class PsrPracticeTypePersistence extends SpringContainerSupport {

    @Inject private PracticeLoaderClient practiceLoaderClient;

    @Getter @Setter
    private Map<Long, PracticeType> practiceTypeMap;

    public PracticeType findById(Long practiceTypeId) {
        if (practiceTypeId == null) {
            return null;
        }
        if (practiceTypeMap == null)
            practiceTypeMap = new HashMap<>();

        if (practiceTypeMap.containsKey(practiceTypeId))
            return practiceTypeMap.get(practiceTypeId);

        PracticeType practiceType = practiceLoaderClient.loadPractice(practiceTypeId);
        if (practiceType != null)
            practiceTypeMap.put(practiceTypeId, practiceType);

        return practiceType;
    }

    public String findAppEnPatternById(Long practiceTypeId) {
        PracticeType practiceType = findById(practiceTypeId);
        if (practiceType == null)
            return null;
        if (!"单词辨识".equals(practiceType.getCategoryName())
                && !"听音选词".equals(practiceType.getCategoryName())
                && !"看图识词".equals(practiceType.getCategoryName())
                && !"单词拼写".equals(practiceType.getCategoryName()))
            return null;

        return "pattern#" + practiceType.getCategoryName() + ";";
    }

    public String findAppMathPatternById(Long practiceTypeId) {
        PracticeType practiceType = findById(practiceTypeId);
        if (practiceType == null)
            return null;

        return "pattern#" + practiceType.getCategoryName() + ";";
    }
}
