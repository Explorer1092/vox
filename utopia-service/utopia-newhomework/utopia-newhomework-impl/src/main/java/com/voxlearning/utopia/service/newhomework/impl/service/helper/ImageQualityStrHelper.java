package com.voxlearning.utopia.service.newhomework.impl.service.helper;

import com.google.common.collect.Maps;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.service.config.api.entity.PageBlockContent;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

/**
 * \* Created: liuhuichao
 * \* Date: 2018/11/1
 * \* Time: 6:03 PM
 * \* Description:
 * \
 */
@Named
public class ImageQualityStrHelper {
    @Inject
    private PageBlockContentServiceClient pageBlockContentServiceClient;

    private Map<String, Object> loadConfig() {
        //读取页面内容的配置信息
        List<PageBlockContent> teacherTask = pageBlockContentServiceClient.getPageBlockContentBuffer()
                .findByPageName("ImageQualityStrConfig");
        if (CollectionUtils.isEmpty(teacherTask)) {
            return Maps.newHashMap();
        }
        PageBlockContent configPageBlockContent = teacherTask.stream()
                .filter(p -> "ImageQuality".equals(p.getBlockName()))
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


    public String getImageQualityStr(StudentDetail studentDetail) {
        Map<String, Object> config = loadConfig();
        if (MapUtils.isEmpty(config)) {
            return "";
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
        for (Map.Entry<String,Object> entry : config.entrySet()) {
            try {
                if (studentRegex.matches(entry.getKey())) {
                    return SafeConverter.toString(entry.getValue());
                }
            } catch (Exception e) {
                // do nothing here
            }
        }
        return "";
    }

}
