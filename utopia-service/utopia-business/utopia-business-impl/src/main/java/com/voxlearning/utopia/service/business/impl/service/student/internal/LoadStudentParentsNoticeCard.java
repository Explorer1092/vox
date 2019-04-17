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

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;

import javax.inject.Named;
import java.util.Map;

/**
 * Created by tanguohong on 14-11-13.
 */
@Named
public class LoadStudentParentsNoticeCard extends AbstractStudentIndexDataLoader {

    // FIXME dirty hack
    // FIXME 要在校讯通的卡片里加入收老师感谢的内容

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        boolean showParentsNoticecard = true;

        boolean displayNoviceCard = SafeConverter.toBoolean(context.getParam().get("displayNoviceCard"));
        if (displayNoviceCard) {
            Map map = JsonUtils.fromJson(JsonUtils.toJson(context.getParam().get("taskMapper")), Map.class);
            boolean parentWechatBinded = conversionService.convert(map.get("parentWechatBinded"), Boolean.class);
            if (parentWechatBinded) {
                showParentsNoticecard = false;
            }
        } else {
            showParentsNoticecard = false;
        }
        context.getParam().put("showParentsNoticecard", showParentsNoticecard);
        return context;
    }
}
