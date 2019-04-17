package com.voxlearning.utopia.service.piclisten.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;

import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author jiangpeng
 * @since 2017-12-27 下午5:21
 **/
@ServiceVersion(version = "2017.12.27")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface DPPicListenService {

    Map<String, Object> loadPicListenBookInfo(String bookId, Long studentId, String sys);

}
