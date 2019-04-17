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
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.util.MiscUtils;
import com.voxlearning.utopia.service.content.api.constant.BookPress;
import com.voxlearning.utopia.service.content.api.constant.BookType;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.Lesson;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.content.api.mapper.ExFollowerPracticeType;
import com.voxlearning.utopia.service.content.api.mapper.ExLesson;
import com.voxlearning.washington.controller.open.v1.content.AbstractContentLoaderWrapper;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.washington.controller.open.v1.content.ContentApiConstants.*;

/**
 * Created by Alex on 14-10-16.
 */
@Named
public class EnglishContentLoaderWrapper extends AbstractContentLoaderWrapper {

    @Override
    public List<String> loadPressByBookType(Integer bookType) {
        BookType bt = BookType.safeParse(bookType);
        if (bt == BookType.UNKNOWN) {
            return Collections.emptyList();
        }
        return englishContentLoaderClient.loadEnglishBooks()
                .originalLocationsAsList()
                .stream()
                .filter(t -> !t.isDisabled())
                .filter(t -> t.getBookType() == bt.getKey())
                .filter(t -> t.getPress() != null)
                .map(Book.Location::getPress)
                .collect(Collectors.toSet())
                .stream()
                .sorted(String::compareTo)
                .collect(Collectors.toList());
    }

    @Override
    public List loadPressBooks(String press, String subject) {
        return englishContentLoaderClient.loadEnglishBooks()
                .enabled()
                .online()
                .filter(t -> StringUtils.equals(t.getPress(), press))
                .clazzLevel_termType_ASC()
                .toList();
    }

    @Override
    public List loadClazzLevelBook(Integer clazzLevel) {
        return englishContentLoaderClient.loadEnglishBooks()
                .enabled()
                .online()
                .filter(t -> t.getBookType() == BookType.PRIMARY.getKey())
                .filter(t -> Objects.equals(t.getClazzLevel(), clazzLevel))
                .termType_ASC()
                .toList();
    }

    @Override
    public Object loadBook(Long bookId) {
        return englishContentLoaderClient.loadEnglishBook(bookId);
    }

    @Override
    public List loadBookUnitList(Long bookId) {
        return englishContentLoaderClient.loadEnglishBookUnits(bookId);
    }

    @Override
    public Object loadUnit(Long unitId) {
        return englishContentLoaderClient.loadEnglishUnit(unitId);
    }

    @Override
    public List loadUnitLessonList(Long unitId) {
        return englishContentLoaderClient.loadEnglishUnitLessons(unitId);
    }

    //@Override
    public List<Map<String, Object>> loadUnitLessons(Long unitId) {
        List<ExLesson> lessons = englishContentLoaderClient.loadUnitExLessons(unitId);
        if (lessons == null || lessons.size() == 0) {
            return Collections.emptyList();
        }


        List<Map<String, Object>> retLessonList = new ArrayList<>();
        for (ExLesson exLesson : lessons) {
            // LESSON 基本信息
            Map<String, Object> lessonInfo = convertLessonInfo(exLesson);

            // LESSON的内容信息，英语是SENTENCE信息
            List<String> contentList = new ArrayList<>();
            for (Sentence sentence : englishContentLoaderClient.loadEnglishLessonSentences(exLesson.getId())) {
                contentList.add(sentence.getEnText());
            }
            if (contentList.size() > 0) {
                lessonInfo.put(RES_LESSON_CONTENT_LIST, contentList);
            }

            // 练习类型
            List<ExFollowerPracticeType> practiceTypeList = exLesson.getPracticeTypesMap();
            if (practiceTypeList != null && practiceTypeList.size() > 0) {
                List<Map<String, Object>> lessonPracticeList = new ArrayList<>();
                for (ExFollowerPracticeType practiceType : practiceTypeList) {
                    Map<String, Object> practiceItem = new LinkedHashMap<>();
                    //addIntoMap(practiceItem, "", practiceType.g);
                }

            }


        }

        return retLessonList;
    }

    @Override
    public Map<String, Object> convertBookInfo(String subject, Object book) {
        if (book == null || !(book instanceof Book)) {
            return null;
        }

        Book bookItem = (Book) book;
        Map<String, Object> bookInfo = new LinkedHashMap<>();
        addIntoMap(bookInfo, RES_BOOK_ID, bookItem.getId());
        addIntoMap(bookInfo, RES_BOOK_SUBJECT, subject);
        addIntoMap(bookInfo, RES_BOOK_TYPE, bookItem.getBookType());
        addIntoMap(bookInfo, RES_BOOK_CLASS_LEVEL, bookItem.getClassLevel());
        addIntoMap(bookInfo, RES_BOOK_START_CLASS_LEVEL, bookItem.getStartClassLevel());
        addIntoMap(bookInfo, RES_BOOK_TERM, bookItem.getTermType());
        addIntoMap(bookInfo, RES_BOOK_PRESS, bookItem.getPress());
        addIntoMap(bookInfo, RES_CNAME, bookItem.getCname());
        addIntoMap(bookInfo, RES_ENAME, bookItem.getEname());
        addIntoMap(bookInfo, RES_BOOK_LATEST_VERSION, bookItem.getLatestVersion());
        addIntoMap(bookInfo, RES_BOOK_OPEN_EXAM, bookItem.getOpenExam());
        BookPress bookPress = BookPress.getBySubjectAndPress(Subject.ENGLISH, bookItem.getPress());
        if (bookPress != null) {
            addIntoMap(bookInfo, RES_BOOK_COLOR, bookPress.getColor());
            addIntoMap(bookInfo, RES_BOOK_VIEW_CONTENT, bookPress.getViewContent());
        }
        return bookInfo;
    }

    @Override
    public Map<String, Object> convertUnitInfo(Object unit) {
        if (unit == null || !(unit instanceof Unit)) {
            return Collections.emptyMap();
        }

        Unit unitItem = (Unit) unit;
        Map<String, Object> unitInfo = new LinkedHashMap<>();
        addIntoMap(unitInfo, RES_BOOK_ID, unitItem.getBookId());
        addIntoMap(unitInfo, RES_UNIT_ID, unitItem.getId());
        addIntoMap(unitInfo, RES_CNAME, unitItem.getCname());
        addIntoMap(unitInfo, RES_ENAME, unitItem.getEname());
        addIntoMap(unitInfo, RES_RANK, unitItem.getRank());
        addIntoMap(unitInfo, RES_GROUP_CNAME, unitItem.getGroupCname());
        addIntoMap(unitInfo, RES_GROUP_ENAME, unitItem.getGroupEname());

        return unitInfo;
    }

    @Override
    public Map<String, Object> convertLessonInfo(Object lesson) {
        if (lesson == null || !(lesson instanceof Lesson)) {
            return null;
        }

        Lesson lessonItem = (Lesson) lesson;
        Map<String, Object> lessonInfo = new LinkedHashMap<>();
        addIntoMap(lessonInfo, RES_UNIT_ID, lessonItem.getUnitId());
        addIntoMap(lessonInfo, RES_LESSON_ID, lessonItem.getId());
        addIntoMap(lessonInfo, RES_CNAME, lessonItem.getCname());
        addIntoMap(lessonInfo, RES_ENAME, lessonItem.getEname());
        addIntoMap(lessonInfo, RES_RANK, lessonItem.getRankValue());
        addIntoMap(lessonInfo, RES_ROLE_COUNT, lessonItem.getRoleCount());
        addIntoMap(lessonInfo, RES_HAS_DIALOG, lessonItem.hasDialog());

        return lessonInfo;
    }

    @Override
    public List<String> loadLessonContent(Object lesson) {
        if (lesson == null || !(lesson instanceof Lesson)) {
            return Collections.emptyList();
        }

        Lesson lessonItem = (Lesson) lesson;

        List<Sentence> sentenceList = englishContentLoaderClient.loadEnglishLessonSentences(lessonItem.getId());
        if (sentenceList == null || sentenceList.size() == 0) {
            return Collections.emptyList();
        }

        List<String> retSentenceList = new ArrayList<>();
        for (Sentence sentence : sentenceList) {
            retSentenceList.add(sentence.getEnText());
        }

        return retSentenceList;
    }

    //@Override
    public List<Map<String, Object>> loadLessonPractices(Object lesson) {
        if (lesson == null || !(lesson instanceof Lesson)) {
            return Collections.emptyList();
        }
        Lesson lessonItem = (Lesson) lesson;


        return Collections.emptyList();
    }

    @Override
    public List<Map<String, Object>> groupUnitStructure(List<Map<String, Object>> unitList) {
        //单元分组  新结构
        List<Map<String, Object>> groupList = new ArrayList<>();
        Map<String, List<Map<String, Object>>> tempMap = new HashMap<>();
        for (Map<String, Object> unitMap : unitList) {
            Object cname = unitMap.get(RES_GROUP_CNAME);
            if (cname != null && StringUtils.isNotBlank(cname.toString())) {
                if (tempMap.containsKey(cname.toString())) {
                    tempMap.get(cname.toString()).add(unitMap);
                } else {
                    List<Map<String, Object>> unitMapList = new ArrayList<>();
                    unitMapList.add(unitMap);
                    tempMap.put(cname.toString(), unitMapList);
                }
            }
        }
        for (Map.Entry<String, List<Map<String, Object>>> entry : tempMap.entrySet()) {
            Map<String, Object> dataMap = new HashMap<>();
            dataMap.put("groupCname", entry.getKey());
            dataMap.put("groupEname", MiscUtils.firstElement(entry.getValue()).get(RES_GROUP_ENAME));
            dataMap.put("rank", MiscUtils.firstElement(entry.getValue()).get(RES_RANK));
            dataMap.put("units", entry.getValue());
            groupList.add(dataMap);
        }
        Collections.sort(groupList, new Comparator<Map<String, Object>>() {
            @Override
            public int compare(Map<String, Object> o1, Map<String, Object> o2) {
                return Integer.compare(ConversionUtils.toInt(o1.get("rank")), ConversionUtils.toInt(o2.get("rank")));
            }
        });
        return groupList;
    }
}
