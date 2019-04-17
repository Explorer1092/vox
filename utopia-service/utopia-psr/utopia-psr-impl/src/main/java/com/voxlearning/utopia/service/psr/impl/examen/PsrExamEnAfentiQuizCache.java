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

package com.voxlearning.utopia.service.psr.impl.examen;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

@Slf4j
@Named
@Deprecated // 2015.08.10
public class PsrExamEnAfentiQuizCache implements InitializingBean {
// fixme 2015.09.01 之后暂不维护
/*
    @Inject EkCouchbaseDao ekCouchbaseDao;

    @Setter @Getter private Map<Long/ *bookId* /, Map<Long/ *unitId* /, Map<String/ *quizId* /, ExamEnQuizPackageAfenti>>> quizBookMap = new ConcurrentHashMap<>();

    public List<Long> getBooks() {
        List<Long> retList = new ArrayList<>();
        for (Long book : quizBookMap.keySet()) {
            retList.add(book);
        }
        return retList;
    }

    public List<Long> getUnitsByBook(Long bookId) {
        List<Long> retList = new ArrayList<>();
        if (bookId == null || !quizBookMap.containsKey(bookId))
            return retList;
        if (quizBookMap.get(bookId) == null)
            return retList;

        for (Long book : quizBookMap.get(bookId).keySet()) {
            retList.add(book);
        }
        return retList;
    }

    public Map<Long/ *unitId* /, Map<String/ *quizId* /, ExamEnQuizPackageAfenti>> getExamEnQuizByBookId(Long bookId) {
        Map<Long/ *unitId* /, Map<String/ *quizId* /, ExamEnQuizPackageAfenti>> retMap = null;
        if (quizBookMap.containsKey(bookId))
            retMap = quizBookMap.get(bookId);
        else
            retMap = new HashMap<>();

        return retMap;
    }

    public List<ExamEnQuizPackageAfenti> getExamEnQuizByBookIdAndUnitId(Long bookId, Long unitId) {
        if (bookId == null || unitId == null)
            return null;
        List<Long> units = new ArrayList<>();
        units.add(unitId);
        return (getExamEnQuizByBookIdAndUnitId(bookId, units));
    }

    public List<ExamEnQuizPackageAfenti> getExamEnQuizByBookIdAndUnitId(Long bookId, List<Long> unitIds) {
        if (bookId == null || unitIds == null || unitIds.size() <= 0)
            return null;
        if (!quizBookMap.containsKey(bookId))
            return null;

        List<ExamEnQuizPackageAfenti> retQuizs = new ArrayList<>();
        for (Long unit : unitIds) {
            if (quizBookMap.get(bookId).containsKey(unit)) {
                Map<String, ExamEnQuizPackageAfenti> quizMap = quizBookMap.get(bookId).get(unit);
                if (quizMap == null)
                    continue;
                for (ExamEnQuizPackageAfenti tmpQuiz : quizMap.values()) {
                    retQuizs.add(tmpQuiz);
                }
            }
        }

        return retQuizs;
    }

    private void reLoad() {
        quizBookMap.clear();
        Integer count = PsrTools.stringToInt(ekCouchbaseDao.getCouchbaseDataByKey("quizafenti_count"));
        if (count <= 0)
            return;

        for (Integer i = 0; i < count; i++) {
            ExamEnQuizPackageAfenti tmpQuiz = PsrTools.decodeQuziAfentiPackageFromLine(ekCouchbaseDao.getCouchbaseDataByKey("quizafenti_" + i.toString()));
            if (tmpQuiz == null || tmpQuiz.getQuizEidMap() == null || tmpQuiz.getQuizEidMap().size() <= 0)
                continue;
            if (tmpQuiz.getQuizStatus().equals("unopened"))
                continue;
            Map<Long/ *unitId* /, Map<String/ *quizId* /, ExamEnQuizPackageAfenti>> tmpUnitMap = null;
            Map<String/ *quizId* /, ExamEnQuizPackageAfenti> tmpQuizMap = null;
            if ( ! quizBookMap.containsKey(tmpQuiz.getBookId())) {
                tmpUnitMap = new HashMap<>();
                tmpQuizMap = new HashMap<>();
                tmpQuizMap.put(tmpQuiz.getQuizId(), tmpQuiz);
                tmpUnitMap.put(tmpQuiz.getUnitId(), tmpQuizMap);
            } else {
                tmpUnitMap = quizBookMap.get(tmpQuiz.getBookId());
                if (tmpUnitMap == null)
                    tmpUnitMap = new HashMap<>();
                if ( ! tmpUnitMap.containsKey(tmpQuiz.getUnitId())) {
                    // 没有改unitId, 录入进去吧
                    tmpQuizMap = new HashMap<>();
                    tmpQuizMap.put(tmpQuiz.getQuizId(), tmpQuiz);
                    tmpUnitMap.put(tmpQuiz.getUnitId(), tmpQuizMap);
                } else {
                    // 该单元下已经有QuizMap了,把该tmpQuiz 追加进去吧,如果一个单元下有相同的QuizId覆盖吧(bug了)
                    tmpQuizMap = tmpUnitMap.get(tmpQuiz.getUnitId());
                    if (tmpQuizMap == null)
                        tmpQuizMap = new HashMap<>();
                    tmpQuizMap.put(tmpQuiz.getQuizId(), tmpQuiz);
                    tmpUnitMap.put(tmpQuiz.getUnitId(), tmpQuizMap);
                }
            }
            quizBookMap.put(tmpQuiz.getBookId(), tmpUnitMap);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        reLoad();

        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrExamEnAfentiQuizCache-loader") {
            @Override
            public void runSafe() {
                reLoad();
                log.info("PsrExamEnAfentiQuizCache reLoad on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 3600 * 1000, 3600 * 1000);
    }

*/
// fixme 2015.09.01 之后暂不维护

    @Override
    public void afterPropertiesSet() throws Exception {

    }

}

