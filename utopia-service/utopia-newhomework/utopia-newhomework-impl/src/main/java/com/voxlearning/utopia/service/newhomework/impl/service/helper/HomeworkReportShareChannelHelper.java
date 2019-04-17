package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * @Description: 作业分享渠道
 * @author: Mr_VanGogh
 * @date: 2019/2/2 上午9:57
 */
@Named
public class HomeworkReportShareChannelHelper {
    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    public List<Integer> loadHomeworkReportShareChannel(TeacherDetail teacherDetail) {
        Map<String, Object> config = loadConfig();
        List<Integer> shareChannels = new ArrayList<>();
        if (MapUtils.isEmpty(config)) {
            return Collections.emptyList();
        }
        if (teacherDetail == null) {
            return Collections.emptyList();
        }
        String teacherRegex;
        if (teacherDetail.getRegionCode() == null) {
            teacherRegex = "000000";
        } else {
            teacherRegex = teacherDetail.getRegionCode() + "";
        }
        if (teacherDetail.getTeacherSchoolId() == null) {
            teacherRegex = StringUtils.join(teacherRegex, "000000", "_");
        } else {
            teacherRegex = StringUtils.join(teacherRegex, "_", teacherDetail.getTeacherSchoolId());
        }
        teacherRegex = StringUtils.join(teacherRegex, "_", teacherDetail.getId());
        for (Map.Entry<String,Object> entry : config.entrySet()) {
            try {
                if (teacherRegex.matches(entry.getKey())) {
                    shareChannels.addAll((List<Integer>)(entry.getValue()));
                    break;
                }
            } catch (Exception e) {
                // do nothing here
            }
        }
        return shareChannels;
    }

    private Map<String, Object> loadConfig() {
        //读取页面内容的配置信息
        List<PageBlockContent> teacherTask = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("HomeworkReportShareChannelConfig");
        if (CollectionUtils.isEmpty(teacherTask)) {
            return Maps.newHashMap();
        }
        PageBlockContent configPageBlockContent = teacherTask.stream()
                .filter(p -> "HomeworkReportShareChannel".equals(p.getBlockName()))
                .findFirst()
                .orElse(null);
        if (configPageBlockContent == null) {
            return Maps.newHashMap();
        }
        String configContent = configPageBlockContent.getContent();
        if (StringUtils.isBlank(configContent)) {
            return Maps.newHashMap();
        }
        configContent = configContent.replaceAll("[\n\r\t]", "").trim();
        return JsonUtils.convertJsonObjectToMap(configContent);
    }
}
