/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.content;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.service.content.consumer.ChineseContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.MathContentLoaderClient;
import com.voxlearning.washington.cache.WashingtonCacheSystem;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Created by Alex on 14-10-16.
 */
public abstract class AbstractContentLoaderWrapper {

    @Inject protected ChineseContentLoaderClient chineseContentLoaderClient;
    @Inject protected EnglishContentLoaderClient englishContentLoaderClient;
    @Inject protected MathContentLoaderClient mathContentLoaderClient;
    @Inject private WashingtonCacheSystem washingtonCacheSystem;

    public abstract List<String> loadPressByBookType(Integer bookType);

    public List<Map<String, Object>> loadBooksByPress(String press, String subject) {
        List bookList = loadPressBooks(press, subject);
        if (bookList == null || bookList.size() == 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> retBookList = new ArrayList<>();
        for (Object bookItem : bookList) {
            CollectionUtils.addNonNullElement(retBookList, convertBookInfo(subject, bookItem));
        }

        return retBookList;
    }

    public List<Map<String, Object>> loadBooksByClazzLevel(String subject, Integer clazzLevel) {
        List bookList = loadClazzLevelBook(clazzLevel);
        if (bookList == null || bookList.size() == 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> retBookList = new ArrayList<>();
        for (Object bookItem : bookList) {
            CollectionUtils.addNonNullElement(retBookList, convertBookInfo(subject, bookItem));
        }

        return retBookList;
    }

    public Map<String, Object> loadBookInfo(String subject, Long bookId) {
        String cacheKey = CacheKeyGenerator.generateCacheKey(AbstractContentLoaderWrapper.class,
                new String[]{"subject", "bookId"},
                new Object[]{subject, bookId});
        Map<String, Object> bookInfo = washingtonCacheSystem.CBS.flushable.load(cacheKey);
        if (MapUtils.isNotEmpty(bookInfo)) {
            return bookInfo;
        }
        Object book = loadBook(bookId);
        if (book == null) {
            return Collections.emptyMap();
        }

        bookInfo = convertBookInfo(subject, book);
        List<Map<String, Object>> units = loadBookUnits(bookId);
        bookInfo.put(RES_UNIT_LIST, units);
        List<Map<String, Object>> groupList = groupUnitStructure(units);
        bookInfo.put(RES_GROUP_FLAG, CollectionUtils.isNotEmpty(groupList));
        bookInfo.put(RES_GROUP_LIST, groupList);

        washingtonCacheSystem.CBS.flushable.add(cacheKey, DateUtils.getCurrentToDayEndSecond(), bookInfo);

        return bookInfo;
    }

    private List<Map<String, Object>> loadBookUnits(Long bookId) {
        List unitList = loadBookUnitList(bookId);
        if (unitList == null || unitList.size() == 0) {
            return Collections.emptyList();
        }

        List<Map<String, Object>> retUnitList = new ArrayList<>();
        for (Object unitItem : unitList) {
            CollectionUtils.addNonNullElement(retUnitList, convertUnitInfo(unitItem));
        }
        return retUnitList;
    }

    public abstract List<Map<String, Object>> groupUnitStructure(List<Map<String, Object>> unitList);

    public Map<String, Object> loadUnitInfo(Long unitId) {
        Object unit = loadUnit(unitId);
        if (unit == null) {
            return Collections.emptyMap();
        }

        Map<String, Object> unitInfo = convertUnitInfo(unit);
        //unitInfo.put(RES_LESSON_LIST, loadUnitLessons(unitId));

        return unitInfo;
    }

//    public abstract List<Map<String, Object>> loadUnitLessons(Long unitId);

//    private List<Map<String, Object>> loadUnitLessons(Long unitId) {
//        List lessonList = loadUnitLessonList(unitId);
//        if (lessonList == null || lessonList.size() == 0) {
//            return Collections.emptyList();
//        }
//
//        List<Map<String, Object>> retLessonList = new ArrayList<>();
//        for (Object lessonItem : lessonList) {
//            MiscUtils.addNonNullElement(retLessonList, loadLessonInfo(lessonItem));
//        }
//
//        return retLessonList;
//    }

//    public Map<String, Object> loadLessonInfo(Object lesson) {
//        if (lesson == null) {
//            return Collections.emptyMap();
//        }
//
//        // LESSON具体信息
//        Map<String, Object> lessonInfo = convertLessonInfo(lesson);
//
//        // LESSON的内容信息，英语是SENTENCE信息，语文是字词信息，数学没有
//        List<String> contentList = loadLessonContent(lesson);
//        if (contentList != null && contentList.size() > 0) {
//            lessonInfo.put(RES_LESSON_CONTENT_LIST, contentList);
//        }
//
//        // LESSON下的应用信息列表
//        List<Map<String, Object>> practiceList = loadLessonPractices(lesson);
//        if (practiceList != null && practiceList.size() > 0) {
//            lessonInfo.put(RES_LESSON_PRACTICE_LIST, practiceList);
//        }
//
//        return lessonInfo;
//    }

    public List<String> loadLessonContent(Object lessonItem) {
        return Collections.emptyList();
    }

    public abstract List loadPressBooks(String press, String subject);

    public abstract Map<String, Object> convertBookInfo(String subject, Object book);

    public abstract List loadClazzLevelBook(Integer clazzLevel);

    public abstract Object loadBook(Long bookId);

    public abstract List loadBookUnitList(Long bookId);

    public abstract Map<String, Object> convertUnitInfo(Object unit);

    public abstract Object loadUnit(Long unitId);

    public abstract List loadUnitLessonList(Long unitId);

    public abstract Map<String, Object> convertLessonInfo(Object lesson);
    //public abstract List<Map<String, Object>> loadLessonPractices(Object lesson);


    protected void addIntoMap(Map<String, Object> dataMap, String key, Object value) {
        if (value == null) {
            dataMap.put(key, "");
        } else {
            dataMap.put(key, value);
        }
    }
}
