/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.psr.impl.util;

import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimer;
import com.voxlearning.alps.lang.concurrent.ExceptionSafeTimerTask;
import com.voxlearning.utopia.entity.content.UnitKnowledgePointRef;
import com.voxlearning.utopia.service.content.client.UnitKnowledgePointServiceClient;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

/*
 * Created by ChaoLi Lee on 14-7-11.
 * 必须要初始化 unitKnowledgePointRefDao 这个东东
 */
@Slf4j
@Named
public class PsrBooksPointsRef implements InitializingBean {

    private Map<Long, List<UnitKnowledgePointRef>> unitKnowledgePointRefMap = new ConcurrentHashMap<>();
    @Inject private UnitKnowledgePointServiceClient unitKnowledgePointServiceClient;

    public List<UnitKnowledgePointRef> getUnitKnowledgePointRefs(Long bookId) {
        if (bookId < 0) return null;

        List<UnitKnowledgePointRef> lst = unitKnowledgePointRefMap.get(bookId);
        if (lst != null)
            return lst;

        List<UnitKnowledgePointRef> tmpList = unitKnowledgePointServiceClient.getUnitKnowledgePointService()
                .findUnitKnowledgePointRefsByBookId(bookId)
                .getUninterruptibly();
        if (tmpList.size() <= 0)
            return tmpList;

        List<UnitKnowledgePointRef> unitKnowledgePointRefs = new ArrayList<>();
        for (UnitKnowledgePointRef ref : tmpList) {
            if (!ref.getPointType().equals("WORDS"))
                continue;
            unitKnowledgePointRefs.add(ref);
        }

        /*
         * 日后 优化 为 访问度最低的 剔除队列,以便让频繁访问的数据进入队列
         * 不能无限制的 存入map
         */
        if (unitKnowledgePointRefMap.size() < 1000)
            unitKnowledgePointRefMap.put(bookId, unitKnowledgePointRefs);
        else
            log.error("getUnitKnowledgePointRefs message buffer full: " + unitKnowledgePointRefMap.size());

        return unitKnowledgePointRefs;
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrBooksPointsRef-Loader") {
            @Override
            public void runSafe() {
                unitKnowledgePointRefMap.clear();
                log.info("PsrBooksPointsRef map clear on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 60 * 60 * 1000, 60 * 60 * 1000);
    }
}
