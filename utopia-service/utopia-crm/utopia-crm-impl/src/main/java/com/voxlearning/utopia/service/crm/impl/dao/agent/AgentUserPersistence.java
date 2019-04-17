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

package com.voxlearning.utopia.service.crm.impl.dao.agent;

import com.voxlearning.alps.annotation.cache.UtopiaCacheKey;
import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.jdbc.persistence.StaticPersistence;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUser;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
@Named
@UtopiaCacheSupport(AgentUser.class)
public class AgentUserPersistence extends StaticPersistence<Long, AgentUser> {

    @Override
    protected void calculateCacheDimensions(AgentUser source, Collection<String> dimensions) {
        dimensions.add(AgentUser.ck_all());
        dimensions.add(AgentUser.ck_name(source.getAccountName()));
        dimensions.add(AgentUser.ck_id(source.getId()));
    }

    @UtopiaCacheable(key = "ALL")
    public List<AgentUser> findAll() {
        return withSelectFromTable("WHERE STATUS <> 9").queryAll();
    }

    @UtopiaCacheable
    public AgentUser findByName(@UtopiaCacheKey(name = "name") String accountName) {
        return withSelectFromTable("WHERE ACCOUNT_NAME=?").useParamsArgs(accountName).queryObject();
    }

    public AgentUser findByMobile(String mobile) {
        return withSelectFromTable("WHERE TEL=? AND STATUS <> 9").useParamsArgs(mobile).queryObject();
    }

    public List<AgentUser> findByRealName(String realName) {
        return withSelectFromTable("WHERE REAL_NAME like '%" + realName + "%' AND STATUS <> 9").queryAll();
    }

    /**
     * 全匹配查询
     * @param realName
     * @return
     */
    public List<AgentUser> getUserByRealName(String realName) {
        return withSelectFromTable("WHERE REAL_NAME=? AND STATUS <> 9").useParamsArgs(realName).queryAll();
    }

    public int delete(final Long id) {
        Collection<String> keys = calculateDimensions(id);
        String sql = "UPDATE AGENT_USER SET STATUS=9 WHERE ID=?";
        int rows = utopiaSql.withSql(sql).useParamsArgs(id).executeUpdate();
        if (rows > 0) {
            getCache().delete(keys);
        }
        return rows;
    }

    /**
     * 根据工号查询用户
     * @param accountNumber
     * @return
     */
    public List<AgentUser> getUserByAccountNumber(String accountNumber) {
        return withSelectFromTable("WHERE ACCOUNT_NUMBER=? AND STATUS <> 9").useParamsArgs(accountNumber).queryAll();
    }

}
