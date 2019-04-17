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

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.entity.BookDat;
import com.voxlearning.utopia.service.content.api.entity.LessonDat;
import com.voxlearning.utopia.service.content.api.entity.UnitDat;
import com.voxlearning.washington.controller.open.v1.content.AbstractContentLoaderWrapper;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Created by Alex on 14-10-16.
 */
@Named
public class ChineseContentLoaderWrapper extends AbstractContentLoaderWrapper {

    public List<String> loadPressByBookType(Integer bookType) {
        return chineseContentLoaderClient.loadChineseBooks()
                .originalLocationsAsList()
                .stream()
                .filter(t -> !t.isDisabled())
                .filter(t -> t.getPress() != null)
                .map(BookDat.Location::getPress)
                .collect(Collectors.toSet())
                .stream()
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }

    @SuppressWarnings("unchecked")
    @Override
    public List loadPressBooks(String press, String subject) {
        return chineseContentLoaderClient.loadChineseBooks()
                .enabled()
                .filter(t -> StringUtils.equals(t.getPress(), press))
                .clazzLevel_termType_ASC()
                .toList();
    }

    @SuppressWarnings("unchecked")
    @Override
    public List loadClazzLevelBook(Integer clazzLevel) {
        return chineseContentLoaderClient.loadChineseBooks()
                .enabled()
                .online()
                .filter(t -> Objects.equals(t.getClazzLevel(), clazzLevel))
                .sorted((o1, o2) -> {
                    int t1 = o1.getTermType() == Term.全年.getKey() ? 0 : 1;
                    int t2 = o2.getTermType() == Term.全年.getKey() ? 0 : 1;
                    return Integer.compare(t1, t2);
                }).toList();
    }

    @Override
    public Object loadBook(Long bookId) {
        return chineseContentLoaderClient.loadChineseBook(bookId);
    }

    @Override
    public List loadBookUnitList(Long bookId) {
        return chineseContentLoaderClient.loadChineseBookUnits(bookId);
    }

    @Override
    public Object loadUnit(Long unitId) {
        return chineseContentLoaderClient.loadChineseUnit(unitId);
    }

    @Override
    public List loadUnitLessonList(Long unitId) {
        return chineseContentLoaderClient.loadChineseUnitLessons(unitId);
    }

    @Override
    public Map<String, Object> convertBookInfo(String subject, Object book) {
        if (book == null || !(book instanceof BookDat)) {
            return null;
        }

        BookDat bookItem = (BookDat) book;
        Map<String, Object> bookInfo = new LinkedHashMap<>();
        addIntoMap(bookInfo, RES_BOOK_ID, bookItem.getId());
        addIntoMap(bookInfo, RES_BOOK_SUBJECT, subject);
        addIntoMap(bookInfo, RES_BOOK_TYPE, bookItem.getBookType());
        addIntoMap(bookInfo, RES_BOOK_CLASS_LEVEL, bookItem.getClazzLevel());
        addIntoMap(bookInfo, RES_BOOK_START_CLASS_LEVEL, bookItem.getStartClazzLevel());
        addIntoMap(bookInfo, RES_BOOK_TERM, bookItem.getTermType());
        addIntoMap(bookInfo, RES_BOOK_PRESS, bookItem.getPress());
        addIntoMap(bookInfo, RES_CNAME, bookItem.getCname());
        addIntoMap(bookInfo, RES_ENAME, bookItem.getEname());
        addIntoMap(bookInfo, RES_BOOK_LATEST_VERSION, bookItem.getLatestVersion());
        addIntoMap(bookInfo, RES_BOOK_OPEN_EXAM, bookItem.getOpenExam());
        BookPress bookPress = BookPress.getBySubjectAndPress(Subject.CHINESE, bookItem.getPress());
        if (bookPress != null) {
            addIntoMap(bookInfo, RES_BOOK_COLOR, bookPress.getColor());
            addIntoMap(bookInfo, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
        }

        return bookInfo;
    }

    @Override
    public Map<String, Object> convertUnitInfo(Object unit) {
        if (unit == null || !(unit instanceof UnitDat)) {
            return Collections.emptyMap();
        }

        UnitDat unitItem = (UnitDat) unit;
        Map<String, Object> unitInfo = new LinkedHashMap<>();
        addIntoMap(unitInfo, RES_BOOK_ID, unitItem.getBookId());
        addIntoMap(unitInfo, RES_UNIT_ID, unitItem.getId());
        addIntoMap(unitInfo, RES_CNAME, unitItem.getCname());
        addIntoMap(unitInfo, RES_ENAME, unitItem.getEname());
        addIntoMap(unitInfo, RES_RANK, unitItem.getRank());

        return unitInfo;
    }

    @Override
    public Map<String, Object> convertLessonInfo(Object lesson) {
        if (lesson == null || !(lesson instanceof LessonDat)) {
            return null;
        }

        LessonDat lessonItem = (LessonDat) lesson;
        Map<String, Object> lessonInfo = new LinkedHashMap<>();
        addIntoMap(lessonInfo, RES_UNIT_ID, lessonItem.getUnitId());
        addIntoMap(lessonInfo, RES_LESSON_ID, lessonItem.getId());
        addIntoMap(lessonInfo, RES_CNAME, lessonItem.getCname());
        addIntoMap(lessonInfo, RES_RANK, lessonItem.getRank());
        addIntoMap(lessonInfo, RES_SHOW_NAME, lessonItem.getShowName());
        return lessonInfo;
    }

    @Override
    public List<Map<String, Object>> groupUnitStructure(List<Map<String, Object>> unitList) {
        return Collections.emptyList();
    }
}
