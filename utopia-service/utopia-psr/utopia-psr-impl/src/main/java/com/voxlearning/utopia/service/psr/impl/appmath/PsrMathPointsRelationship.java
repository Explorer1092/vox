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
import com.voxlearning.utopia.service.psr.entity.MathEkEkContent;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
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
public class PsrMathPointsRelationship implements InitializingBean {
    @Inject EkCouchbaseDao ekCouchbaseDao;

    private Map<String,String> mapBaseToPoionts = new ConcurrentHashMap<>();
    private Map<String,String> mapToBasePoionts = new ConcurrentHashMap<>();
    private Map<String, List<String>> mapPoints = new ConcurrentHashMap<>();  //  高级节点 对应 低级节点列表

    public Map<String, List<String>> getMapPoints() {
        if (mapPoints.size() <= 0) {
            log.info("PsrMathPointsRelationship reLoad on getMapPoints");
            reLaod();
        }
        return mapPoints;
    }

    public void reLaod() {
        mapBaseToPoionts.clear();
        mapToBasePoionts.clear();
        mapPoints.clear();
        init();
        doTree();
        mapBaseToPoionts.clear();
        mapToBasePoionts.clear();
    }

    public void init() {

        List<MathEkEkContent> mathEkEkContentList = ekCouchbaseDao.getMathEkEkContentFromCouchbase();

        for (MathEkEkContent item : mathEkEkContentList) {
            if (item == null)
                continue;
            mapBaseToPoionts.put(item.getEkBase(), item.getEk());
            mapToBasePoionts.put(item.getEk(), item.getEkBase());
        }
    }

    public List<String> next(String key) {
        if (StringUtils.isEmpty(key))
            return null;
        List<String> retList = new ArrayList<>();
        if (mapToBasePoionts.containsKey(key)) {
            String value = mapToBasePoionts.get(key);
            if (!StringUtils.isEmpty(value)) {
                retList.add(value);
                List<String> tmpList = next(value);
                if (tmpList != null && tmpList.size() > 0)
                    retList.addAll(tmpList);
            }
        }
        return retList;
    }

    public void doTree() {
        for (Map.Entry<String,String> entry : mapToBasePoionts.entrySet()) {
            List<String> tmpList = next(entry.getValue());
            if (tmpList == null)
                tmpList = new ArrayList<>();
            tmpList.add(0, entry.getValue());
            if (!mapPoints.containsKey(entry.getKey()))
                mapPoints.put(entry.getKey(), tmpList);
            else {
                if (mapPoints.get(entry.getKey()) != null)
                    mapPoints.get(entry.getKey()).addAll(tmpList);
                else
                    mapPoints.put(entry.getKey(), tmpList);
            }
        }

        for (Map.Entry<String,String> entry : mapBaseToPoionts.entrySet()) {
            if (mapPoints.containsKey(entry.getKey()))
                continue;
            List<String> tmpList = new ArrayList<>();
            mapPoints.put(entry.getKey(), tmpList);
        }
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrMathPointsRelationship-Loader") {
            @Override
            public void runSafe() {
                reLaod();
                log.info("PsrMathPointsRelationship reLoad on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60*60*1000, 60*60*1000);
    }
}
