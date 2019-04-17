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

package com.voxlearning.utopia.service.psr.impl.appen;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.content.api.entity.Book;
import com.voxlearning.utopia.service.content.api.entity.Lesson;
import com.voxlearning.utopia.service.content.api.entity.Sentence;
import com.voxlearning.utopia.service.content.api.entity.Unit;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.psr.entity.PsrBookPersistence;
import com.voxlearning.utopia.service.psr.entity.PsrLessonPersistence;
import com.voxlearning.utopia.service.psr.entity.PsrUnitPersistence;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Named
public class PsrBooksSentences implements InitializingBean {

    @Inject private EnglishContentLoaderClient englishContentLoaderClient;
    @Inject private PsrAppEnEksFilter psrAppEnEksFilter;

    // books
    protected Map<Long/*bookid*/, PsrBookPersistence> bookPersistenceMap = new ConcurrentHashMap<>();

    public PsrBookPersistence getBookPersistenceByBookId(Long bookId) {
        if (bookPersistenceMap.containsKey(bookId))
            return bookPersistenceMap.get(bookId);

        // 查库
        Book book = englishContentLoaderClient.loadEnglishBook(bookId);
        if (book == null) {
            log.error("PsrBooksSentences getBookPersistenceByBookId find bookid err : " + bookId.toString());
            return null;
        }

        // 结果中的Unit已经按Rank 排序
        List<Unit> unitList = englishContentLoaderClient.loadEnglishBookUnits(bookId);
        if (unitList == null) {
            log.error("PsrBooksSentences getBookPersistenceByBookId not found any unit in book :" + bookId.toString());
            return null;
        }

        PsrBookPersistence psrBookPersistence = new PsrBookPersistence();
        psrBookPersistence.setBookId(bookId);
        psrBookPersistence.setEname(book.getEname());
        psrBookPersistence.setCname(book.getCname());
        psrBookPersistence.setBookStructure(book.getBookStructure());

        Map<Long, PsrUnitPersistence> bookMapTmp = new LinkedHashMap<>();
        Map<Long/*unitid*/, Long/*groupid*/> unitToGroupIdMap = new HashMap<>();  // bookStructure==1
        Map<Long/*groupid*/, List<Long/*unitid*/>> groupIdToUnitIdsMap = new HashMap<>();  // bookStructure==1

        for (Unit unit : unitList) {
            List<Lesson> lessonList = englishContentLoaderClient.loadEnglishUnitLessons(unit.getId());
            if (lessonList == null)
                continue;

            PsrUnitPersistence psrUnitPersistence = new PsrUnitPersistence();
            psrUnitPersistence.setRank(unit.getRank());
            if (book.getBookStructure() == 1) {
                // 教材结构不同 所以特殊处理 以 groupId 和 group_cname group_ename 初始化
                psrUnitPersistence.setUnitId(unit.getGroupId());
                psrUnitPersistence.setEname(unit.getGroupEname());
                psrUnitPersistence.setCname(unit.getGroupCname());
            } else {
                psrUnitPersistence.setUnitId(unit.getId());
                psrUnitPersistence.setEname(unit.getEname());
                psrUnitPersistence.setCname(unit.getCname());
            }

            if (!unitToGroupIdMap.containsKey(unit.getId()))
                unitToGroupIdMap.put(unit.getId(), unit.getGroupId());

            List<Long> unitsTmp = null;
            if (groupIdToUnitIdsMap.containsKey(unit.getGroupId()))
                unitsTmp = groupIdToUnitIdsMap.get(unit.getGroupId());
            if (unitsTmp == null)
                unitsTmp = new ArrayList<>();
            unitsTmp.add(unit.getId());

            groupIdToUnitIdsMap.put(unit.getGroupId(), unitsTmp);

            Map<Long, PsrLessonPersistence> unitMapTmp = new LinkedHashMap<>();
            Collection<Long> lessonIds = lessonList.stream()
                    .map(Lesson::getId)
                    .filter(t -> t != null)
                    .collect(Collectors.toCollection(LinkedHashSet::new));
            Map<Long, List<Sentence>> lessonSentences = englishContentLoaderClient.loadEnglishLessonSentences(lessonIds);

            for (Lesson lesson : lessonList) {
                List<Sentence> sentenceList = lessonSentences.get(lesson.getId());
                if (sentenceList == null)
                    continue;
                PsrLessonPersistence psrLessonPersistence = new PsrLessonPersistence();
                psrLessonPersistence.setLessonId(lesson.getId());
                psrLessonPersistence.setEname(lesson.getEname());
                psrLessonPersistence.setCname(lesson.getCname());

                List<String> sentenceListTmp = new ArrayList<>();
                for (Sentence sentence : sentenceList) {
                    if (sentence == null) continue;
                    if (sentence.getType() != 1 && sentence.getType() != 11)   // TYPE=1 AND TYPE=11 IS WORD
                        continue;
                    // 没有中文和没有音频的不加入备选列表
                    if (StringUtils.isBlank(sentence.getCnText()) || StringUtils.isBlank(sentence.getWaveUri()))
                        continue;
                    // 没有图片示意的不加入备选列表
                    if (!psrAppEnEksFilter.isValid(sentence.getEnText()))
                        continue;
                    // type = 1 为 单词, 并且格式化 加上word#
                    sentenceListTmp.add("word#" + sentence.getEnText());
                }
                psrLessonPersistence.setSentences(sentenceListTmp);
                unitMapTmp.put(lesson.getId(), psrLessonPersistence);
            }
            psrUnitPersistence.setLessonPersistenceMap(unitMapTmp);

            if (book.getBookStructure() == 1) {
                // 教材结构不同 所以特殊处理 如果已经存在改组的 Persistence 信息 则合并
                if (!bookMapTmp.containsKey(unit.getGroupId()))
                    bookMapTmp.put(unit.getGroupId(), psrUnitPersistence);
                else // 合并同一个 groupId 对应的多个 unit 信息
                    bookMapTmp.get(unit.getGroupId()).getLessonPersistenceMap().putAll(psrUnitPersistence.getLessonPersistenceMap());
            } else {
                bookMapTmp.put(unit.getId(), psrUnitPersistence);
            }
        }

        if (book.getBookStructure() == 1)
            psrBookPersistence.setUnitGroupIdPersistenceMap(bookMapTmp);
        else
            psrBookPersistence.setUnitPersistenceMap(bookMapTmp);

        psrBookPersistence.setUnitToGroupIdMap(unitToGroupIdMap);
        psrBookPersistence.setGroupIdToUnitIdsMap(groupIdToUnitIdsMap);

        if (bookPersistenceMap.size() < 1000)
            bookPersistenceMap.put(bookId, psrBookPersistence);
        else
            log.error("getBookPersistenceByBookId buffer full: " + bookPersistenceMap.size());

        return psrBookPersistence;
    }

    public List<Long> getUnitsByBookId(Long bookId) {
        List<Long> retList = new ArrayList<>();
        PsrBookPersistence psrBookPersistence = getBookPersistenceByBookId(bookId);
        if (psrBookPersistence == null)
            return retList;

        Map<Long, PsrUnitPersistence> psrUnitPersistenceMap = psrBookPersistence.getUnitMap();
        if (psrUnitPersistenceMap == null)
            return retList;

        retList.addAll(
                psrUnitPersistenceMap.entrySet()
                        .stream()
                        .map(Map.Entry<Long, PsrUnitPersistence>::getKey)
                        .collect(Collectors.toList())
        );

        return retList;
    }

    public Map<Long, List<String>> getUnitsSentenceByBookId(Long bookId) {
        Map<Long, List<String>> retMap = new LinkedHashMap<>();

        PsrBookPersistence psrBookPersistence = getBookPersistenceByBookId(bookId);
        if (psrBookPersistence == null)
            return retMap;

        Map<Long, PsrUnitPersistence> psrUnitPersistenceMap = psrBookPersistence.getUnitMap();
        if (psrUnitPersistenceMap == null)
            return retMap;

        for (Map.Entry<Long, PsrUnitPersistence> entry : psrUnitPersistenceMap.entrySet()) {
            PsrUnitPersistence psrUnitPersistence = entry.getValue();
            if (psrUnitPersistence == null)
                continue;
            Map<Long, PsrLessonPersistence> psrLessonPersistenceMap = psrUnitPersistence.getLessonPersistenceMap();
            if (psrLessonPersistenceMap == null)
                continue;

            List<String> tmpList = new ArrayList<>();

            for (Map.Entry<Long, PsrLessonPersistence> entry1 : psrLessonPersistenceMap.entrySet()) {
                PsrLessonPersistence psrLessonPersistence = entry1.getValue();
                if (psrLessonPersistence == null)
                    continue;

                List<String> pList = psrLessonPersistence.getSentences();
                if (pList != null)
                    tmpList.addAll(pList);
            }

            retMap.put(entry.getKey(), tmpList);
        }

        return retMap;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrBooksSentences-Loader") {
            @Override
            public void runSafe() {
                bookPersistenceMap.clear();
                log.info("PsrBooksSentences map clear on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60 * 60 * 1000, 60 * 60 * 1000);
    }
}


