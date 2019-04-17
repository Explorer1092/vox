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

package com.voxlearning.washington.controller.student;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.content.api.entity.MathUnit;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.content.api.entity.UnitDat;
import com.voxlearning.utopia.service.content.api.mapper.*;
import com.voxlearning.washington.support.AbstractController;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Student book controller implementation.
 *
 * @author Xinqiang Wang
 * @author Guohong Tan
 * @author Xiaohai Zhang
 * @serial 2013-03-29 15:34
 */
@Controller
@RequestMapping(value = "student/book")
public class StudentBookController extends AbstractController {

    /**
     * ************************************************** 自学相关 ***************************************************
     */

    // 2014暑期改版 -- 根据bookId查询units
    @RequestMapping(value = "units.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningUnit() {
        Long bookId = getRequestLong("bookId");
        String subjectStr = getRequest().getParameter("subjectType");
        Subject subject = Subject.of(subjectStr);

        switch (subject) {
            case ENGLISH: {
                return MapMessage.successMessage().add("units", englishContentLoaderClient.loadEnglishBookUnits(bookId));
            }
            case MATH: {
                return MapMessage.successMessage().add("units", mathContentLoaderClient.loadMathBookUnits(bookId));
            }
            case CHINESE: {
                return MapMessage.successMessage().add("units", chineseContentLoaderClient.loadChineseBookUnits(bookId));
            }
            default:
                return MapMessage.errorMessage("学科不存在");
        }
    }

    // 2014暑期改版 -- 英语 -- 根据bookId，unitId查询lessons
    @RequestMapping(value = "lessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningLesson() {
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");
        Unit unit = englishContentLoaderClient.loadEnglishUnit(unitId);
        List<ExLesson> lessons = englishContentLoaderClient.loadUnitExLessons(unitId);
        return MapMessage.successMessage().add("lessons", lessons).add("unit", unit).add("bookId", bookId);
    }

    // 2014暑期改版 -- 数学 -- 根据bookId，unitId查询lessons
    @RequestMapping(value = "mathlessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningMathLesson() {
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");

        MathUnit unit = mathContentLoaderClient.loadMathUnit(unitId);
        List<DisplayMathLessonMapper> lessons = mathContentLoaderClient.getExtension().loadMathLessonPracticeTypeInfoByUnitId(unitId);
        // 将专用知识点的应用分拆
        List<DisplayMathLessonMapper> specLessons = new ArrayList<>();
        for (int i = lessons.size() - 1; i >= 0; i--) {
            DisplayMathLessonMapper mathLessonMapper = lessons.get(i);
            DisplayMathLessonMapper newMathLessonMapper = null;

            List<DisplayMathPointMapper> mathPointMapperList = mathLessonMapper.getMathPointMapperList();
            for (int j = mathPointMapperList.size() - 1; j >= 0; j--) {
                DisplayMathPointMapper mathPointMapper = mathPointMapperList.get(j);
                if (mathPointMapper.isSpecialMathPoint()) {
                    if (newMathLessonMapper == null) { // create a new instance
                        newMathLessonMapper = mathLessonMapper.copy();
                    }
                    //专项知识点应用练习中，屏蔽掉类别为”知识点应用_老师端“的应用
                    List<DisplayPracticeTypeMapper> practiceTypeMapperList = mathPointMapper.getPracticeTypeMapperList();
                    practiceTypeMapperList = practiceTypeMapperList.stream().filter(source ->
                            !StringUtils.equals(source.getCategoryName(), "知识点应用_老师端")
                    ).collect(Collectors.toList());

                    mathPointMapper.setPracticeTypeMapperList(practiceTypeMapperList);
                    mathPointMapper.setDataTypeCountList(mathContentLoaderClient.getExtension().loadPointBaseTypeAndCount(mathPointMapper.getId()));
                    newMathLessonMapper.getMathPointMapperList().add(mathPointMapper);
                    mathLessonMapper.getMathPointMapperList().remove(j);
                }
            }
            if (newMathLessonMapper != null) {
                specLessons.add(newMathLessonMapper);
            }
            if (mathLessonMapper.getMathPointMapperList().size() == 0) {
                lessons.remove(i);
            }
        }
        lessons.addAll(specLessons);
        return MapMessage.successMessage().add("lessons", lessons).add("unit", unit).add("bookId", bookId);
    }

    // 2014暑期改版 -- 语文 -- 根据bookId，unitId查询lessons
    @RequestMapping(value = "chineselessons.vpage", method = RequestMethod.GET)
    @ResponseBody
    public MapMessage learningchineseLesson() {
        Long bookId = getRequestLong("bookId");
        Long unitId = getRequestLong("unitId");

        UnitDat unit = chineseContentLoaderClient.loadChineseUnit(unitId);
        List<ExLessonDat> lessons = chineseContentLoaderClient.loadChineseUnitExLessons(unitId);
        return MapMessage.successMessage().add("lessons", lessons).add("unit", unit).add("bookId", bookId);
    }
}
