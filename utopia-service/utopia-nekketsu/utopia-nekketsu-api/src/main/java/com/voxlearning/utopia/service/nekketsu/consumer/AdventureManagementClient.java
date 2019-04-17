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

package com.voxlearning.utopia.service.nekketsu.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.nekketsu.adventure.api.AdventureService;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.BookStages;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.SystemApp;
import com.voxlearning.utopia.service.nekketsu.adventure.entity.UserAdventure;
import com.voxlearning.utopia.service.nekketsu.cache.NekketsuCache;
import lombok.Getter;

/**
 * 管理功能客户端
 *
 * @author GaoJian
 * @version 0.1
 * @since 2014/9/28 16:57
 */
public class AdventureManagementClient {

    @Getter
    @ImportService(interfaceClass = AdventureService.class)
    private AdventureService remoteReference;

    public MapMessage getCache(Long userId) {
        UserAdventure userAdventure = NekketsuCache.getNekketsuCache().load(CacheKeyGenerator.generateCacheKey(UserAdventure.class, userId));
        BookStages bookStages = null;
        if (null != userAdventure) {
            bookStages = NekketsuCache.getNekketsuCache().load(BookStages.ck_id(userAdventure.getBookStagesId()));
        }
        return MapMessage.successMessage().add("userAdventure", userAdventure).add("bookStages", bookStages);
    }

    public MapMessage refreshAppCache() {
        NekketsuCache.getNekketsuCache().delete(CacheKeyGenerator.generateCacheKey(SystemApp.class, "all"));
        return MapMessage.successMessage();
    }

    public MapMessage refreshCache(Long userId) {
        UserAdventure userAdventure = NekketsuCache.getNekketsuCache().load(CacheKeyGenerator.generateCacheKey(UserAdventure.class, userId));
        if (null != userAdventure) {
            NekketsuCache.getNekketsuCache().delete(CacheKeyGenerator.generateCacheKey(UserAdventure.class, userId));
            NekketsuCache.getNekketsuCache().delete(BookStages.ck_id(userAdventure.getBookStagesId()));
        }
        return MapMessage.successMessage();
    }

}
