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

package com.voxlearning.washington.service.support;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;
import com.voxlearning.utopia.api.constant.StudyType;
import com.voxlearning.washington.service.LoadFlashGameContext;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * For loading homework flash game.
 *
 * @author Xiaohai Zhang
 * @since 2013-06-08 13:41
 */
@Named
@Slf4j
@NoArgsConstructor
@IdentificationStudyType(StudyType.homework)
public class GameFlashLoaderTemplate_Homework extends GameFlashLoaderTemplate {
    @Override
    protected void verifyContext(LoadFlashGameContext context) {
        Validate.notNull(context.getUserId());
        Validate.notNull(context.getHomeworkId());
        Validate.notNull(context.getClazzId());
    }

    @Override
    protected String buildJson(LoadFlashGameContext context) {
        Map<String, Object> jsonMap = new HashMap<>();
        jsonMap.put("userId", String.valueOf(context.getUserId()));
        jsonMap.put("studyType", context.getStudyType().name());
        jsonMap.put("bookId", String.valueOf(context.getBookId()));
        jsonMap.put("unitId", String.valueOf(context.getUnitId()));
        jsonMap.put("lessonId", String.valueOf(context.getLessonId()));
        jsonMap.put("hid", context.getHomeworkId());
        jsonMap.put("cid", String.valueOf(context.getClazzId()));
        //gameType和practiceType取的是同一个值，以后新FLASH应用统一取gameType,但为了兼容老的FLASH应用，所以practiceType也保留
        jsonMap.put("gameType", String.valueOf(context.getEnglishPractice().getId()));
        jsonMap.put("practiceType", String.valueOf(context.getEnglishPractice().getId()));
        jsonMap.put("grade",String.valueOf(context.getClazzLevel())); //年级
        jsonMap.put("prvRgnCode",String.valueOf(context.getPrvRgnCode())); //省编码
        //flush往后台传数的时候中文有编码问题
//        jsonMap.put("gameName",context.getPracticeType().getValue());
        return JsonUtils.toJson(jsonMap);
    }
}
