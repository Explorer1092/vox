/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.web.UrlUtils;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;

import javax.inject.Named;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static com.voxlearning.utopia.service.homework.api.constant.HomeworkType.CHINESE;

/**
 * 古诗活动卡片
 * @author majianxin
 */
@Named
public class LoadStudentAncientPoetryActivityCard extends AbstractStudentIndexDataLoader {

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        GroupMapper groupMapper = context.__studentGroups.stream()
                .filter(group -> group.getSubject().equals(Subject.CHINESE))
                .findFirst()
                .orElse(null);
        if (groupMapper != null) {

            MapMessage message = ancientPoetryLoaderClient.fetchGroupActivityList(groupMapper.getId(), Boolean.TRUE, context.getStudent().getId());
            if (message.isSuccess()) {
                String desc = "诗词大会";
                // 四川省特殊显示卡片名称为『四川省诗词大会』,510000为四川code
                Integer rootRegionCode = context.getStudent().getRootRegionCode();
                if (rootRegionCode != null && 510000 == rootRegionCode) {
                    desc = "四川省诗词大会";
                }
                List<Map<String, Object>> result = (List<Map<String, Object>>) message.get("result");
                for (Map<String, Object> resultMap : result) {
                    boolean passed = resultMap.get("passed") instanceof Boolean && (boolean) resultMap.get("passed");
                    if (!passed) {
                        Map<String, Object> card = generateCard((String) resultMap.get("activityId"), desc);
                        context.__ancientPoetryActivityCards.add(card);
                        break;
                    }
                }
            }
        }

        return context;
    }

    private Map<String, Object> generateCard(String activityId, String desc) {
        Map<String, Object> cardMap = new HashMap<>();
        cardMap.put("activityId", activityId);
        cardMap.put("desc", desc);
        cardMap.put("subject", Subject.CHINESE);
        cardMap.put("startComment", "学习古诗");
        cardMap.put("homeworkType", CHINESE);
        cardMap.put("types", Collections.singletonList("CHINESE_READING"));
        cardMap.put("url", UrlUtils.buildUrlQuery(NewHomeworkConstants.STUDENT_ANCIENT_POETRY_ACTIVITY_URL, MapUtils.m("activityId", activityId)));
        return cardMap;
    }

}
