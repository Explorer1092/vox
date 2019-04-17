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

package com.voxlearning.utopia.service.mizar.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.mizar.api.AsyncMizarCacheService;
import com.voxlearning.utopia.service.mizar.consumer.manager.CodecSaltCacheManager;
import com.voxlearning.utopia.service.mizar.consumer.manager.MizarCourseReadCountManager;
import com.voxlearning.utopia.service.mizar.consumer.manager.MizarLikeShopMonthCountManager;
import com.voxlearning.utopia.service.mizar.consumer.manager.MizarUserSessionManager;

import javax.inject.Named;

@Named("com.voxlearning.utopia.service.mizar.impl.service.AsyncMizarCacheServiceImpl")
@ExposeService(interfaceClass = AsyncMizarCacheService.class)
public class AsyncMizarCacheServiceImpl extends SpringContainerSupport implements AsyncMizarCacheService {

    private MizarUserSessionManager mizarUserSessionManager;

    private CodecSaltCacheManager codecSaltCacheManager;
    private MizarLikeShopMonthCountManager mizarLikeShopMonthCountManager;

    private MizarCourseReadCountManager mizarCourseReadCountManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        UtopiaCache flushable = CacheSystem.CBS.getCache("flushable");
        mizarUserSessionManager = new MizarUserSessionManager(flushable);

        UtopiaCache unflushable = CacheSystem.CBS.getCache("unflushable");
        codecSaltCacheManager = new CodecSaltCacheManager(unflushable);
        mizarLikeShopMonthCountManager = new MizarLikeShopMonthCountManager(unflushable);

        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        mizarCourseReadCountManager = new MizarCourseReadCountManager(persistence);
    }

    @Override
    public AlpsFuture<Boolean> CodecSaltCacheManager_setCode(String name) {
        codecSaltCacheManager.setCode(name);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<String> CodecSaltCacheManager_getCode(String name) {
        String s = codecSaltCacheManager.getCode(name);
        return new ValueWrapperFuture<>(s);
    }

    @Override
    public AlpsFuture<Long> MizarCourseReadCountManager_increaseCount(String courseId) {
        long l = mizarCourseReadCountManager.increaseCount(courseId);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Long> MizarCourseReadCountManager_loadReadCount(String courseId) {
        long l = mizarCourseReadCountManager.loadReadCount(courseId);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Long> MizarLikeShopMonthCountManager_increaseCount(Long parentId) {
        long l = mizarLikeShopMonthCountManager.increaseCount(parentId);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Long> MizarLikeShopMonthCountManager_loadLikeCount(Long parentId) {
        long l = mizarLikeShopMonthCountManager.loadLikeCount(parentId);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Boolean> MizarUserSessionManager_addUserSessionAttribute(String userId, String attrKey, Object attrValue) {
        mizarUserSessionManager.addUserSessionAttribute(userId, attrKey, attrValue);
        return new ValueWrapperFuture<>(true);
    }

    @Override
    public AlpsFuture<Object> MizarUserSessionManager_getUserSessionAttribute(String userId, String attrKey) {
        Object o = mizarUserSessionManager.getUserSessionAttribute(userId, attrKey);
        return new ValueWrapperFuture<>(o);
    }

    @Override
    public AlpsFuture<Boolean> MizarUserSessionManager_removeUserSession(String userId) {
        mizarUserSessionManager.removeUserSession(userId);
        return new ValueWrapperFuture<>(true);
    }
}
