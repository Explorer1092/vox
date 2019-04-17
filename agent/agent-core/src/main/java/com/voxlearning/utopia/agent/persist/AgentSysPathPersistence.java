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

import com.voxlearning.alps.annotation.cache.CacheBean;
import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.dao.jdbc.dao.AlpsStaticJdbcDao;
import com.voxlearning.utopia.agent.persist.entity.AgentSysPath;

import javax.inject.Named;
import java.util.Collection;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Created by Shuai.Huan on 2014/7/3.
 */
@Named
@CacheBean(type = AgentSysPath.class)
public class AgentSysPathPersistence extends AlpsStaticJdbcDao<AgentSysPath, Long> {

    @Override
    protected void calculateCacheDimensions(AgentSysPath document, Collection<String> dimensions) {
        dimensions.add(AgentSysPath.ck_id(document.getId()));
        dimensions.add(AgentSysPath.ck_all());
    }

    @CacheMethod(key = "ALL")
    public List<AgentSysPath> findAll() {
        return query();
    }

    public int delete(Long id) {
        int rows = $remove(id) ? 1 : 0;
        if (rows > 0) {
            Set<String> cacheKeys = new HashSet<>();
            cacheKeys.add(AgentSysPath.ck_id(id));
            cacheKeys.add(AgentSysPath.ck_all());
            getCache().delete(cacheKeys);
        }
        return rows;
    }

}
