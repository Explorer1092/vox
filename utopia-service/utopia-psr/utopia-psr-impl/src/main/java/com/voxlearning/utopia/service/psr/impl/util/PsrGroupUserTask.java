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

package com.voxlearning.utopia.service.psr.impl.util;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;

@Slf4j
@Named
@Deprecated  // 2015.08.10
public class PsrGroupUserTask implements InitializingBean {
// fixme 2015.09.01 之后暂不维护
/*
    private Map<String,String> userTaskMap = new ConcurrentHashMap<>();
    @Inject private EkCouchbaseDao ekCouchbaseDao;

    public PsrGroupUserTask() {
    }

    public void init () {
        userTaskMap.clear();

        String s = ekCouchbaseDao.getCouchbaseDataByKey("groupusertask_count");
        if (!StringUtils.isEmpty(s)) {
            Integer c = PsrTools.stringToInt(s);
            String v = null;
            for (int i = 0; i < c; i++) {
                v = ekCouchbaseDao.getCouchbaseDataByKey("groupusertask_" + i);
                Map<String,String> tmpMap = PsrTools.decodeGroupUserTaskFromLine(v);
                if (tmpMap == null)
                    continue;
                userTaskMap.putAll(tmpMap);
            }
        }
    }
    // 从配置文件获取用户的分组信息
    public String getTaskFromConfByUid(Long uid) {
        if (uid == null)
            return "defaultGroup";

        if (userTaskMap.containsKey(uid.toString()))
            return userTaskMap.get(uid.toString());
        else
            return "defaultGroup";
    }

    // 根据uid的尾数获取分组信息[1-4,5-7,8-0]
    public String getTaskByUid(Long uid) {
        if (uid == null)
            return "defaultGroup";

        long remainer = uid % 10;
        if (remainer >= 1 && remainer <= 4) {         //  uid尾数为 1\2\3\4,难度=0.8 全部
            return "userGroupA";
        } else if (remainer >= 5 && remainer <= 7) {  //  uid尾数为 5\6\7,难度=0.8 =0.6 各一半
            return "userGroupB";
        } else if (remainer == 8 || remainer == 9 || remainer == 0) {  //  uid尾数为 0\8\9,难度=0.8 错题 各一半
            return "userGroupC";
        }

        return "defaultGroup";
    }

    @Override
    public void afterPropertiesSet() throws Exception {
        init();
        ExceptionSafeTimerTask task = new ExceptionSafeTimerTask("PsrGroupUserTask-Loader") {
            @Override
            public void runSafe() {
                init();
                log.info("PsrGroupUserTask map init on the timer");
            }
        };
        ExceptionSafeTimer.getCommonInstance().schedule(task, 43200*1000, 43200*1000);
    }
*/
// fixme 2015.09.01 之后暂不维护
    @Override
    public void afterPropertiesSet() throws Exception {

    }

}
