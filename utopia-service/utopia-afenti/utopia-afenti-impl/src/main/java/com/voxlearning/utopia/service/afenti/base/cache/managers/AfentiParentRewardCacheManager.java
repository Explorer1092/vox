package com.voxlearning.utopia.service.afenti.base.cache.managers;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.parentreward.api.constant.ParentRewardType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by Summer on 2017/6/21.
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.today)
public class AfentiParentRewardCacheManager extends PojoCacheObject<AfentiParentRewardCacheManager.GenerateKey, Boolean> {

    public AfentiParentRewardCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public boolean addRecord(Long studentId, ParentRewardType type) {
        if (studentId == null || type == null) return false;
        return set(new GenerateKey(studentId, type), true);
    }

    public boolean existRecord(Long studentId, ParentRewardType type) {
        if (studentId == null || type == null) return false;
        Boolean exist = load(new GenerateKey(studentId, type));
        if (exist == null) return false;
        return exist;
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"studentId", "type"})
    class GenerateKey {
        private Long studentId;
        private ParentRewardType type;

        @Override
        public String toString() {
            return "UID=" + studentId + ",REWARD_TYPE=" + type;
        }
    }

}
