package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.newhomework.api.mapper.WorkBookConfig;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

@Named
public class OcrMentalWorkBookConfigHelper {
    @Inject private PageBlockContentServiceClient pageBlockContentServiceClient;

    @SuppressWarnings("unchecked")
    public List<WorkBookConfig> loadConfig() {
        List<PageBlockContent> uniSoundScoreLevels = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("OcrMental");
        if (CollectionUtils.isNotEmpty(uniSoundScoreLevels)) {
            PageBlockContent configPageBlockContent = uniSoundScoreLevels.stream()
                    .filter(p -> "WorkBookConfig".equals(p.getBlockName()))
                    .findFirst()
                    .orElse(null);
            if (configPageBlockContent != null) {
                String configContent = configPageBlockContent.getContent();
                if (StringUtils.isBlank(configContent)) {
                    return null;
                }
                configContent = configContent.replaceAll("[\n\r\t]", "").trim();
//                List<OcrMentalWorkBookConfig> configs = JsonUtils.fromJsonToList(configContent, OcrMentalWorkBookConfig.class);
//                return configs;
                List<Map<String, Object>> configList = (List<Map<String, Object>>) JsonUtils.fromJson(configContent, List.class);
                if (CollectionUtils.isNotEmpty(configList)) {
                    List<WorkBookConfig> workBookConfigs = new ArrayList<>();
                    for (Map<String, Object> config : configList) {
                        WorkBookConfig workBookConfig = new WorkBookConfig();
                        workBookConfig.setPcode(SafeConverter.toInt(config.get("pcode")));
                        workBookConfig.setCcode(SafeConverter.toInt(config.get("ccode")));
                        workBookConfig.setAcode(SafeConverter.toInt(config.get("acode")));
                        workBookConfig.setWorkBookId(SafeConverter.toString(config.get("workBookId")));
                        workBookConfig.setPriority(SafeConverter.toInt(config.get("priority")));
                        workBookConfigs.add(workBookConfig);
                    }
                    return workBookConfigs;
                }
            }
        }
        return Collections.emptyList();
    }
}
