package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.api.constant.Unisound8SentenceScoreLevel;
import com.voxlearning.utopia.api.constant.Unisound8WordScoreLevel;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.WordTeachUniSound7SentenceScoreLevel;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 云知声分数等级帮助类
 *
 * @author guoqiang.li
 * @since 2017/9/12
 */
@Named
public class UniSoundScoreLevelHelper {
    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    public List<Map<String, Object>> loadWordScoreLevels(StudentDetail studentDetail) {
        Map<String, Object> wordConfig = loadConfig("WordConfig");
        return processConfig(wordConfig, studentDetail, Unisound8WordScoreLevel.levels);
    }

    public List<Map<String, Object>> loadSentenceScoreLevels(StudentDetail studentDetail) {
        Map<String, Object> sentenceConfig = loadConfig("SentenceConfig");
        return processConfig(sentenceConfig, studentDetail, Unisound8SentenceScoreLevel.levels);
    }

    public List<Map<String, Object>> loadUniSoundWordTeachSentenceScoreLevels(StudentDetail studentDetail) {
        Map<String, Object> sentenceConfig = loadConfig("WordTeachSentenceConfig");
        return processConfig(sentenceConfig, studentDetail, WordTeachUniSound7SentenceScoreLevel.levels);
    }

    private Map<String, Object> loadConfig(String blockName) {
        List<PageBlockContent> uniSoundScoreLevels = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("UniSoundScoreLevels");
        if (CollectionUtils.isNotEmpty(uniSoundScoreLevels)) {
            PageBlockContent configPageBlockContent = uniSoundScoreLevels.stream()
                    .filter(p -> blockName.equals(p.getBlockName()))
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

    private List<Map<String, Object>> processConfig(Map<String, Object> config, StudentDetail studentDetail, List<Map<String, Object>> defaultScoreLevels) {
        Map<String, List<ConfigScoreLevel>> configMap = parseConfig(config);
        List<ConfigScoreLevel> configScoreLevels = matchConfig(configMap, studentDetail);
        if (CollectionUtils.isNotEmpty(configScoreLevels)) {
            return configScoreLevels.stream()
                    .map(configScoreLevel -> MapUtils.m(
                            "level", configScoreLevel.getLevel(),
                            "minScore", configScoreLevel.getMinScore(),
                            "maxScore", configScoreLevel.getMaxScore()
                    ))
                    .collect(Collectors.toList());
        }
        return defaultScoreLevels;
    }

    @SuppressWarnings("unchecked")
    private Map<String, List<ConfigScoreLevel>> parseConfig(Map<String, Object> config) {
        if (MapUtils.isNotEmpty(config)) {
            Map<String, List<ConfigScoreLevel>> configMap = new LinkedHashMap<>();
            config.forEach((k, v) -> {
                List<Map<String, Object>> configMapperList = (List<Map<String, Object>>) v;
                if (CollectionUtils.isNotEmpty(configMapperList)) {
                    List<ConfigScoreLevel> configScoreLevels = new ArrayList<>();
                    for (Map<String, Object> mapper : configMapperList) {
                        if (mapper == null) {
                            return;
                        }
                        String level = SafeConverter.toString(mapper.get("level"));
                        if (StringUtils.isBlank(level)) {
                            return;
                        }
                        int minScore = SafeConverter.toInt(mapper.get("minScore"));
                        if (minScore < 0 || minScore > 8) {
                            return;
                        }
                        int maxScore = SafeConverter.toInt(mapper.get("maxScore"));
                        if (maxScore < 0 || maxScore > 8) {
                            return;
                        }
                        if (minScore > maxScore) {
                            return;
                        }
                        ConfigScoreLevel configScoreLevel = new ConfigScoreLevel(level, minScore, maxScore);
                        configScoreLevels.add(configScoreLevel);
                    }
                    if (CollectionUtils.isNotEmpty(configScoreLevels)) {
                        configMap.put(k, configScoreLevels);
                    }
                }
            });
            return configMap;
        }
        return null;
    }

    private List<ConfigScoreLevel> matchConfig(Map<String, List<ConfigScoreLevel>> configMap, StudentDetail studentDetail) {
        if (MapUtils.isNotEmpty(configMap)) {
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
            for (Map.Entry<String, List<ConfigScoreLevel>> entry : configMap.entrySet()) {
                try {
                    if (studentRegex.matches(entry.getKey())) {
                        return entry.getValue();
                    }
                } catch (Exception e) {
                    // do nothing here
                }
            }
        }
        return null;
    }

    @Getter
    @Setter
    @AllArgsConstructor
    private class ConfigScoreLevel {
        private String level;
        private Integer minScore;
        private Integer maxScore;
    }
}
