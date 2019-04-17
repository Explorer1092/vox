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

package com.voxlearning.utopia.service.psr.impl.appmath;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.content.api.entity.*;
import com.voxlearning.utopia.service.content.consumer.MathContentLoaderClient;
import com.voxlearning.utopia.service.psr.entity.PsrMathBookPersistence;
import com.voxlearning.utopia.service.psr.entity.PsrMathLessonPersistence;
import com.voxlearning.utopia.service.psr.entity.PsrMathPointPersistence;
import com.voxlearning.utopia.service.psr.entity.PsrMathUnitPersistence;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

@Slf4j
@Named
public class PsrMathBooksPoints implements InitializingBean {

    @Inject private MathContentLoaderClient mathContentLoaderClient;
    @Inject private EkCouchbaseDao ekCouchbaseDao;

    // 存储所有point
    @Getter private Map<Long, MathPoint> mathPointMap;   // pointid -> pointInfo

    // 获取book对应的press,以便查找 同press下其它的课本
    protected Map<Long, String> mathBookPressMap;

    // press -> books
    // 获取同一个press下 book的学习顺序
    protected Map<String, List<Long>> mathPressBooksMap;

    // 获取某一book下的unit lesson point信息，并把point按学习的先后顺序标号
    protected Map<Long, PsrMathBookPersistence> mathBookPersistenceMap;

    // 数学知识点对应的score,根据score归一化随机知识点
    protected Map<Long/*pointid*/, Integer> mathPointScores;

    public PsrMathBooksPoints() {
        mathPointMap = new ConcurrentHashMap<>();
        mathBookPressMap = new ConcurrentHashMap<>();
        mathPressBooksMap = new ConcurrentHashMap<>();
        mathBookPersistenceMap = new ConcurrentHashMap<>();
        mathPointScores = new ConcurrentHashMap<>();
    }

    public Integer getScoreByPointId(Long pointId) {
        if (null == pointId)
            return 1;

        if (mathPointScores.size() <= 0) {
            log.info("initMathPointScores");
            initMathPointScores();
        }

        if (mathPointScores.containsKey(pointId))
            return mathPointScores.get(pointId);

        return 1;
    }

    public void initMathPointScores() {
        String strKey = "app_math_kp_socre";
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey); // kpid:score;kpid:score
        if (StringUtils.isBlank(strValue))
            return;

        String[] aValue = strValue.split(";");
        if (aValue.length <= 0) {
            log.error("get math_kp_score error ex:" + aValue.length + " % 2 != 0");
            return;
        }
        for (String kp : aValue) {
            String[] aKp = kp.split(":");
            if (aKp.length != 2)
                continue;
            Long pointId = PsrTools.stringToLong(aKp[0]);
            Integer score = PsrTools.stringToInt(aKp[1]);
            mathPointScores.put(pointId, score);
        }
    }

    public String getPointNameByPointId(Long pointId) {
        if (mathPointMap == null || !mathPointMap.containsKey(pointId))
            return null;
        return "point#" + mathPointMap.get(pointId).getPointName();
    }

    public String getPressByBookId(Long bookId) {

        /*
         * 查找该book对应的press
         */
        if (!mathBookPressMap.containsKey(bookId)) {
            MathBook tempMathBook = mathContentLoaderClient.loadMathBook(bookId);
            if (tempMathBook == null) {
                log.error("PsrMathBooksPoints not found book: " + bookId.toString());
                return null;
            }
            mathBookPressMap.put(bookId, tempMathBook.getPress());
        }

        return mathBookPressMap.get(bookId);
    }

    /*
     * 获取当前课本 及 之后的课本
     */
    public List<Long> getMoreBooksByBookId(Long bookId) {
        List<Long> retList = new ArrayList<>();
        retList.add(bookId);

        String press = getPressByBookId(bookId);
        if (StringUtils.isEmpty(press))
            return retList;

        List<Long> tempList = null;
        int index = 0;

        // 从cache中获取
        if (mathPressBooksMap.containsKey(press)) {
            tempList = mathPressBooksMap.get(press);
            if (tempList != null) {
                index = tempList.indexOf(bookId);
                for (int i = index + 1; index >= 0 && i < tempList.size(); i++) {
                    retList.add(tempList.get(i));
                }
                return retList;
            }
        }

        // 从数据库中获取
        List<MathBook> mathBooks = mathContentLoaderClient.loadMathBooks()
                .enabled()
                .filter(t -> StringUtils.equals(t.getPress(), press))
                .clazzLevel_termType_ASC()
                .toList();
        if (mathBooks == null || mathBooks.isEmpty()) {
            log.error("PsrMathBooksPoints findByPress return null press:" + press);
            return retList;
        }

        List<Long> books = new ArrayList<>();
        for (MathBook book : mathBooks) {
            books.add(book.getId());
        }
        mathPressBooksMap.put(press, books);

        index = books.indexOf(bookId);
        if (index < 0)
            return retList;
        for (int i = index + 1; index >= 0 && i < books.size(); i++) {
            retList.add(books.get(i));
        }

        return retList;
    }

    /*
     * 获取当前课本UnitIds
     */
    public List<Long> getUnitIdsByBookId(Long bookId) {
        PsrMathBookPersistence psrMathBookPersistence = getBookPersistenceByBookId(bookId);
        if (psrMathBookPersistence == null)
            return null;
        return psrMathBookPersistence.getMathUnitList();
    }

    /*
     * 获取当前课本LessonIds
     */
    public List<Long> getLessonIdsByBookId(Long bookId) {
        PsrMathBookPersistence psrMathBookPersistence = getBookPersistenceByBookId(bookId);
        if (psrMathBookPersistence == null)
            return null;
        List<Long> retList = new ArrayList<>();

        Map<Long, PsrMathUnitPersistence> psrMathUnitPersistenceMap = psrMathBookPersistence.getMathUnitPersistenceMap();
        for (Long unit : psrMathBookPersistence.getMathUnitList()) {
            if (psrMathUnitPersistenceMap.containsKey(unit))
                retList.addAll(psrMathUnitPersistenceMap.get(unit).getMathLessonList());
        }
        return retList;
    }

    public List<String> getPointsByBookId(Long bookId) {
        List<PsrMathPointPersistence> points = getPointsInfoByBookId(bookId);
        if (points == null || points.size() <= 0)
            return null;

        return points.stream().map(PsrMathPointPersistence::getPointName).collect(Collectors.toList());
    }

    /*
     * 获取当前课本知识点
     */
    public List<PsrMathPointPersistence> getPointsInfoByBookId(Long bookId) {
        PsrMathBookPersistence psrMathBookPersistence = getBookPersistenceByBookId(bookId);
        if (psrMathBookPersistence == null)
            return null;
        Map<Long, PsrMathPointPersistence> retMap = new HashMap<>();
        Map<Long, PsrMathUnitPersistence> psrMathUnitPersistenceMap = psrMathBookPersistence.getMathUnitPersistenceMap();
        for (Long unit : psrMathBookPersistence.getMathUnitList()) {
            if (psrMathUnitPersistenceMap.containsKey(unit)) {
                PsrMathUnitPersistence psrMathUnitPersistence = psrMathUnitPersistenceMap.get(unit);
                Map<Long, PsrMathLessonPersistence> psrMathLessonPersistenceMap = psrMathUnitPersistence.getMathLessonPersistenceMap();
                for (Long lesson : psrMathUnitPersistence.getMathLessonList()) {
                    if (psrMathLessonPersistenceMap.containsKey(lesson)) {
                        PsrMathLessonPersistence psrMathLessonPersistence = psrMathLessonPersistenceMap.get(lesson);

                        // 直接用 mysql中查出来的 知识点学习顺序
                        for (PsrMathPointPersistence point : psrMathLessonPersistence.getPoints()) {
                            PsrMathPointPersistence item = new PsrMathPointPersistence();
                            item.setPointId(point.getPointId());
                            item.setPointName("point#" + point.getPointName());
                            if (!retMap.containsKey(point.getPointId()))
                                retMap.put(point.getPointId(), item);
                        }
                    }
                }
            }
        }
        return retMap.values().stream().collect(Collectors.toList());
    }

    /*
     * 获取当前课本知识点 及 之后的课本知识点 组成知识点备选集合
     */
    public List<String> getMorePointsByBookId(Long bookId) {
        List<String> retPoints = new ArrayList<>();
        List<Long> books = getMoreBooksByBookId(bookId);

        for (Long book : books) {
            List<String> tempList = getPointsByBookId(book);
            if (tempList == null)
                continue;
            retPoints.addAll(tempList);
        }
        return retPoints;
    }

    public PsrMathBookPersistence getBookPersistenceByBookId(Long bookId) {
        if (mathBookPersistenceMap.containsKey(bookId))
            return mathBookPersistenceMap.get(bookId);

        MathBook mathBook = mathContentLoaderClient.loadMathBook(bookId);
        if (mathBook == null) {
            return null;
        }

        PsrMathBookPersistence psrMathBookPersistence = new PsrMathBookPersistence();
        psrMathBookPersistence.setBookId(bookId);
        psrMathBookPersistence.setPress(mathBook.getPress());
        psrMathBookPersistence.setTerm(mathBook.getTerm());
        psrMathBookPersistence.setCname(mathBook.getCname());

        //  -------------    1
        Map<Long, PsrMathUnitPersistence> psrMathUnitPersistenceMap = psrMathBookPersistence.getMathUnitPersistenceMap();
        List<Long> psrMathUnitList = psrMathBookPersistence.getMathUnitList();

        // 获取 units
        List<MathUnit> mathUnits = mathContentLoaderClient.loadMathBookUnits(bookId);
        if (CollectionUtils.isEmpty(mathUnits)) {
            log.error("PsrMathBooksPoints find MathUnit by bookId err: " + bookId.toString());
            return psrMathBookPersistence;
        }
        mathUnits = new LinkedList<>(mathUnits);
        //按照单元rank顺序排序
        Collections.sort(mathUnits, new Comparator<MathUnit>() {
            @Override
            public int compare(MathUnit o1, MathUnit o2) {
                return o1.getRank().compareTo(o2.getRank());
            }
        });

        if (mathPointMap.size() <= 0)
            getAllPointsFromDatabase();

        // 获取lessons
        for (MathUnit unit : mathUnits) {
            List<MathLesson> mathLessons = mathContentLoaderClient.loadMathUnitLessons(unit.getId());
            if (CollectionUtils.isEmpty(mathLessons)) {
                log.error("PsrMathBooksPoints find MathLesson by UnitId ex: " + unit.getId().toString());
                continue;
            }
            mathLessons = new LinkedList<>(mathLessons);
            Collections.sort(mathLessons, new Comparator<MathLesson>() {
                @Override
                public int compare(MathLesson o1, MathLesson o2) {
                    return o1.getRank().compareTo(o2.getRank());
                }
            });

            // lesson
            PsrMathUnitPersistence psrMathUnitPersistence = new PsrMathUnitPersistence();
            psrMathUnitPersistence.setUnitId(unit.getId());
            psrMathUnitPersistence.setCname(unit.getCname());
            psrMathUnitPersistence.setRank(unit.getRank());

            //  -------------    2
            Map<Long, PsrMathLessonPersistence> psrMathLessonPersistenceMap = psrMathUnitPersistence.getMathLessonPersistenceMap();
            List<Long> mathLessonList = psrMathUnitPersistence.getMathLessonList();
            // 获取 lesson -> points
            for (MathLesson mathLesson : mathLessons) {
                List<MathLessonPointRef> mathLessonPointRefs = mathContentLoaderClient.loadMathLessonPointRefs(mathLesson.getId());

                // 根据lesson 可能取不到 point
                if (CollectionUtils.isEmpty(mathLessonPointRefs)) {
                    continue;
                }

                // 匹配lesson 和 points
                List<PsrMathPointPersistence> psrMathPointPersistences = new ArrayList<>();
                for (MathLessonPointRef mathLessonPointRef : mathLessonPointRefs) {
                    if (mathPointMap.containsKey(mathLessonPointRef.getPointId())) {
                        MathPoint mathPoint = mathPointMap.get(mathLessonPointRef.getPointId());
                        // 1. 非计算类的point不推荐
                        // 2. app_list不适合熊出没(appId=1)的知识点不推荐(数学新增加的知识点可能导致psr推荐出问题:找不到对应知识点的题型)
                        if (!mathPoint.isCalculateMathPoint() || !mathPoint.containApp(1L))
                            continue;

                        PsrMathPointPersistence psrMathPointPersistence = new PsrMathPointPersistence();
                        psrMathPointPersistence.setPointId(mathPoint.getId());
                        psrMathPointPersistence.setPointName(mathPoint.getPointName());

                        psrMathPointPersistences.add(psrMathPointPersistence);
                    } else {
                        log.error("PsrMathBooksPoints not found MathPoints by pointId: " + mathLessonPointRef.getPointId());
                    }
                }

                //  -------------    3
                PsrMathLessonPersistence psrMathLessonPersistence = new PsrMathLessonPersistence();
                psrMathLessonPersistence.setLessonId(mathLesson.getId());
                psrMathLessonPersistence.setCname(mathLesson.getCname());
                psrMathLessonPersistence.setPoints(psrMathPointPersistences);

                psrMathLessonPersistenceMap.put(mathLesson.getId(), psrMathLessonPersistence);

                mathLessonList.add(mathLesson.getId());
            }

            // unit
            psrMathUnitPersistenceMap.put(unit.getId(), psrMathUnitPersistence);
            psrMathUnitList.add(unit.getId());
        }

        psrMathBookPersistence.setMathUnitPersistenceMap(psrMathUnitPersistenceMap);
        psrMathBookPersistence.setMathUnitList(psrMathUnitList);

        // add to map
        mathBookPersistenceMap.put(bookId, psrMathBookPersistence);

        return psrMathBookPersistence;
    }

    public void getAllPointsFromDatabase() {
        // 获取所有point
        List<MathPoint> mathPoints = new LinkedList<>(mathContentLoaderClient.loadMathPoints().values());
        if (CollectionUtils.isEmpty(mathPoints)) {
            log.error("PsrMathBooksPoints find all MathPoints err");
            return;
        }

        for (MathPoint point : mathPoints) {
            mathPointMap.put(point.getId(), point);
        }
    }

    private void clearMap() {
        mathPointMap.clear();
        mathBookPressMap.clear();
        mathPressBooksMap.clear();
        mathBookPersistenceMap.clear();
        mathPointScores.clear();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrMathBooksPoints-Loader") {
            @Override
            public void runSafe() {
                clearMap();
                log.info("PsrMathBooksPoints clearMap on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60 * 60 * 1000, 60 * 60 * 1000);
    }
}



