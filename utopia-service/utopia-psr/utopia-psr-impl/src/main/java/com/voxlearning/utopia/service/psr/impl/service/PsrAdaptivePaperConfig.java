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

package com.voxlearning.utopia.service.psr.impl.service;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * lichaoli 20161102
 */

@Slf4j
@Named
public class PsrAdaptivePaperConfig implements InitializingBean {
    @Inject private EkCouchbaseDao ekCouchbaseDao;
    private Map<String, Integer> mapConfig = new ConcurrentHashMap<>();

    public PsrAdaptivePaperConfig() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        mapConfig.clear();
        reLoad();
    }

    /**
     * Start the reload timer of psr_adaptivepaper.
     * This method will be trigger in StartPsrAdaptivePaperConfigReloadTimer from provider.
     */
    public void startReloadTimer() {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrAdaptivePaperConfig-Loader") {
            @Override
            public void runSafe() {
                reLoad();
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 300 * 1000, 300 * 1000);
    }

    /*
     * key = psr_adaptivepaper
     * value = BK_1;BK_2;BK_3;**
     */
    public void reLoad() {
        if (ekCouchbaseDao == null) {
            log.error("ekCouchbaseDao is null");
            return;
        }
        String strKey = "psr_adaptivepaper";
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);
        if (StringUtils.isEmpty(strValue)) {
            log.error("psr_adaptivepaper reLoad Error from coucebase");
            return;
        }
        Map<String, Integer> configMap = decodeAdaptivePaperConfig(strValue);
        if (MapUtils.isEmpty(configMap)) {
            log.error("psr_adaptivepaper reLoad decode Error");
            return;
        }

        log.info("psr_adaptivepaper reLoad on the timer [" + strValue + "]");

        mapConfig.clear();
        mapConfig.putAll(configMap);
    }

    public Map<String, Integer> decodeAdaptivePaperConfig(String config)  {
        if (StringUtils.isBlank(config))
            return Collections.emptyMap();

        String[] configArr = config.split(";");
        if (configArr.length <= 0)
            return Collections.emptyMap();

        Map<String, Integer> retMap = new HashMap<>();
        for (String bookId : configArr) {
            if (StringUtils.isBlank(bookId))
                continue;
            retMap.put(bookId, 0);
        }

        return retMap;
    }

    public boolean isAdaptivePaperByBookId(String bookId) {
        if (StringUtils.isBlank(bookId) || MapUtils.isEmpty(mapConfig))
            return false;

        return (mapConfig.containsKey(bookId));
    }

}

