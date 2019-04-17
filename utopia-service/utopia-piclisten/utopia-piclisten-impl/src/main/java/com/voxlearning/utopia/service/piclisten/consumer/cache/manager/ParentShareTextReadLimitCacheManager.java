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

package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by jiangpeng on 16/7/18.
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class ParentShareTextReadLimitCacheManager extends PojoCacheObject<ParentShareTextReadLimitCacheManager.ParentParagraph, String> {


    public ParentShareTextReadLimitCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Long incr(Long parentId, String paragraphId, Long delta) {
        return cache.incr(cacheKey(new ParentParagraph(parentId,paragraphId)), delta, 1, expirationInSeconds());
    }

    public Long get(Long parentId, String paragraphId){
        String countString = load(new ParentParagraph(parentId,paragraphId));
        return countString == null ? 0: SafeConverter.toLong(countString);
    }

    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"parentId", "paragraphId"})
    public static class ParentParagraph {
        public Long parentId;
        public String paragraphId;

        @Override
        public String toString() {
            return "PID=" + parentId + ",PAID=" + paragraphId;
        }
    }
}
