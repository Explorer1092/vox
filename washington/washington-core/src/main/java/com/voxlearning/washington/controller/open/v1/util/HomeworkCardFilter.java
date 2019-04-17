package com.voxlearning.washington.controller.open.v1.util;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.washington.support.PageBlockContentGenerator;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import lombok.Setter;

import java.util.List;
import java.util.Map;

/**
 * Created by Shuai Huan on 2015/10/22.
 */
public class HomeworkCardFilter {

    public static Map<String, Object> getMappingInfo(PageBlockContentGenerator pageBlockContentGenerator) {
        String config = pageBlockContentGenerator.getPageBlockContentHtml("client_app_publish", "homeworkcard_mapping");
        config = config.replace("\r", "").replace("\n", "").replace("\t", "");
        return JsonUtils.fromJson(config);
    }

    // 根据作业类型，从nativeHomeworkCardMapping拿出详细信息。
    // 使用内部类HomeworkCardInfo
    public static HomeworkCardInfo generateHomeworkCardInfo(Map<String, Object> configMap, String homeworkType, String ver, String sys) {
        HomeworkCardInfo info = new HomeworkCardInfo();
        if (MapUtils.isNotEmpty(configMap)) {
            sys = StringUtils.lowerCase(sys);
            HomeworkCardSupportType supportType = HomeworkCardSupportType.SUPPORTED;
            Map contentMap = (Map) configMap.get(homeworkType);
            // 这个值如果命中了，则表示压根不支持某种作业卡，线上的新版本也不支持
            // 知道某天线上的新版本支持某类作业卡了，再将其从配置中去掉
            Map notSupportCardList = (Map) configMap.get("NOT_SUPPORT_LIST");
            if (contentMap != null) {
                info.setSourceType((String) contentMap.get("source_type"));
                info.setHomeworkOrQuiz((String) contentMap.get("homework_or_quiz"));
                // 如果能找到作业卡，但是当前版本(ver)与此作业卡可以支持的版本配置(version_support)不匹配，
                // 则需参照NOT_SUPPORT_LIST列表中是否存在此作业卡类型，若存在，则表示压根不支持，否则提示升级新版本
                if (!VersionUtil.checkVersionConfig((String) contentMap.get("version_support_" + sys), ver)) {
                    if (notSupportCardList != null && notSupportCardList.get(sys) != null) {
                        List list = (List) notSupportCardList.get(sys);
                        if (list.contains(homeworkType)) {
                            supportType = HomeworkCardSupportType.NOT_SUPPORTED;
                        } else {
                            supportType = HomeworkCardSupportType.NEW_VERION_SUPPORTED;
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

    public static class HomeworkCardInfo {
        @Getter @Setter private String sourceType;                                          // 作业类型，h5 or native
        @Getter @Setter private String homeworkOrQuiz;                                      // 作业分类，homework or quiz or vh_homework
        @Getter @Setter private HomeworkCardSupportType supportType;                        // 作业卡的"支持"属性分类。1：支持，2：新版支持（用户升级之后就可以使用），3：不支持
    }

    @RequiredArgsConstructor(access = AccessLevel.PRIVATE)
    public enum HomeworkCardSupportType {
        SUPPORTED(1),                                                   // 支持
        NEW_VERION_SUPPORTED(2),                                        // 新版支持（用户升级之后就可以使用）
        NOT_SUPPORTED(3);                                               // 不支持
        @Getter private final int code;
    }
}
