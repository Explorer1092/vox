package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;

import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * @author songtao
 * @since 2018/4/11
 */
@ServiceVersion(version = "20180411")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface DPAiService extends IPingable {

    Map<String, Object> loadAiDialogueLesson(String id);

    List<Map<String, Object>> loadAllAiDialogueLesson();
    /**
    　* @Description: 根据id获取任务对话内容
    　* @author zhiqi.yao
    　* @date 2018/4/12 21:08
    */
    Map<String, Object> loadAiDialogueTask(String id);
    /**
    　* @Description: 获取所以任务对话内容列表
    　* @author zhiqi.yao
    　* @date 2018/4/12 21:09
    */
    List<Map<String, Object>> loadAllAiDialogueTask();
}
