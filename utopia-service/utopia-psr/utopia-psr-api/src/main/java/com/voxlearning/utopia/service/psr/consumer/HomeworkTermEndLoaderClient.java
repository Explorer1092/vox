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

package com.voxlearning.utopia.service.psr.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.psr.homeworktermend.client.IHomeworkTermEndLoaderClient;
import com.voxlearning.utopia.service.psr.homeworktermend.loader.HomeworkTermEndLoader;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.EnglishQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathMentalQuestionBox;
import com.voxlearning.utopia.service.psr.homeworktermend.mapper.MathQuestionBox;
import lombok.Getter;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2016-05-12
 */
public class HomeworkTermEndLoaderClient implements IHomeworkTermEndLoaderClient {

    @Getter
    @ImportService(interfaceClass = HomeworkTermEndLoader.class)
    private HomeworkTermEndLoader remoteReference;

    @Override
    public Map<String, EnglishQuestionBox> loadEnglishQuestionBoxs(Collection<String> boxIds) {
        if (CollectionUtils.isEmpty(boxIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadEnglishQuestionBoxs(boxIds);
    }

    @Deprecated
    @Override
    public Map<String, MathMentalQuestionBox> loadMathMentalQuestionBoxs(Collection<String> boxIds) {
        if (CollectionUtils.isEmpty(boxIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadMathMentalQuestionBoxs(boxIds);
    }

    @Override
    public Map<String, MathQuestionBox> loadMathQuestionBoxs(Collection<String> boxIds) {
        if (CollectionUtils.isEmpty(boxIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadMathQuestionBoxs(boxIds);
    }

    @Override
    public List<EnglishQuestionBox> pushEnglishQuestionBoxes(List<Long> unitIds, Long userId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        return remoteReference.pushEnglishQuestionBoxes(unitIds, userId);
    }

    @Override
    public List<MathQuestionBox> pushMathQuestionBoxes(List<String> unitIds, Long teacherId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        return remoteReference.pushMathQuestionBoxes(unitIds, teacherId);
    }

    @Override
    public List<MathMentalQuestionBox> pushMathMentalQuestionBoxes(List<String> unitIds, Long teacherId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        return remoteReference.pushMathMentalQuestionBoxes(unitIds, teacherId);
    }


    @Override
    public List<MathMentalQuestionBox> pushMathMentalQuestionBoxesByTermEndUnit(List<String> unitIds, Long teacherId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        return remoteReference.pushMathMentalQuestionBoxesByTermEndUnit(unitIds, teacherId);
    }


    @Override
    public List<MathQuestionBox> pushMathQuestionBoxesByTermEndUnit(List<String> unitIds, Long teacherId) {
        if (CollectionUtils.isEmpty(unitIds)) {
            return Collections.emptyList();
        }
        return remoteReference.pushMathQuestionBoxesByTermEndUnit(unitIds, teacherId);
    }
}
