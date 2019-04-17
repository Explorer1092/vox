package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.google.common.collect.Lists;
import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.newhomework.api.constant.Vox8SentenceScoreLevel;
import com.voxlearning.utopia.service.newhomework.api.constant.Vox8SongScoreLevel;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @Description: 自研引擎分数等级帮助类
 * @author: Mr_VanGogh
 * @date: 2018/9/13 下午2:46
 */
@Named
public class VoxScoreLevelHelper {

    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    public List<Map<String, Object>> loadSentenceScoreLevels(StudentDetail studentDetail) {
        Map<String, Object> sentenceConfig = loadConfig("VoxSentenceConfig");
        return processConfig(sentenceConfig, studentDetail, Vox8SentenceScoreLevel.levels);
    }

    public List<Map<String, Object>> loadSongScoreLevels(StudentDetail studentDetail) {
        Map<String, Object> sentenceConfig = loadConfig("VoxSongConfig");
        return processConfig(sentenceConfig, studentDetail, Vox8SongScoreLevel.levels);
    }

    /**
     * 口语交际单句判分标准
     *
     * @param studentDetail
     * @return
     */
    public List<Map<String, Object>> loadVoxOralCommunicationSingleLevel(StudentDetail studentDetail) {
        Map<String, Object> sentenceConfig = loadConfig("VoxOralCommunicationSingleConfig");
        return parseOralConfig(sentenceConfig, studentDetail, Vox8SongScoreLevel.levels);
    }

    /**
     * 口语交际整个题包判分标准
     *
     * @return
     */
    public List<Map<String, Object>> loadVoxOralCommunicationTotalLevel(StudentDetail studentDetail) {
        Map<String, Object> sentenceConfig = loadConfig("VoxOralCommunicationTotalConfig");
        return processOralTotalConfig(sentenceConfig, studentDetail, Vox8SongScoreLevel.levels);
    }

    private Map<String, Object> loadConfig(String blockName) {
        List<PageBlockContent> vox17ScoreLevels = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("Vox17ScoreLevels");
        if (CollectionUtils.isNotEmpty(vox17ScoreLevels)) {
            PageBlockContent configPageBlockContent = vox17ScoreLevels.stream()
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
        Map<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> configMap = parseConfig(config);
        List<VoxScoreLevelHelper.ConfigScoreLevel> configScoreLevels = matchConfig(configMap, studentDetail);
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

    private List<Map<String, Object>> processOralTotalConfig(Map<String, Object> config, StudentDetail studentDetail, List<Map<String, Object>> defaultScoreLevels) {
        Map<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> configMap = parseOralConfig(config);
        List<VoxScoreLevelHelper.ConfigScoreLevel> configScoreLevels = matchConfig(configMap, studentDetail);
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

    private List<Map<String, Object>> parseOralConfig(Map<String, Object> config, StudentDetail studentDetail, List<Map<String, Object>> defaultScoreLevels) {
        Map<String, List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel>> configMap = parseOralCommunicationConfig(config);
        List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel> configScoreLevels = matchOralConfig(configMap, studentDetail);
        if (CollectionUtils.isEmpty(configScoreLevels)) {
            return defaultScoreLevels;
        }
        return configScoreLevels.stream()
                .map(configScoreLevel -> MapUtils.m(
                        "level", configScoreLevel.getLevel(),
                        "star", configScoreLevel.getStar(),
                        "integrityMinScore", configScoreLevel.getIntegrityMinScore(),
                        "integrityMaxScore", configScoreLevel.getIntegrityMaxScore(),
                        "pronunciationMinScore", configScoreLevel.getPronunciationMinScore(),
                        "pronunciationMaxScore", configScoreLevel.getPronunciationMaxScore()
                ))
                .collect(Collectors.toList());
    }

    private Map<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> parseOralConfig(Map<String, Object> config) {
        if (MapUtils.isNotEmpty(config)) {
            Map<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> configMap = new LinkedHashMap<>();
            config.forEach((k, v) -> {
                List<Map<String, Object>> configMapperList = (List<Map<String, Object>>) v;
                if (CollectionUtils.isNotEmpty(configMapperList)) {
                    List<VoxScoreLevelHelper.ConfigScoreLevel> configScoreLevels = new ArrayList<>();
                    for (Map<String, Object> mapper : configMapperList) {
                        if (mapper == null) {
                            return;
                        }
                        String level = SafeConverter.toString(mapper.get("level"));
                        if (StringUtils.isBlank(level)) {
                            return;
                        }
                        int minScore = SafeConverter.toInt(mapper.get("minScore"));
                        if (minScore < 0 || minScore > 100) {
                            return;
                        }
                        int maxScore = SafeConverter.toInt(mapper.get("maxScore"));
                        if (maxScore < 0 || maxScore > 100) {
                            return;
                        }
                        if (minScore > maxScore) {
                            return;
                        }
                        VoxScoreLevelHelper.ConfigScoreLevel configScoreLevel = new VoxScoreLevelHelper.ConfigScoreLevel(level, minScore, maxScore);
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


    @SuppressWarnings("unchecked")
    private Map<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> parseConfig(Map<String, Object> config) {
        if (MapUtils.isNotEmpty(config)) {
            Map<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> configMap = new LinkedHashMap<>();
            config.forEach((k, v) -> {
                List<Map<String, Object>> configMapperList = (List<Map<String, Object>>) v;
                if (CollectionUtils.isNotEmpty(configMapperList)) {
                    List<VoxScoreLevelHelper.ConfigScoreLevel> configScoreLevels = new ArrayList<>();
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
                        VoxScoreLevelHelper.ConfigScoreLevel configScoreLevel = new VoxScoreLevelHelper.ConfigScoreLevel(level, minScore, maxScore);
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

    private Map<String, List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel>> parseOralCommunicationConfig(Map<String, Object> config) {
        if (MapUtils.isNotEmpty(config)) {
            Map<String, List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel>> configMap = new LinkedHashMap<>();
            config.forEach((k, v) -> {
                List<Map<String, Object>> configMapperList = (List<Map<String, Object>>) v;
                if (CollectionUtils.isNotEmpty(configMapperList)) {
                    List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel> configScoreLevels = new ArrayList<>();
                    for (Map<String, Object> mapper : configMapperList) {
                        if (mapper == null) {
                            return;
                        }
                        String level = SafeConverter.toString(mapper.get("level"));
                        if (StringUtils.isBlank(level)) {
                            return;
                        }
                        int integrityMinScore = SafeConverter.toInt(mapper.get("integrityMinScore"));
                        if (integrityMinScore < 0 || integrityMinScore > 100) {
                            return;
                        }
                        int integrityMaxScore = SafeConverter.toInt(mapper.get("integrityMaxScore"));
                        if (integrityMaxScore < 0 || integrityMaxScore > 100) {
                            return;
                        }
                        int pronunciationMinScore = SafeConverter.toInt(mapper.get("pronunciationMinScore"), -1);
                        int pronunciationMaxScore = SafeConverter.toInt(mapper.get("pronunciationMaxScore"), -1);
                        int star = SafeConverter.toInt(mapper.get("star"));
                        VoxScoreLevelHelper.VoxOralCommunicationSingleLevel configScoreLevel = new VoxScoreLevelHelper.VoxOralCommunicationSingleLevel(level, star, integrityMinScore, integrityMaxScore, pronunciationMinScore, pronunciationMaxScore);
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


    private List<VoxScoreLevelHelper.ConfigScoreLevel> matchConfig(Map<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> configMap, StudentDetail studentDetail) {
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
            for (Map.Entry<String, List<VoxScoreLevelHelper.ConfigScoreLevel>> entry : configMap.entrySet()) {
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

    private List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel> matchOralConfig(Map<String, List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel>> configMap, StudentDetail studentDetail) {
        if (MapUtils.isEmpty(configMap)) {
            return null;
        }
        if (studentDetail == null) {
            for (Map.Entry<String, List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel>> entry : configMap.entrySet()) {
                try {
                    return entry.getValue();
                } catch (Exception e) {
                    // do nothing here
                }
            }
        }
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
        for (Map.Entry<String, List<VoxScoreLevelHelper.VoxOralCommunicationSingleLevel>> entry : configMap.entrySet()) {
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
    private class ConfigScoreLevel {
        private String level;
        private Integer minScore;
        private Integer maxScore;
    }

    /**
     * 口语交际评分维度：integrity，pronunciation ，keyStandardScore
     */
    @Getter
    @Setter
    @AllArgsConstructor
    private class VoxOralCommunicationSingleLevel implements Serializable {
        private static final long serialVersionUID = 1615529552561410274L;
        private String level;
        private Integer star;
        private Integer integrityMinScore;
        private Integer integrityMaxScore;
        private Integer pronunciationMinScore;
        private Integer pronunciationMaxScore;
    }
}
