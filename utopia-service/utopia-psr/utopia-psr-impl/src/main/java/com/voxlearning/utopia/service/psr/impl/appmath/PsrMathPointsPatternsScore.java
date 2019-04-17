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

import com.voxlearning.alps.annotation.common.KeyValuePair;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Named
public class PsrMathPointsPatternsScore implements InitializingBean {

    @Inject private EkCouchbaseDao ekCouchbaseDao;

    private Map<String/*point*/, Map<String/*pattern*/, KeyValuePair<Integer, Double>>> mathPointPatternScores;

    public PsrMathPointsPatternsScore() {
        mathPointPatternScores = new ConcurrentHashMap<>();
    }

    // pointName : "point#一个因数中间有0的乘法（竖式运算）"
    public Map<String/*pattern*/, KeyValuePair<Integer, Double>> getScoreByPointName(String pointName,String userBucket) {
        if (StringUtils.isBlank(pointName))
            return Collections.emptyMap();

        if (mathPointPatternScores.containsKey(userBucket + "_" + pointName))
            return mathPointPatternScores.get(userBucket + "_" + pointName);

        Map<String, KeyValuePair<Integer, Double>> retMap = getPointsScoresFromCouchbaseByPointName(pointName,userBucket);
        if (MapUtils.isEmpty(retMap))
            return Collections.emptyMap();

        mathPointPatternScores.put(userBucket + "_" + pointName, retMap);

        return retMap;
    }

    // 1 \t 一个因数中间有0的乘法（竖式运算）,243,2.090093161677801E-4;一个因数中间有0的乘法（竖式运算）,243,2.090093161677801E-4
    private Map<String/*pattern*/, KeyValuePair<Integer, Double>> getPointsScoresFromCouchbaseByPointName(String pointName,String userBucket) {
        if (StringUtils.isBlank(pointName))
            return Collections.emptyMap();

        String strKey = /*"appmatheketscore_"*/ "appmatheketscore"+userBucket+"_" + pointName;
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);
        if (StringUtils.isBlank(strValue))
            return Collections.emptyMap();

        String[] verValue = strValue.split("\t");
        if (verValue.length < 2) {
            log.error("getPointsScoresFromCouchbaseByPointName version error pointName:" + pointName + ",userBucket:" + userBucket);
            return Collections.emptyMap();
        }

        Integer ver = PsrTools.stringToInt(verValue[0]);
        if (StringUtils.isBlank(verValue[1])) {
            log.error("getPointsScoresFromCouchbaseByPointName value error pointName:" + pointName + ",userBucket:" + userBucket);
            return Collections.emptyMap();
        }

        String[] aValue = verValue[1].split(";");
        if (aValue.length <= 0) {
            log.error("getPointsScoresFromCouchbaseByPointName value split error pointName:" + pointName + ",userBucket:" + userBucket);
            return Collections.emptyMap();
        }

        Map<String/*pattern*/, KeyValuePair<Integer, Double>> retMap = new HashMap<>();
        for (String kp : aValue) {
            String[] aKp = kp.split(",");
            if (aKp.length != 3)
                continue;

            String pattern = aKp[0];
            Integer maxCount = PsrTools.stringToInt(aKp[1]);
            Double score = PsrTools.stringToDouble(aKp[2]);

            if (retMap.containsKey(pattern))
                continue;
            KeyValuePair<Integer, Double> pair = new KeyValuePair<>();
            pair.setKey(maxCount);
            pair.setValue(score);
            retMap.put(pattern, pair);
        }

        return retMap;
    }

    private void clearMap() {
        mathPointPatternScores.clear();
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrMathPointsPatternsScore-Loader") {
            @Override
            public void runSafe() {
                clearMap();
                log.info("PsrMathPointsPatternsScore clearMap on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60 * 60 * 1000, 60 * 60 * 1000);
    }
}



