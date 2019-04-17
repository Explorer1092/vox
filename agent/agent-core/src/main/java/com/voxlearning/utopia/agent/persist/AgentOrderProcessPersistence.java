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

import com.voxlearning.alps.dao.jdbc.persistence.AbstractEntityPersistence;
import com.voxlearning.utopia.agent.persist.entity.AgentOrderProcess;

import javax.inject.Named;
import java.util.List;

/**
 * Created by Shuai.Huan on 2014/7/14.
 */
@Named
public class AgentOrderProcessPersistence extends AbstractEntityPersistence<Long, AgentOrderProcess> {

    public List<AgentOrderProcess> findByTargetUser(Long targetUser) {
        return withSelectFromTable("WHERE TARGET_USER=?").useParamsArgs(targetUser).queryAll();
    }

    public List<AgentOrderProcess> findByTargetGroup(Long targetGroup) {
        return withSelectFromTable("WHERE TARGET_GROUP=?").useParamsArgs(targetGroup).queryAll();
    }

    public AgentOrderProcess findByOrderId(Long orderId) {
        return withSelectFromTable("WHERE ORDER_ID=?").useParamsArgs(orderId).queryObject();
    }

    public int delete(Long id) {
        final String sql = "DELETE FROM AGENT_ORDER_PROCESS WHERE ID=?";
        return super.getUtopiaSql().withSql(sql).useParamsArgs(id).executeUpdate();
    }

    public int deleteByOrderId(Long orderId) {
        final String sql = "DELETE FROM AGENT_ORDER_PROCESS WHERE ORDER_ID=?";
        return super.getUtopiaSql().withSql(sql).useParamsArgs(orderId).executeUpdate();
    }
}
