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

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

@Slf4j
@Named
@Deprecated // 2015.08.10
public class PsrGroupTask implements InitializingBean {
// fixme 2015.09.01 之后暂不维护
/*
    @Inject private EkCouchbaseDao ekCouchbaseDao;

    // algo:算法类型,rate:该算法占推题的比例
    private Map<String/ *task* /,Map<String/ *algo* /, Double/ *rate* />> taskMap = new ConcurrentHashMap<>();

    public PsrGroupTask() {
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        taskMap.clear();
        reLoad();

        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrGroupTask-Loader") {
            @Override
            public void runSafe() {
                reLoad();
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 300 * 1000, 300 * 1000);
    }

    / *
     * 分组实验配置文件
     * ver[\t]taskA=algoA:0.5;algoB:0.5[\t]taskB=algodefault:1
     * /
    public void reLoad() {
        if (ekCouchbaseDao == null) {
            log.error("ekCouchbaseDao is null");
            return;
        }
        String strKey = "psr_grouptask";
        String strValue = ekCouchbaseDao.getCouchbaseDataByKey(strKey);
        if (StringUtils.isEmpty(strValue)) {
            log.error("PsrGroupTask reLoad Error from coucebase");
            return;
        }

        String[] sArr = strValue.split("\t");
        if (sArr.length == 0 || sArr.length < 2)
            return;

        String ver = sArr[0];

        Map<String,Map<String,Double>> retMap = new HashMap<>();

        for (int i = 1; i< sArr.length; i++) {
            String[] tmpArr = sArr[i].split("=");
            if (tmpArr.length != 2)
                continue;
            String task = tmpArr[0];

            String[] aArr = tmpArr[1].split(";");
            if (aArr.length == 0)
                continue;

            Map<String, Double> tmpMap = new HashMap<>();

            for (String item : aArr) {
                String[] algoArr = item.split(":");
                if (algoArr.length != 2)
                    continue;

                String algo = algoArr[0];
                Double rate = PsrTools.stringToDouble(algoArr[1]);

                tmpMap.put(algo, rate);
            }

            retMap.put(task, tmpMap);
        }

        if (retMap.size() > 0) {
            taskMap.clear();
            taskMap.putAll(retMap);
            log.info("PsrGroupTask reLoad on the timer [" + strValue + "]");
        } else {
            log.info("PsrGroupTask reLoad on the timer err [" + strValue + "]");
        }
    }


    public Map<String/ *algo* /, Double/ *rate* /> getTask(String key) {
        if (key == null || StringUtils.isEmpty(key) || taskMap == null)
            return null;

        if (!taskMap.containsKey(key)) {
            return null;
        }
        return taskMap.get(key);
    }

*/
// fixme 2015.09.01 之后暂不维护
    @Override
    public void afterPropertiesSet() throws Exception {

    }
}

