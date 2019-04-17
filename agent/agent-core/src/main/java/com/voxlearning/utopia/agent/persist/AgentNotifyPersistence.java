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

package com.voxlearning.utopia.agent.persist;

import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.persist.entity.AgentNotify;

import javax.inject.Named;
import java.util.Collection;
import java.util.Date;
import java.util.List;

/**
 * Notify Persistence
 * Created by Shuai.Huan on 2014/7/21.
 */
@Named
//@UtopiaCacheSupport(AgentNotify.class)
public class AgentNotifyPersistence extends StaticPersistence<Long, AgentNotify> {
    @Override
    protected void calculateCacheDimensions(AgentNotify source, Collection<String> dimensions) {

    }

    public List<AgentNotify> findAgentNotifyList(String notifyType,Date beginDate, Date endDate){
        String sql = "WHERE NOTIFY_TYPE = ?  AND CREATE_DATETIME >= ? AND CREATE_DATETIME <= ? ORDER BY CREATE_DATETIME DESC";
        return withSelectFromTable(sql).useParamsArgs(notifyType,beginDate,endDate).queryAll();
    }
}
