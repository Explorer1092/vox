package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.AiVideoConfigService;
import com.voxlearning.utopia.service.ai.entity.AIDialogueLessonConfig;
import com.voxlearning.utopia.service.ai.entity.AIVideoConfig;
import com.voxlearning.utopia.service.ai.impl.persistence.AIVideoConfigDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;

/**
 * ${app视频模块操作的具体实现类}
 *
 * @author zhiqi.yao
 * @create 2018-04-19 18:54
 **/
@Named
@ExposeService(interfaceClass = AiVideoConfigService.class)
public class AiVideoConfigServiceImpl implements AiVideoConfigService {

    @Inject
    private AIVideoConfigDao aiVideoConfigDao;

    @Override
    public MapMessage saveOrUpdateAIVideoConfigData(AIVideoConfig config) {
        AIVideoConfig oldConfig = aiVideoConfigDao.load(config.getId());
        if (oldConfig == null) {
            config.setCreateDate(new Date());
        } else {
            config.setCreateDate(oldConfig.getCreateDate());
        }
        config.setUpdateDate(new Date());
        config.setDisabled(false);
        aiVideoConfigDao.upsert(config);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage deleteAIVideoConfig(String id) {
        aiVideoConfigDao.deleteById(id);
        return MapMessage.successMessage();
    }

    @Override
    public List<AIVideoConfig> loadAllAIVideoConfigs() {
        return aiVideoConfigDao.findAll();
    }

    @Override
    public AIVideoConfig loadAIVideoConfigById(String id) {
        return aiVideoConfigDao.load(id);
    }
}
