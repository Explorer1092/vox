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

package com.voxlearning.washington.controller.open.v1.content.impl;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.MathBook;
import com.voxlearning.utopia.service.content.api.entity.MathLesson;
import com.voxlearning.utopia.service.content.api.entity.MathUnit;
import com.voxlearning.washington.controller.open.v1.content.AbstractContentLoaderWrapper;

import javax.inject.Named;
import java.util.Collections;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Created by Alex on 14-10-16.
 */
@Named
public class MathContentLoaderWrapper extends AbstractContentLoaderWrapper {

    public List<String> loadPressByBookType(Integer bookType) {
        return mathContentLoaderClient.loadMathAvailableBookPresses()
                .stream()
                .collect(Collectors.toList());
    }

    @Override
    public List loadPressBooks(String press, String subject) {
        return mathContentLoaderClient.loadMathBooks()
                .enabled()
                .filter(t -> StringUtils.equals(t.getPress(), press))
                .clazzLevel_termType_ASC()
                .toList();
    }

    @Override
    public List loadClazzLevelBook(Integer clazzLevel) {
        ClazzLevel clazzLevel1 = ClazzLevel.parse(clazzLevel);
        if (clazzLevel1 == null) {
            return Collections.emptyList();
        }

        return mathContentLoaderClient.getExtension().loadMathBooksByClassLevelOrderByTerm(clazzLevel1, Term.全年);
    }

    @Override
    public Object loadBook(Long bookId) {
        return mathContentLoaderClient.loadMathBook(bookId);
    }

    @Override
    public List loadBookUnitList(Long bookId) {
        return mathContentLoaderClient.loadMathBookUnits(bookId);
    }

    @Override
    public Object loadUnit(Long unitId) {
        return mathContentLoaderClient.loadMathUnit(unitId);
    }

    @Override
    public List loadUnitLessonList(Long unitId) {
        return mathContentLoaderClient.loadMathUnitLessons(unitId);
    }

    @Override
    public Map<String, Object> convertBookInfo(String subject, Object book) {
        if (book == null || !(book instanceof MathBook)) {
            return null;
        }

        MathBook bookItem = (MathBook) book;
        Map<String, Object> bookInfo = new LinkedHashMap<>();
        addIntoMap(bookInfo, RES_BOOK_ID, bookItem.getId());
        addIntoMap(bookInfo, RES_BOOK_SUBJECT, subject);
        addIntoMap(bookInfo, RES_BOOK_TYPE, 1);
        addIntoMap(bookInfo, RES_BOOK_CLASS_LEVEL, bookItem.getClassLevel());
        addIntoMap(bookInfo, RES_BOOK_START_CLASS_LEVEL, null);
        addIntoMap(bookInfo, RES_BOOK_TERM, bookItem.getTerm());
        addIntoMap(bookInfo, RES_BOOK_PRESS, bookItem.getPress());
        addIntoMap(bookInfo, RES_CNAME, bookItem.getCname());
        addIntoMap(bookInfo, RES_ENAME, null);
        Boolean latestVersion = "1".equals(bookItem.getVersions());
        addIntoMap(bookInfo, RES_BOOK_LATEST_VERSION, latestVersion);
        addIntoMap(bookInfo, RES_BOOK_OPEN_EXAM, bookItem.getOpenExam());
        BookPress bookPress = BookPress.getBySubjectAndPress(Subject.MATH, bookItem.getPress());
        if (bookPress != null) {
            addIntoMap(bookInfo, RES_BOOK_COLOR, bookPress.getColor());
            addIntoMap(bookInfo, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
        }
        return bookInfo;
    }

    @Override
    public Map<String, Object> convertUnitInfo(Object unit) {
        if (unit == null || !(unit instanceof MathUnit)) {
            return Collections.emptyMap();
        }

        MathUnit unitItem = (MathUnit) unit;
        Map<String, Object> unitInfo = new LinkedHashMap<>();
        addIntoMap(unitInfo, RES_BOOK_ID, unitItem.getBookId());
        addIntoMap(unitInfo, RES_UNIT_ID, unitItem.getId());
        addIntoMap(unitInfo, RES_CNAME, unitItem.getCname());
        addIntoMap(unitInfo, RES_ENAME, null);
        addIntoMap(unitInfo, RES_RANK, unitItem.getRank());
        return unitInfo;
    }

    @Override
    public Map<String, Object> convertLessonInfo(Object lesson) {
        if (lesson == null || !(lesson instanceof MathLesson)) {
            return null;
        }

        MathLesson lessonItem = (MathLesson) lesson;
        Map<String, Object> lessonInfo = new LinkedHashMap<>();
        addIntoMap(lessonInfo, RES_UNIT_ID, lessonItem.getUnitId());
        addIntoMap(lessonInfo, RES_LESSON_ID, lessonItem.getId());
        addIntoMap(lessonInfo, RES_CNAME, lessonItem.getCname());
        addIntoMap(lessonInfo, RES_RANK, lessonItem.getRank());

        return lessonInfo;
    }

    @Override
    public List<Map<String, Object>> groupUnitStructure(List<Map<String, Object>> unitList) {
        return Collections.emptyList();
    }
}
