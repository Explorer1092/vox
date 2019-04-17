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

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import lombok.Getter;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Named
@Deprecated
public class PsrMathBookLessonPoints implements InitializingBean {
    @Inject private EkCouchbaseDao ekCouchbaseDao;

    // press+book+unit+lesson => pointList,且pointList是有序的
    private Map<String, List<String>> mapLessonPoints = new ConcurrentHashMap<>();

    public boolean isMapLessonPointsNull() {
        return (mapLessonPoints == null);
    }

    public List<String> getPointsByBookUnitLesson(String key) {
        if (StringUtils.isEmpty(key))
            return null;

        key = key.trim();
        if (mapLessonPoints.containsKey(key)) {
            return mapLessonPoints.get(key);
        }
        if (mapLessonPoints.size() > 0) {
            // 已经查过库了,如果没有就构造一个空列表
            List<String> strList = new ArrayList<>();
            mapLessonPoints.put(key, strList);
            return strList;
        }

        // 从couchbase数据库中取
        if (ekCouchbaseDao == null) {
            log.error("PsrEkRegions ekCouchbaseDao is null");
            return null;
        }

        String strLine = ekCouchbaseDao.getCouchbaseDataByKey("mathbook_count");
        int count = PsrTools.stringToInt(strLine);

        String strKey = "";
        for (Integer i = 0; i < count; i++) {
            strLine = ekCouchbaseDao.getCouchbaseDataByKey("mathbook_" + i.toString());
            LineItem item = parseLine(strLine);
            if (item == null)
                continue;
            strKey = item.getPress() + item.getBook() + item.getUnit() + item.getLesson();
            if (StringUtils.isEmpty(strKey))
                continue;
            List<String> strList = null;
            if (StringUtils.isEmpty(item.getPoint())) {
                strList = new ArrayList<>();
                mapLessonPoints.put(strKey, strList);
                continue;
            }
            if (mapLessonPoints.containsKey(strKey)) {
                strList = mapLessonPoints.get(strKey);
                if (!strList.contains(item.getPoint()))
                    strList.add(item.getPoint());
            } else {
                strList = new ArrayList<>();
                strList.add(item.getPoint());
                mapLessonPoints.put(strKey, strList);
            }
        }

        if (mapLessonPoints.containsKey(key)) {
            return mapLessonPoints.get(key);
        }
        // 如果数据库中没有此key 则构造一个空的value 放入map,避免错误的查询 对数据库造成的影响
        List<String> strList = new ArrayList<>();
        mapLessonPoints.put(key, strList);

        return strList;
    }

    public LineItem parseLine(String strLine) {
        if (StringUtils.isEmpty(strLine))
            return null;
        String[] sArr = strLine.split(",");
        if (sArr.length < 3)
            return null;

        LineItem retItem = new LineItem();
        retItem.setPress(sArr[0]);
        retItem.setBook(sArr[1]);
        retItem.setUnit(sArr[2]);

        if (sArr.length >= 4)
            retItem.setLesson(sArr[3]);
        if (sArr.length >= 5)
            retItem.setPoint(sArr[4]);

        return retItem;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrMathBookLessonPoints-Loader") {
            @Override
            public void runSafe() {
                mapLessonPoints.clear();
                log.info("PsrMathBookLessonPoints clearMap on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60*60*1000, 60*60*1000);
    }
}
@Deprecated
class LineItem {
    @Getter @Setter String press;
    @Getter @Setter String book;
    @Getter @Setter String unit;
    @Getter @Setter String lesson;
    @Getter @Setter String point;
}
