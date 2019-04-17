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

package com.voxlearning.utopia.service.nekketsu.adventure.impl.dao;

import com.voxlearning.alps.annotation.cache.UtopiaCacheSupport;
import com.voxlearning.alps.annotation.cache.UtopiaCacheable;
import com.voxlearning.alps.dao.mongo.dao.StaticMongoDao;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;

import javax.inject.Named;
import java.util.Collection;
import java.util.List;

/**
 * SystemAppDao
 * 数据量很小，先全部放入缓存
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/8 15:56
 */
@Named
@UtopiaCacheSupport(SystemApp.class)
public class SystemAppDao extends StaticMongoDao<SystemApp, Long> {

    @Override
    protected void calculateCacheDimensions(SystemApp source, Collection<String> dimensions) {
        dimensions.add(SystemApp.cacheKeyAll());
    }

    @UtopiaCacheable(key = "ALL")
    public List<SystemApp> findAllSystemApps() {
        return __find_OTF();
    }

    public void changeSystemAppValid(Long id) {
        SystemApp inst = load(id);
        if (inst == null) {
            return;
        }
        SystemApp candidate = new SystemApp();
        candidate.setValid(!inst.isValidTrue());
        if (__update_OTF(id, candidate) != null) {
            getCache().delete(SystemApp.cacheKeyAll());
        }
    }

}
