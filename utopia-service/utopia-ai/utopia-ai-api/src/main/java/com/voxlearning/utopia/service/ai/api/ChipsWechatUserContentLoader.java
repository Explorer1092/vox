package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.data.ChipsLessonRequest;

import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190304")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsWechatUserContentLoader extends IPingable {

    MapMessage loadTrailCourse(Long wechatUserId);

    MapMessage loadLessonInfo(Long wechatUserId, ChipsLessonRequest chipsLessonRequest);

    MapMessage loadLessonResult(Long wechatUserId, ChipsLessonRequest chipsLessonRequest);

    MapMessage loadUnitResult(Long wechatUserId, String bookId, String unitId);

}
