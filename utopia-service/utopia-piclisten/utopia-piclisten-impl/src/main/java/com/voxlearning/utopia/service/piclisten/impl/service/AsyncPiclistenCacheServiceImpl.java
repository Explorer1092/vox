package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.cache.redis.command.IRedisCommands;
import com.voxlearning.alps.cache.redis.command.RedisCommandsBuilder;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.alps.spi.cache.CacheBuilder;
import com.voxlearning.alps.spi.cache.UtopiaCache;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.piclisten.api.AsyncPiclistenCacheService;
import com.voxlearning.utopia.service.piclisten.consumer.cache.manager.*;
import lombok.Getter;

import javax.inject.Named;
import java.util.Date;

@Named("com.voxlearning.utopia.service.vendor.impl.service.AsyncVendorCacheServiceImpl")
@ExposeServices({
        @ExposeService(interfaceClass = AsyncPiclistenCacheService.class, version = @ServiceVersion(version = "2017.05.16")),
        @ExposeService(interfaceClass = AsyncPiclistenCacheService.class, version = @ServiceVersion(version = "2017.12.04"))
})
@Getter
public class AsyncPiclistenCacheServiceImpl extends SpringContainerSupport implements AsyncPiclistenCacheService {


    private CParentSelfStudyBookCacheManager parentSelfStudyBookCacheManager;
    private ParentShareTextReadLimitCacheManager parentShareTextReadLimitCacheManager;
    private StudentGrindEarDayRecordCacheManager studentGrindEarDayRecordCacheManager;

    private FollowReadDayReadCacheManager followReadDayReadCacheManager;
    private PicListenReportCacheManager picListenReportCacheManager;
    private FollowReadShareLikeRankCacheManager followReadShareLikeRankCacheManager;


    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();

        UtopiaCache unflushable = CacheSystem.CBS.getCache("unflushable");
        parentSelfStudyBookCacheManager = new CParentSelfStudyBookCacheManager(unflushable);
        parentShareTextReadLimitCacheManager = new ParentShareTextReadLimitCacheManager(unflushable);
        studentGrindEarDayRecordCacheManager = new StudentGrindEarDayRecordCacheManager(unflushable);
        UtopiaCache persistence = CacheSystem.CBS.getCache("persistence");
        followReadDayReadCacheManager = new FollowReadDayReadCacheManager(persistence);


        CacheBuilder cacheBuilder = CacheSystem.RDS.getCacheBuilder();


        RedisCommandsBuilder instance = RedisCommandsBuilder.getInstance();
        UtopiaCache parentAppCache = cacheBuilder.getCache("parent-app");
        IRedisCommands parentAppRedisCommands = instance.getRedisCommands("parent-app");
        picListenReportCacheManager = new PicListenReportCacheManager(parentAppCache, parentAppRedisCommands);
        followReadShareLikeRankCacheManager = new FollowReadShareLikeRankCacheManager(parentAppCache, parentAppRedisCommands);


    }

    @Override
    public AlpsFuture<Boolean> CParentSelfStudyBookCacheManager_setParentSeflStudyBook(Long parentId, SelfStudyType selfStudyType, String bookId) {
        Boolean b = parentSelfStudyBookCacheManager.setParentSeflStudyBook(parentId, selfStudyType, bookId);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<String> CParentSelfStudyBookCacheManager_getParentSelfStudyBook(Long parentId, SelfStudyType selfStudyType) {
        String s = parentSelfStudyBookCacheManager.getParentSelfStudyBook(parentId, selfStudyType);
        return new ValueWrapperFuture<>(s);
    }


    @Override
    public AlpsFuture<Long> ParentShareTextReadLimitCacheManager_incr(Long parentId, String paragraphId, Long delta) {
        Long l = parentShareTextReadLimitCacheManager.incr(parentId, paragraphId, delta);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Long> ParentShareTextReadLimitCacheManager_get(Long parentId, String paragraphId) {
        Long l = parentShareTextReadLimitCacheManager.get(parentId, paragraphId);
        return new ValueWrapperFuture<>(l);
    }

    @Override
    public AlpsFuture<Boolean> StudentGrindEarDayRecordCacheManager_hasRecord(Long studentId, Date date) {
        Boolean b = studentGrindEarDayRecordCacheManager.hasRecord(studentId, date);
        return new ValueWrapperFuture<>(b);
    }

    @Override
    public AlpsFuture<Boolean> StudentGrindEarDayRecordCacheManager_todayRecord(Long studentId, Date date) {
        studentGrindEarDayRecordCacheManager.todayRecord(studentId, date);
        return new ValueWrapperFuture<>(true);
    }

}
