package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.data.ChipsLessonRequest;
import com.voxlearning.utopia.service.ai.data.ChipsQuestionResultRequest;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190304")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsWechatUserContentService extends IPingable {
    MapMessage synthesisUserVideo(Long wechatUserId, ChipsLessonRequest request, List<String> userVideos);

    MapMessage processDialogueFeedback(Long wechatUserId, String input, String qid, ChipsLessonRequest request);

    MapMessage processQuestionResult(Long wechatUserId, ChipsQuestionResultRequest chipsQuestionResultRequest);

}
