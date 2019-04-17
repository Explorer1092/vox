package com.voxlearning.utopia.service.piclisten.consumer.cache.manager;

import com.voxlearning.alps.annotation.cache.CachedObjectExpirationPolicy;
import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.cache.support.PojoCacheObject;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import lombok.AllArgsConstructor;
import lombok.EqualsAndHashCode;
import lombok.NoArgsConstructor;

/**
 * Created by jiangpeng on 16/9/20.
 */
@UtopiaCacheExpiration(policy = CachedObjectExpirationPolicy.fixed, value = 86400*30)
public class CParentSelfStudyBookCacheManager extends PojoCacheObject<CParentSelfStudyBookCacheManager.ParentSelfStudyType, String> {

    public CParentSelfStudyBookCacheManager(UtopiaCache cache) {
        super(cache);
    }

    public Boolean setParentSeflStudyBook(Long parentId, SelfStudyType selfStudyType, String bookId){
        ParentSelfStudyType parentSelfStudyType = new ParentSelfStudyType(parentId, selfStudyType.toString());
        return set(parentSelfStudyType, bookId);
    }

    public String getParentSelfStudyBook(Long parentId, SelfStudyType selfStudyType){
        return load(new ParentSelfStudyType(parentId, selfStudyType.toString()));
    }


    @NoArgsConstructor
    @AllArgsConstructor
    @EqualsAndHashCode(of = {"parentId", "selfStudyType"})
    public static class ParentSelfStudyType {
        public Long parentId;
        public String selfStudyType;

        @Override
        public String toString() {
            return "PID=" + parentId + ",ST=" + selfStudyType;
        }
    }
}
