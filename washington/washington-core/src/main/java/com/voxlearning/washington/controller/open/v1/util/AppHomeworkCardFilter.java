/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.open.v1.util;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.washington.support.PageBlockContentGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2016/10/24 10:18
 */
public class AppHomeworkCardFilter {
    public static Map<String, Object> getMappingInfo(PageBlockContentGenerator pageBlockContentGenerator) {
        //读取页面内容的配置信息
        String config = pageBlockContentGenerator.getPageBlockContentHtml("client_app_publish", "homeworkcard_objectiveConfigType_mapping");
        config = config.replace("\r", "").replace("\n", "").replace("\t", "");
        return JsonUtils.fromJson(config);
    }

    // 根据作业类型，从nativeHomeworkCardMapping拿出详细信息。
    // 使用内部类HomeworkCardInfo
    public static HomeworkCardInfo generateHomeworkCardInfo(Map<String, Object> configMap, String homeworkType, List<String> types, String ver, String sys) {
        HomeworkCardInfo info = new HomeworkCardInfo();
        if (MapUtils.isNotEmpty(configMap)) {
            sys = StringUtils.lowerCase(sys);
            HomeworkCardSupportType supportType = HomeworkCardSupportType.SUPPORTED;
            Map<String, Object> contentMap = JsonUtils.fromJson(JsonUtils.toJson(configMap.get(homeworkType)));
            // 这个值如果命中了，则表示压根不支持某种作业卡，线上的新版本也不支持
            // 知道某天线上的新版本支持某类作业卡了，再将其从配置中去掉
            Map notSupportCardList = (Map) configMap.get("NOT_SUPPORT_LIST");
            if (contentMap != null) {
                for (String type : contentMap.keySet()) {
                    if (types != null && types.contains(type)) {
                        Map<String, Object> typeMap = JsonUtils.fromJson(JsonUtils.toJson(contentMap.get(type)));
                        info.setSourceType((String) typeMap.get("source_type"));
                        info.setHomeworkOrQuiz((String) typeMap.get("homework_or_quiz"));
                        //因为NEWEXAM模考不存在作业类型，因此ObjectiveConfigType中不存在，此处判断是否是模考
                        ObjectiveConfigType objectiveConfigType = ObjectiveConfigType.of(type);
                        if (objectiveConfigType != null) {
                            info.setNoSupportObjectiveConfigType(objectiveConfigType.getValue());
                        }else if("NEWEXAM".equals(type)){
                            info.setNoSupportObjectiveConfigType("模拟考试");
                        }else if("VACATION".equals(type)){
                            info.setNoSupportObjectiveConfigType("假期作业");
                        }else if("BASIC_REVIEW".equals(type)) {
                            info.setNoSupportObjectiveConfigType("期末基础复习作业");
                        }
                        // 如果能找到作业卡，但是当前版本(ver)与此作业卡可以支持的版本配置(version_support)不匹配，
                        // 则需参照NOT_SUPPORT_LIST列表中是否存在此作业卡类型，若存在，则表示压根不支持，否则提示升级新版本
                        if (!VersionUtil.checkVersionConfig((String) typeMap.get("version_support_" + sys), ver)) {
                            if (notSupportCardList != null && notSupportCardList.get(sys) != null) {
                                Map notSupportCardMap = (Map) notSupportCardList.get(sys);
                                List list = (List) notSupportCardMap.get(homeworkType);
                                if (null != list && list.contains(type)) {
                                    supportType = HomeworkCardSupportType.NOT_SUPPORTED;
                                    break;
                                } else {
                                    supportType = HomeworkCardSupportType.NEW_VERSION_SUPPORTED;
                                    break;
                                }
                            }
                        }
                    }
                }
            } else {
                supportType = HomeworkCardSupportType.NOT_SUPPORTED;
            }
            info.setSupportType(supportType);
        }
        return info;
    }

    @Getter
    @Setter
    public static class HomeworkCardInfo {
        private String sourceType;                       // 作业类型，h5 or native
        private String homeworkOrQuiz;                   // 作业分类，homework or quiz or vh_homework
        private HomeworkCardSupportType supportType;     // 作业卡的"支持"属性分类。1：支持，2：新版支持（用户升级之后就可以使用），3：不支持
        private String noSupportObjectiveConfigType;     // 不支持的作业形式，BASIC_APP，READING等形式
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum HomeworkCardSupportType {
        SUPPORTED(1),                                                    // 支持
        NEW_VERSION_SUPPORTED(2),                                        // 新版支持（用户升级之后就可以使用）
        NOT_SUPPORTED(3);                                                // 不支持
        @Getter
        private final int code;
    }
}
