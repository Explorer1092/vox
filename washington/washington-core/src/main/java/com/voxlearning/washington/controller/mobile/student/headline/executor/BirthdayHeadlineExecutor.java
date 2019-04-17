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

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.zone.api.constant.ClazzJournalType;
import com.voxlearning.utopia.service.zone.api.entity.ClazzJournal;
import com.voxlearning.washington.controller.mobile.student.headline.helper.AbstractHeadlineExecutor;
import com.voxlearning.washington.controller.mobile.student.headline.helper.HeadlineMapperContext;
import com.voxlearning.washington.mapper.studentheadline.StudentBirthdayHeadlineMapper;
import com.voxlearning.washington.mapper.studentheadline.StudentHeadlineMapper;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created with IntelliJ IDEA.
 * User: qianxiaozhi
 * Date: 2017/1/6
 * Time: 14:29
 */
@Named
public class BirthdayHeadlineExecutor extends AbstractHeadlineExecutor {

    @Inject private RaikouSystem raikouSystem;

    @Override
    public List<ClazzJournalType> journalTypes() {
        return Collections.singletonList(ClazzJournalType.BIRTHDAY);
    }

    @Override
    public StudentHeadlineMapper generateMapper(ClazzJournal clazzJournal, HeadlineMapperContext context) {

        if (context == null || context.getCurrentUserId() == null) {
            return null;
        }
        final Long userId = context.getCurrentUserId();

        String birthdayJson = clazzJournal.getJournalJson();
        if (StringUtils.isBlank(birthdayJson)) {
            return null;
        }
        Map<String, Object> mJson = JsonUtils.fromJson(birthdayJson);
        if (MapUtils.isEmpty(mJson) || !mJson.containsKey("studentId")) {
            return null;
        }

        Long relevantUserId = SafeConverter.toLong(mJson.get("studentId"));
        User relevantUser = raikouSystem.loadUser(relevantUserId);
        if (relevantUser == null) {
            return null;
        }

        // 不是同一个group的同学 则无权查看该同学的生日信息
        if (!mobileStudentClazzHelper.isClassmate(clazzJournal.getClazzId(), relevantUserId, context)) {
            return null;
        }

        String filterKey = String.format("STUDENT_APP_HEADLINE_STUDENT_BIRTHDAY_%s", relevantUserId);
        String content = washingtonCacheSystem.CBS.flushable.load(filterKey);
        if (isLimitBirthday(content, clazzJournal.getId() + "")) {
            return null;
        }

        int month = relevantUser.getProfile().getMonth() == null ? 0 : relevantUser.getProfile().getMonth();
        int day = relevantUser.getProfile().getDay() == null ? 0 : relevantUser.getProfile().getDay();
        if (!isInTimeRange(month, day)) {
            return null;
        }

        StudentBirthdayHeadlineMapper mapper = new StudentBirthdayHeadlineMapper();

        fillInteractiveMapper(mapper, clazzJournal, relevantUser, context);

        mapper.setVId(String.valueOf(clazzJournal.getId()));
        mapper.setType(ClazzJournalType.BIRTHDAY_HEADLINE.name());
        mapper.setRelevantUserId(relevantUserId);
        mapper.setHeadIcon(relevantUser.fetchImageUrl());
        mapper.setTitle("生日祝福");
        mapper.setText(String.format("%s月%s日", month, day));
        // 个人不展示祝福按钮
        mapper.setShowBtn(!Objects.equals(relevantUserId, userId));

        // 头饰处理
        mapper.setHeadWearImg(mobileStudentClazzHelper.getHeadWear(relevantUserId));

        // 缓存 动态id_展示的年份
        String sbContent = clazzJournal.getId() + "_" + Calendar.getInstance().get(Calendar.YEAR);
        // FIXME I am just wondering that whether this could work correctly or not...
        washingtonCacheSystem.CBS.flushable.set(filterKey, 365 * 24 * 60 * 60, sbContent);

        return mapper;
    }

    /**
     * 限制一个同学一年只能有一次生日动态 缓存内容 动态id_展示的年份
     */
    private boolean isLimitBirthday(String content, String sJournalId) {
        // 缓存为空 则展示生日动态
        if (StringUtils.isBlank(content)) {
            return false;
        }
        String[] ss = content.split("_");
        // 缓存格式不对 则展示生日动态
        if (ss.length != 2) {
            return false;
        }
        // 如果是当前动态 则展示生日动态
        if (StringUtils.equals(sJournalId, ss[0])) {
            return false;
        }
        // 判断当前自然年是否展示当前动态
        Calendar calendar = Calendar.getInstance();
        int currentYear = calendar.get(Calendar.YEAR);
        return (StringUtils.equals(ss[1], currentYear + ""));
    }

    /**
     * 如果用户修改生日 则判断修改后的生日 是否在合理范围内， 规则： 生日日期 和 当前日期的差值在10天内，则视为合理范围
     *
     * @param month 生日月份
     * @param date  生日日期
     */
    private boolean isInTimeRange(int month, int date) {
        Calendar ori = Calendar.getInstance();
        ori.set(Calendar.MONTH, month - 1);
        ori.set(Calendar.DATE, date);
        Calendar current = Calendar.getInstance();
        int currentDOY = current.get(Calendar.DAY_OF_YEAR);
        int oriDOY = ori.get(Calendar.DAY_OF_YEAR);
        int difference = Math.abs(currentDOY - oriDOY);
        return (difference <= 10);
    }
}
