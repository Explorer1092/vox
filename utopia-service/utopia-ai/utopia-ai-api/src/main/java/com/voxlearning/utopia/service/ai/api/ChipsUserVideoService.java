package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.constant.AiUserVideoLevel;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;

import java.text.ParseException;
import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20190328")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface ChipsUserVideoService extends IPingable {

    MapMessage synthesisDrawingTaskVideo(Long userId, Long drawingTaskId, String coverImage, List<String> userVideos);

    /**
     * 添加到视频黑名单
     * @param userId
     * @return
     */
    MapMessage addVideoBlackList(Long userId);

    MapMessage deleteVideoBlackList(Long userId);

    void filterVideo(Long userId, String unitId);

    MapMessage examineVideo(AIUserVideo video);

    MapMessage examineAudio(Long userId, String unitId, AiUserVideoLevel level, String qrcId , Integer lscore, String video);

    MapMessage filterRemarkVideo(Long userId, String unitId);

    /**
     * 重新处理没有成功合成用户视频的
     * @return
     */
    MapMessage modifyRemarkVideo(String beginDate) throws ParseException;

    /**
     * 根据remarkStatus重新处理点评视频 * flag 未true代表重新处理
     * @return
     */
    MapMessage modifyRemarkVideoByReamkStatus(String beginDate, String endDate, int remarkStatus, boolean flag) throws ParseException;
}
