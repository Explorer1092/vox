package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.concurrent.TimeUnit;

@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
@ServiceVersion(version = "20190301")
public interface MiniProgramBookService extends IPingable {
    MapMessage classLevelTerm(Long uid);

    MapMessage bookList(Long uid, Long pid, Integer clazzLevel,String publishId,String sys, String cdnBaseUrl);

    MapMessage bookSelf(Long pid, String sys,String cdnBaseUrl);

    MapMessage bookDetail(Long uid,Long pid, String bookId,String sys, String cdnBaseUrl);

    MapMessage bookDetail(Long uid,Long pid, String bookId,String sys, String cdnBaseUrl, String ver);

    MapMessage productInfo(Long uid, Long pid, String bookId,String cdnBaseUrl,String cdnBaseUrlAvatar);

    MapMessage recommend(Long uid, Long pid, String productIds,String cdnBaseUrl);
}
