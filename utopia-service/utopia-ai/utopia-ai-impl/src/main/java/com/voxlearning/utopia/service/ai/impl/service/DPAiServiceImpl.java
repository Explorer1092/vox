package com.voxlearning.utopia.service.ai.impl.service;

import com.alibaba.fastjson.JSONObject;
import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.api.DPAiService;
import com.voxlearning.utopia.service.ai.entity.AIDialogueLessonConfig;
import com.voxlearning.utopia.service.ai.entity.AIDialogueTaskConfig;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @author songtao
 * @since 2018/4/11
 */
@Named
@ExposeService(interfaceClass = DPAiService.class)
public class DPAiServiceImpl implements DPAiService {
    @Inject
    private AiLessonConfigServiceImpl aiLessonConfigService;

    @Override
    public Map<String, Object> loadAiDialogueLesson(String id) {
        if (StringUtils.isBlank(id)) {
            return Collections.emptyMap();
        }
        AIDialogueLessonConfig aiDialogueLessonConfig = aiLessonConfigService.loadAIDialogueLessonConfigById(id);
        JSONObject lesson = toUnderlineJsonString(aiDialogueLessonConfig);
        if (lesson == null) {
            return Collections.emptyMap();
        }
        return lesson;
    }

    @Override
    public List<Map<String, Object>> loadAllAiDialogueLesson() {
        List<AIDialogueLessonConfig> aiDialogueLessonConfigs = aiLessonConfigService.loadAllAIDialogueLessonConfigs();
        List<Map<String, Object>> lessons = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aiDialogueLessonConfigs)) {
            for (AIDialogueLessonConfig aiDialogueLessonConfig : aiDialogueLessonConfigs) {
                JSONObject lesson = toUnderlineJsonString(aiDialogueLessonConfig);
                if (lesson == null) {
                    continue;
                }
                lessons.add(lesson);
            }
        }
        return lessons;
    }

    /**
    　* @Description: 根据id获取任务对话内容
    　* @author zhiqi.yao
    　* @date 2018/4/12 21:04
    */
    @Override
    public Map<String, Object> loadAiDialogueTask(String id) {
        if (StringUtils.isBlank(id)) {
            return Collections.emptyMap();
        }
        AIDialogueTaskConfig aiDialogueTaskConfig = aiLessonConfigService.loadAIDialogueTaskConfigById(id);
        JSONObject lesson = toUnderlineJsonString(aiDialogueTaskConfig);
        if (lesson == null) {
            return Collections.emptyMap();
        }
        return lesson;
    }
    /**
    　* @Description: 获取所以任务对话内容列表
    　* @author zhiqi.yao
    　* @date 2018/4/12 21:10
    */
    @Override
    public List<Map<String, Object>> loadAllAiDialogueTask() {
        List<AIDialogueTaskConfig> aiDialogueTaskConfigs = aiLessonConfigService.loadAllAIDialogueTaskConfigs();
        List<Map<String, Object>> lessons = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(aiDialogueTaskConfigs)) {
            for (AIDialogueTaskConfig aiDialogueTaskConfig : aiDialogueTaskConfigs) {
                JSONObject lesson = toUnderlineJsonString(aiDialogueTaskConfig);
                if (lesson == null) {
                    continue;
                }
                lessons.add(lesson);
            }
        }
        return lessons;
    }

    private JSONObject toUnderlineJsonString(Object config) {
        if (config == null) {
            return null;
        }
        try {
            ObjectMapper mapper = new ObjectMapper();
            mapper.setPropertyNamingStrategy(PropertyNamingStrategy.SNAKE_CASE);
            mapper.setSerializationInclusion(JsonInclude.Include.ALWAYS);
            String reqJson = mapper.writeValueAsString(config);
            return JSONObject.parseObject(reqJson);
        } catch (Exception e) {
            return null;
        }
    }
}
