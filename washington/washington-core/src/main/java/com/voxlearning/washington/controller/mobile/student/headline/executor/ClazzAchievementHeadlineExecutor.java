/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.controller.mobile.student.headline.executor;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.action.api.support.AchievementType;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.StudentClazzAchievementHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/6
 * Time: 14:29
 */
@Named
public class ClazzAchievementHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.CLAZZ_ACHIEVEMENT_HEADLINE);
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {

        StudentClazzAchievementHeadlineMapper mapper = new StudentClazzAchievementHeadlineMapper();
        mapper.setType(ClazzJournalType.CLAZZ_ACHIEVEMENT_HEADLINE.name());
        mapper.setJournalId(clazzJournal.getId());
        mapper.initDateTime(clazzJournal.getCreateDatetime());

        Map<String, Object> extInfo = JsonUtils.fromJson(clazzJournal.getJournalJson());

        if (!extInfo.containsKey("achievementType") || !extInfo.containsKey("level")) return null;
        String achievementType = SafeConverter.toString(extInfo.get("achievementType"));
        AchievementType type = AchievementType.of(achievementType);
        if (null == type) return null;
        mapper.setAchievementTitle(type.getTitle());
        mapper.setAchievementType(achievementType);
        mapper.setLevel(SafeConverter.toInt(extInfo.get("level"), 0));

        User user = raikouSystem.loadUser(clazzJournal.getRelevantUserId());
        if (null == user) return null;
        mapper.setUserName(user.fetchRealname());
        mapper.setAvatar(user.fetchRealname());
        mapper.setUserId(user.getId());
        mapper.setHeadWear(mobileStudentClazzHelper.getHeadWear(user.getId()));

        mapper.setUserImg(user.fetchImageUrl());
        mapper.setHeadWearImg(mobileStudentClazzHelper.getHeadWear(user.getId()));

        return mapper;
    }

}
