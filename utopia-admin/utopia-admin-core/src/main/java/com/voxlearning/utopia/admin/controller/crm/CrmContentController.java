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

package com.voxlearning.utopia.admin.controller.crm;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.core.runtime.ProductConfig;
import com.voxlearning.utopia.service.content.api.entity.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Shuai Huan on 2014/6/10.
 */
@Controller
@RequestMapping("/crm/content")

public class CrmContentController extends CrmAbstractController {

    Map<Integer, Subject> map = Subject.toMap();

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    String contentIndex(Model model) {

        Long lessonId = getRequestLong("lessonId");
        int subjectId = getRequestInt("subjectId");
        long feedbackId = getRequestLong("feedbackId");
        long practiceId = getRequestLong("practiceId");
        Map<String, Object> result = new HashMap<>();
        boolean needFlashData = false;
        if (subjectId == Subject.MATH.getId()) {
            MathLesson lesson = mathContentLoaderClient.loadMathLesson(lessonId);
            if (lesson != null) {
                MathUnit unit = mathContentLoaderClient.loadMathUnit(lesson.getUnitId());
                result.put("unit", unit);
                if (unit != null) {
                    MathBook book = mathContentLoaderClient.loadMathBook(unit.getBookId());
                    result.put("book", book);
                }
                result.put("lesson", lesson);
                needFlashData = true;
            }
        } else if (subjectId == Subject.ENGLISH.getId()) {
            Lesson lesson = englishContentLoaderClient.loadEnglishLesson(lessonId);
            if (lesson != null) {
                Unit unit = englishContentLoaderClient.loadEnglishUnit(lesson.getUnitId());
                result.put("unit", unit);
                if (unit != null) {
                    Book book = englishContentLoaderClient.loadEnglishBook(unit.getBookId());
                    result.put("book", book);
                }
                result.put("lesson", lesson);
                needFlashData = true;
            }
        }

        // FIXME: 先注释掉，需要再重写
//        if (needFlashData) {
//            Subject type = map.get(subjectId);
//            if (practiceId == 0L) {
//                UserFeedback userFeedback = crmUserFeedbackDao.load(feedbackId);
//                practiceId = userFeedback.getPracticeType();
//            }
//
//            PracticeType entity = practiceLoaderClient.loadPractice(practiceId);

//            String json = JsonUtils.toJson(flashGameService.loadData(null, lessonId, entity, type.getValue().substring(0, 2)));
//            if (!StringUtils.isEmpty(json)) {
//                json = json.replaceAll("[ \r\n]+", "");
//                json = json.replaceAll("([\\[\\{])", "<blockquote>");
//                json = json.replaceAll("([\\]\\}])", "</blockquote>");
//            }
//            result.put("flashData", json);
//        }
        result.put("urlPrefix", ProductConfig.getMainSiteBaseUrl());
        model.addAttribute("result", result);
        return "crm/content/contentindex";
    }

}
