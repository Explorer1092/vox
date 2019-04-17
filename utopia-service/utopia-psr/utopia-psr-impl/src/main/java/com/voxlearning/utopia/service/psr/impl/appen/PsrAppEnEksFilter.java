/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.meta.Ktwelve;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.service.content.api.entity.WordStock;
import com.voxlearning.utopia.service.content.consumer.EnglishContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.WordStockLoaderClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Slf4j
@Named
public class PsrAppEnEksFilter implements InitializingBean {

    @Inject private EnglishContentLoaderClient englishContentLoaderClient;
    @Inject private WordStockLoaderClient wordStockLoaderClient;

    private Map<String/*ek*/, Integer/*invalid:0 or valid:1*/> eksInfo = new ConcurrentHashMap<>();

    private boolean hasPicWord(String ek) {
        if (StringUtils.isBlank(ek))
            return false;

        List<WordStock> wsList = wordStockLoaderClient.loadWordStocksByEntext(ek);
        for (WordStock wsItem : wsList) {
            if (!wsItem.getKtwelve().name().equals(Ktwelve.PRIMARY_SCHOOL.name()))
                continue;
            if (wsItem.hasPicture())
                return true;
        }

        return false;
    }

    public boolean isValid(String ek) {
        if (StringUtils.isBlank(ek))
            return false;
        if (eksInfo.containsKey(ek))
            return eksInfo.get(ek) == 1;

        boolean status = hasPicWord(ek);
        eksInfo.put(ek, status ? 1 : 0);

        return status;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrBooksSentences-Loader") {
            @Override
            public void runSafe() {
                eksInfo.clear();
                log.info("PsrAppEnEksFilter map clear on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 86400 * 1000, 86400 * 1000);
    }
}


