package com.voxlearning.utopia.service.ai.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.ai.entity.AIDialogueLessonConfig;
import com.voxlearning.utopia.service.ai.entity.AIDialogueTaskConfig;
import com.voxlearning.utopia.service.ai.entity.AILessonPlay;

import java.util.List;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20181115")
@ServiceTimeout(timeout =30, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 3)
public interface AiLessonConfigService extends IPingable {

    AIDialogueLessonConfig loadAIDialogueLessonConfigById(String id);

    List<AIDialogueLessonConfig> loadAllAIDialogueLessonConfigs();

    MapMessage saveOrUpdateAIDialogueLessonConfigData(AIDialogueLessonConfig config);

    MapMessage deleteAIDialogueLessonConfig(String id);

    /**
    　* @Description: 任务对话相关service
    　* @author zhiqi.yao
    　* @date 2018/4/13 14:23
    */
    AIDialogueTaskConfig loadAIDialogueTaskConfigById(String id);

    List<AIDialogueTaskConfig> loadAllAIDialogueTaskConfigs();

    MapMessage saveOrUpdateAIDialogueTaskConfigData(AIDialogueTaskConfig config);

    MapMessage deleteAIDialogueTaskConfig(String id);

    /**
     * 查询所有的剧本
     */
    List<AILessonPlay> loadAllAILessonPlay();

    AILessonPlay loadAILessonPlayById(String id);

    MapMessage saveOrUpdateAILessonPlayData(AILessonPlay play);

    MapMessage deleteAILessonPlay(String id);
}
