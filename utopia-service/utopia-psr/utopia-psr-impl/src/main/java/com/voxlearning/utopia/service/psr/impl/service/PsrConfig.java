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

import com.cedarsoftware.util.ExceptionUtilities;
import com.voxlearning.alps.api.event.AlpsEventContext;
import com.voxlearning.alps.api.event.EventBus;
import com.voxlearning.alps.api.event.dsl.MinuteTimerEventListener;
import com.voxlearning.alps.api.event.dsl.TimerEvent;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.core.RuntimeModeLoader;
import com.voxlearning.utopia.service.psr.impl.dao.EkCouchbaseDao;
import com.voxlearning.utopia.service.psr.impl.util.PsrTools;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Created by ChaoLi Lee on 14-7-14.
 */

@Slf4j
@Named
public class PsrConfig implements InitializingBean {
    @Inject
    private EkCouchbaseDao ekCouchbaseDao;

    private Map<String, String> mapConfig = new ConcurrentHashMap<>();

    public PsrConfig() {
    }

    private final MinuteTimerEventListener reloadListener = new MinuteTimerEventListener() {
        @Override
        protected long mod() {
            return 5;
        }

        @Override
        protected void processEvent(TimerEvent timerEvent, AlpsEventContext alpsEventContext) {
            if (RuntimeModeLoader.getInstance().isUnitTest()) {
                return;
            }
            try {
                reLoad();
            } catch (Exception ex) {
                ExceptionUtilities.safelyIgnoreException(ex);
            }
        }
    };

    @Override
    public void afterPropertiesSet() throws Exception {
        mapConfig.clear();
        reLoad();
        EventBus.subscribe(reloadListener);
    }

    /*
         * key = psr_config
         * value = lowirttheta:0.0;hightrttheta:1.5;eidcountrateperek:0.4;downsetrange:0.85;basenumberforweight:100;minnotaboveleveleids:5;maxeidcount:50;
         */
    public void reLoad() {
        if (ekCouchbaseDao == null) {
            log.error("ekCouchbaseDao is null");
            return;
        }
        String strKey = "psr_config";
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);
        if (StringUtils.isEmpty(strValue)) {
            log.error("PsrConfig reLoad Error from coucebase");
            return;
        }
        Map<String, String> configMap = PsrTools.decodePsrConfigFromLine(strValue);
        if (configMap == null || configMap.size() <= 0) {
            log.error("PsrConfig reLoad decode Error");
            return;
        }

        log.info("PsrConfig reLoad on the timer [" + strValue + "]");

        mapConfig.clear();
        mapConfig.putAll(configMap);
    }


    public boolean containsKey(String key) {
        if (key == null || StringUtils.isEmpty(key) || mapConfig == null)
            return false;

        return (mapConfig.containsKey(key));
    }

    public Double getDoubleValue(String key) {
        if (key == null || StringUtils.isEmpty(key))
            return 0.0;
        return (PsrTools.stringToDouble(getStringValue(key)));
    }

    public Integer getIntegerValue(String key) {
        if (key == null || StringUtils.isEmpty(key))
            return 0;
        return (PsrTools.stringToInt(getStringValue(key)));
    }

    public Long getLongValue(String key) {
        if (key == null || StringUtils.isEmpty(key))
            return 0L;
        return (PsrTools.stringToLong(getStringValue(key)));
    }

    public String getStringValue(String key) {
        if (key == null || StringUtils.isEmpty(key))
            return null;

        if (mapConfig == null)
            reLoad();

        if (mapConfig != null && mapConfig.containsKey(key))
            return mapConfig.get(key);

        return null;
    }
}

