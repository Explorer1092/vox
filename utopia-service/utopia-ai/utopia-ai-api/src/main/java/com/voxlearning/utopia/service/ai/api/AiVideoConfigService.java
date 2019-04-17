package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.AIVideoConfig;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * ${app视频页面的操作}
 *
 * @author zhiqi.yao
 * @create 2018-04-19 17:59
 **/
@ServiceVersion(version = "20180419")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface AiVideoConfigService extends IPingable {
    /**
     * 增加一个视频，【热门】【精选活动】【搞笑集锦】
     */
    MapMessage saveOrUpdateAIVideoConfigData(AIVideoConfig config);
    /**
     * 根据id删除一个视频
     * @param id
     * @return
     */
    MapMessage deleteAIVideoConfig(String id);
    /**
     * 获取到所以得视频列表
     * @return
     */
    List<AIVideoConfig> loadAllAIVideoConfigs();
    /**
     * 根据id获取到视频
     * @param id
     * @return
     */
    AIVideoConfig loadAIVideoConfigById(String id);



}
