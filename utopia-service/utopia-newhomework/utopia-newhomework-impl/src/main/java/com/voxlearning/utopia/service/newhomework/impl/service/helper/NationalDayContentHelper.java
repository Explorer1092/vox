package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
public class NationalDayContentHelper {
    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @SuppressWarnings("unchecked")
    public List<ConfigContent> loadEnglishConfigContent() {
        List<PageBlockContent> nationalDayHomeworkContents = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("NationalDayHomework");
        if (CollectionUtils.isNotEmpty(nationalDayHomeworkContents)) {
            PageBlockContent configPageBlockContent = nationalDayHomeworkContents.stream()
                    .filter(p -> "EnglishContent".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                List<Map> configContents = JsonUtils.fromJsonToList(configContent, Map.class);
                List<ConfigContent> configContentList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(configContents)) {
                    for (Map map : configContents) {
                        int clazzLevel = SafeConverter.toInt(map.get("clazzLevel"));
                        int startClazzLevel = SafeConverter.toInt(map.get("startClazzLevel"));
                        List<String> dubbingIds = (List<String>) map.get("dubbingIds");
                        List<String> pictureBookPlusIds = (List<String>) map.get("pictureBookPlusIds");
                        ConfigContent content = new ConfigContent(clazzLevel, startClazzLevel, dubbingIds, pictureBookPlusIds);
                        configContentList.add(content);
                    }
                }
                return configContentList;
            }
        }
        return Collections.emptyList();
    }

    @SuppressWarnings("unchecked")
    public List<ConfigContent> loadChineseConfigContent() {
        List<PageBlockContent> nationalDayHomeworkContents = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("NationalDayHomework");
        if (CollectionUtils.isNotEmpty(nationalDayHomeworkContents)) {
            PageBlockContent configPageBlockContent = nationalDayHomeworkContents.stream()
                    .filter(p -> "ChineseContent".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
                List<Map> configContents = JsonUtils.fromJsonToList(configContent, Map.class);
                List<ConfigContent> configContentList = new ArrayList<>();
                if (CollectionUtils.isNotEmpty(configContents)) {
                    for (Map map : configContents) {
                        int clazzLevel = SafeConverter.toInt(map.get("clazzLevel"));
                        List<String> pictureBookPlusIds = (List<String>) map.get("pictureBookPlusIds");
                        ConfigContent content = new ConfigContent(clazzLevel, null, null, pictureBookPlusIds);
                        configContentList.add(content);
                    }
                }
                return configContentList;
            }
        }
        return Collections.emptyList();
    }

    @Getter
    @Setter
    @AllArgsConstructor
    public static class ConfigContent implements Serializable {

        private static final long serialVersionUID = -3256613755783211268L;

        private Integer clazzLevel;
        private Integer startClazzLevel;
        private List<String> dubbingIds;
        private List<String> pictureBookPlusIds;
    }
}
