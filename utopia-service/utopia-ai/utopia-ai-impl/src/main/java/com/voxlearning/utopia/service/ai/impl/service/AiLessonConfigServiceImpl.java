package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.annotation.remote.ExposeServices;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.AiLessonConfigService;
import com.voxlearning.utopia.service.ai.entity.AIDialogueLessonConfig;
import com.voxlearning.utopia.service.ai.entity.AIDialogueTaskConfig;
import com.voxlearning.utopia.service.ai.entity.AILessonPlay;
import com.voxlearning.utopia.service.ai.impl.persistence.AIDialogueLessonConfigDao;
import com.voxlearning.utopia.service.ai.impl.persistence.AIDialogueTaskConfigDao;
import com.voxlearning.utopia.service.ai.impl.persistence.AILessonPlayDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * @author songtao
 * @since 2018/4/10
 */
@Named
@ExposeServices({
        @ExposeService(interfaceClass = AiLessonConfigService.class,version = @ServiceVersion(version = "20181115")),
        @ExposeService(interfaceClass = AiLessonConfigService.class,version = @ServiceVersion(version = "20181030"))
})
public class AiLessonConfigServiceImpl implements AiLessonConfigService {

    @Inject
    private AIDialogueLessonConfigDao aiDialogueLessonConfigDao;
    @Inject
    private AIDialogueTaskConfigDao aiDialogueTaskConfigDao;

    @Inject
    private AILessonPlayDao aiLessonPlayDao;

    @Override
    public AIDialogueLessonConfig loadAIDialogueLessonConfigById(String id) {
        return aiDialogueLessonConfigDao.load(id);
    }

    @Override
    public List<AIDialogueLessonConfig> loadAllAIDialogueLessonConfigs() {
        return aiDialogueLessonConfigDao.findAll();
    }

    @Override
    public MapMessage saveOrUpdateAIDialogueLessonConfigData(AIDialogueLessonConfig config) {
        AIDialogueLessonConfig oldConfig = aiDialogueLessonConfigDao.load(config.getId());
        if (oldConfig == null) {
            config.setCreateDate(new Date());
        } else {
            config.setCreateDate(oldConfig.getCreateDate());
        }
        config.setUpdateDate(new Date());
        config.setDisabled(false);
        aiDialogueLessonConfigDao.upsert(config);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteAIDialogueLessonConfig(String id) {
        aiDialogueLessonConfigDao.deleteById(id);
        return MapMessage.successMessage();
    }
    /**
    　* @Description: 任务对话server具体实现类
    　* @author zhiqi.yao
    　* @date 2018/4/13 14:27
    */
    @Override
    public AIDialogueTaskConfig loadAIDialogueTaskConfigById(String id) {
        return aiDialogueTaskConfigDao.load(id);
    }

    @Override
    public List<AIDialogueTaskConfig> loadAllAIDialogueTaskConfigs() {
        return aiDialogueTaskConfigDao.findAll();
    }

    @Override
    public MapMessage saveOrUpdateAIDialogueTaskConfigData(AIDialogueTaskConfig config) {
        AIDialogueTaskConfig oldConfig = aiDialogueTaskConfigDao.load(config.getId());
        if (oldConfig == null) {
            config.setCreateDate(new Date());
        } else {
            config.setCreateDate(oldConfig.getCreateDate());
        }
        config.setUpdateDate(new Date());
        config.setDisabled(false);
        aiDialogueTaskConfigDao.upsert(config);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteAIDialogueTaskConfig(String id) {
        aiDialogueTaskConfigDao.deleteById(id);
        return MapMessage.successMessage();
    }

    @Override
    public List<AILessonPlay> loadAllAILessonPlay() {
        return aiLessonPlayDao.findAll();
    }

    @Override
    public AILessonPlay loadAILessonPlayById(String id) {
        return aiLessonPlayDao.load(id);
    }

    @Override
    public MapMessage saveOrUpdateAILessonPlayData(AILessonPlay play) {
        AILessonPlay oldConfig = aiLessonPlayDao.load(play.getId());
        if (oldConfig == null) {
            play.setCreateDate(new Date());
        } else {
            play.setCreateDate(oldConfig.getCreateDate());
        }
        play.setUpdateDate(new Date());
        play.setDisabled(false);
        aiLessonPlayDao.upsert(play);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteAILessonPlay(String id) {
        aiLessonPlayDao.deleteById(id);
        return MapMessage.successMessage();
    }
}
