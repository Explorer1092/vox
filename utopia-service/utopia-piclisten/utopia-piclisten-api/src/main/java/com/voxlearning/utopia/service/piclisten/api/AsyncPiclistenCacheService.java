package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.remote.Async;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.utopia.api.constant.SelfStudyType;

import java.util.Date;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "2017.12.04")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface AsyncPiclistenCacheService {

    @Async
    AlpsFuture<Boolean> CParentSelfStudyBookCacheManager_setParentSeflStudyBook(Long parentId, SelfStudyType selfStudyType, String bookId);

    @Async
    AlpsFuture<String> CParentSelfStudyBookCacheManager_getParentSelfStudyBook(Long parentId, SelfStudyType selfStudyType);


    @Async
    AlpsFuture<Long> ParentShareTextReadLimitCacheManager_incr(Long parentId, String paragraphId, Long delta);

    @Async
    AlpsFuture<Long> ParentShareTextReadLimitCacheManager_get(Long parentId, String paragraphId);



    @Async
    AlpsFuture<Boolean> StudentGrindEarDayRecordCacheManager_hasRecord(Long studentId, Date date);

    @Async
    AlpsFuture<Boolean> StudentGrindEarDayRecordCacheManager_todayRecord(Long studentId, Date date);


}
