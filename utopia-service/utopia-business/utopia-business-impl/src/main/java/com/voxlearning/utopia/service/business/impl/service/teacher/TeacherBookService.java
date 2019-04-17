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

package com.voxlearning.utopia.service.business.impl.service.teacher;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.mapper.DisplayBookOfClazzMapper;
import com.voxlearning.utopia.service.business.impl.service.clazz.SameBookClazzFinder;
import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;
import com.voxlearning.utopia.service.business.impl.support.TeacherCallback;
import com.voxlearning.utopia.service.user.api.entities.UserExtensionAttribute;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.Validate;

import javax.inject.Named;
import java.util.Calendar;
import java.util.Collections;
import java.util.Date;
import java.util.List;

@Named
public class TeacherBookService extends BusinessServiceSpringBean {

    public List<DisplayBookOfClazzMapper> getSameBookClazz(Long teacherId, final Long clazzId) {
        try {
            return execute(teacherId, new TeacherCallback<List<DisplayBookOfClazzMapper>>() {

                public List<DisplayBookOfClazzMapper> callback(Teacher teacher) {
                    SameBookClazzFinder sameBookClazzFinder = applicationContext.getBean(SameBookClazzFinder.class);
                    return sameBookClazzFinder.find(teacher.getId(), clazzId, teacher.getSubject());
                }
            });
        } catch (Exception ex) {
            return Collections.emptyList();
        }
    }


    public boolean showedUpgradeClazzBookTip(Long userId) {
        Teacher teacher = teacherLoaderClient.loadTeacher(userId);
        if (teacher == null) {
            return false;
        }
        Date endDate = new Date();

        if (SchoolYear.newInstance().currentTerm() == Term.上学期) {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, Calendar.OCTOBER);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            endDate = DateUtils.getDayStart(calendar.getTime());
        } else {
            Calendar calendar = Calendar.getInstance();
            calendar.set(Calendar.MONTH, Calendar.FEBRUARY);
            calendar.set(Calendar.DAY_OF_MONTH, 1);
            endDate = DateUtils.getDayStart(calendar.getTime());
        }
        // 本学期还未升级课本
        // 升级时间要在截止日期之前
        // 必须是英语老师
        // 必须是新学期开始时间之前注册的老师
        UserExtensionAttribute userExtensionAttribute = userAttributeLoaderClient.loadUserExtensionAttributes(userId)
                .key(generateUpgradeBookKey())
                .findFirst();
        if (userExtensionAttribute == null
                && endDate.after(new Date())
                && teacher.matchSubject(Subject.ENGLISH)
                && SchoolYear.newInstance().currentTermDateRange().getStartDate().after(teacher.getCreateTime())) {
            return true;
        }
        return false;
    }

    public String generateUpgradeBookKey() {
        Calendar calendar = Calendar.getInstance();
        int year = calendar.get(Calendar.YEAR);
        return "UpgradeClazzBookKey:year=" + year + "&term=" + SchoolYear.newInstance().currentTerm().getKey();
    }

    private <R> R execute(Long teacherId, TeacherCallback<R> callback) {
        Validate.notNull(teacherId, "Teacher id must not be null");
        Validate.notNull(callback, "TeacherClazzCallback must not be null");
        Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);
        Validate.notNull(teacher, "Teacher %s not found", teacherId);
        return callback.callback(teacher);
    }
}
