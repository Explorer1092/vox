package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.api.constant.VoiceEngineType;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

@Named
public class VoiceEngineConfigHelper {

    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    public MapMessage loadConfig(StudentDetail studentDetail, ObjectiveConfigType objectiveConfigType) {
        Map<String, Object> configMap = loadConfig();
        Map<ObjectiveConfigType, Map<String, VoiceEngineConfig>> configTypeMapMap = parseConfig(configMap);
        VoiceEngineConfig voiceEngineConfig = null;
        if (MapUtils.isNotEmpty(configTypeMapMap) && MapUtils.isNotEmpty(configTypeMapMap.get(objectiveConfigType))) {
            voiceEngineConfig = matchConfig(configTypeMapMap.get(objectiveConfigType), studentDetail);
        }
        return MapMessage.successMessage()
                .add("processVoiceEngine", voiceEngineConfig != null ? voiceEngineConfig.getProcessVoiceEngine() : "normal")
                .add("submitVoiceEngine", voiceEngineConfig != null ? voiceEngineConfig.getSubmitVoiceEngine() : VoiceEngineType.Unisound.name());
    }

    /**
     * {
     * "BASIC_APP": {
     * "\\d{6}_\\d+_333850004": {
     * "processVoiceEngine": "yzs",
     * "submitVoiceEngine": "UniSound"
     * },
     * "\\d{6}_\\d+_\\d+": {
     * "processVoiceEngine": "xs",
     * "submitVoiceEngine": "SingSound"
     * }
     * }
     * }
     */
    public Map<String, Object> loadConfig() {
        List<PageBlockContent> configs = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("VoiceEngine");
        if (CollectionUtils.isNotEmpty(configs)) {
            PageBlockContent configPageBlockContent = configs.stream()
                    .filter(p -> "VoiceEngineConfig".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                return JsonUtils.convertJsonObjectToMap(configContent);
            }
        }
        return null;
    }

    @SuppressWarnings("unchecked")
    private Map<ObjectiveConfigType, Map<String, VoiceEngineConfig>> parseConfig(Map<String, Object> config) {
        if (MapUtils.isNotEmpty(config)) {
            Map<ObjectiveConfigType, Map<String, VoiceEngineConfig>> configTypeMapMap = new LinkedHashMap<>();
            config.forEach((k, v) -> {
                ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(k);
                Map<String, Object> objectiveConfigMap = (Map<String, Object>) v;
                if (objectiveConfigType != null && MapUtils.isNotEmpty(objectiveConfigMap)) {
                    Map<String, VoiceEngineConfig> voiceEngineConfigMap = new LinkedHashMap<>();
                    for (Map.Entry<String, Object> entry : objectiveConfigMap.entrySet()) {
                        String key = entry.getKey();
                        Map<String, Object> value = (Map<String, Object>) entry.getValue();
                        if (StringUtils.isNotEmpty(key) && MapUtils.isNotEmpty(value)) {
                            String processVoiceEngine = SafeConverter.toString(value.get("processVoiceEngine"));
                            String submitVoiceEngine = SafeConverter.toString(value.get("submitVoiceEngine"));
                            VoiceEngineConfig voiceEngineConfig = new VoiceEngineConfig(processVoiceEngine, submitVoiceEngine);
                            voiceEngineConfigMap.put(key, voiceEngineConfig);
                        }
                    }
                    if (MapUtils.isNotEmpty(voiceEngineConfigMap)) {
                        configTypeMapMap.put(objectiveConfigType, voiceEngineConfigMap);
                    }
                }
            });
            return configTypeMapMap;
        }
        return null;
    }

    private VoiceEngineConfig matchConfig(Map<String, VoiceEngineConfig> voiceEngineConfigMap, StudentDetail studentDetail) {
        String studentRegex;
        if (studentDetail.getStudentSchoolRegionCode() == null) {
            studentRegex = "000000";
        } else {
            studentRegex = studentDetail.getStudentSchoolRegionCode() + "";
        }
        if (studentDetail.getClazz() == null) {
            studentRegex = StringUtils.join(studentRegex, "000000", "_");
        } else {
            studentRegex = StringUtils.join(studentRegex, "_", studentDetail.getClazz().getSchoolId());
        }
        studentRegex = StringUtils.join(studentRegex, "_", studentDetail.getId());
        for (Map.Entry<String, VoiceEngineConfig> entry : voiceEngineConfigMap.entrySet()) {
            try {
                if (studentRegex.matches(entry.getKey())) {
                    return entry.getValue();
                }
            } catch (Exception e) {
                // do nothing here
            }
        }
        return null;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class VoiceEngineConfig implements Serializable {
        private static final long serialVersionUID = -4528245174365266452L;

        private String processVoiceEngine;
        private String submitVoiceEngine;
    }
}
