package com.voxlearning.utopia.service.zone.impl.support;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZone;
import com.voxlearning.utopia.service.zone.api.entity.DiscussZoneUserRecord;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.List;

/**
 * @author chensn
 * @date 2018-10-23 16:41
 */
@Named
public class DiscussZoneCacheManager implements InitializingBean {
    private DiscussZoneCache discussZoneCache;
    private DiscussRecordCache discussRecordCache;
    @Override
    public void afterPropertiesSet() throws Exception {
        UtopiaCache cache = CacheSystem.CBS.getCacheBuilder().getCache("columb-zone-cache");
        discussZoneCache = new DiscussZoneCache(cache);
        discussRecordCache = new DiscussRecordCache(cache);
    }
    public List<DiscussZone> findUsedDiscussZoneCache(){
        return discussZoneCache.findUsedDiscussZoneCache();
    }
    public void save(List<DiscussZone> list){
        discussZoneCache.set(DiscussZoneCache.KEY, list);
    }

    public void saveRecord(Integer discussId, Long clazzId, List<DiscussZoneUserRecord> discussZoneUserRecords) {
        discussRecordCache.set(DiscussRecordCache.KEY + discussId + "_" + clazzId, discussZoneUserRecords);
    }

    public List<DiscussZoneUserRecord> findRecordCache(Integer discussId, Long clazzId) {
        return discussRecordCache.findRecordCache(discussId, clazzId);
    }

    public void deleteDiscussCache() {
        discussZoneCache.evict(DiscussZoneCache.KEY);
    }
}
