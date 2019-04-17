package com.voxlearning.utopia.service.afenti.base.cache.managers.picbook;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.spi.cache.UtopiaCache;

/**
 * Created by Summer on 2018/4/11
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.this_week)
public class StudentPicBookWeekRankRewardCacheManager extends PojoCacheObject<Long, String> {

    public StudentPicBookWeekRankRewardCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean showCard(Long studentId) {
        if (studentId == null) {
            return false;
        }
        String content = load(studentId);
        return StringUtils.isBlank(content);
    }

    public void pop(Long studentId) {
        if (studentId == null) return;
        set(studentId, "dummy");
    }
}
