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

package com.voxlearning.washington.service.homework;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.consumer.NewAccomplishmentLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.user.api.constants.GroupType;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

// FIXME: temporary
@Named
public class LoadHomeworkHelper {

    @Inject private DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject private NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject private NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;

    // 返回true表示有作业，啥都甭干了，老实儿的做作业吧。。。
    public boolean hasUndoneHomework(StudentDetail student) {
        if (student == null || student.getClazz() == null) {
            return true;
        }

        // 获得分组信息
        List<GroupMapper> groupMappers = groupLoaderClient.loadStudentGroups(student.getId(), false);

        // english
        Long englishGroupId = 0L;
        for(GroupMapper gm : groupMappers){
            if(gm.getGroupType().equals(GroupType.TEACHER_GROUP) && gm.getSubject().equals(Subject.ENGLISH)){
                englishGroupId = gm.getId();
                break;
            }
        }
        if(hasUndoneNewHomework(englishGroupId, Subject.ENGLISH, student.getId())){
            return true;
        }


        // math
        Long mathGroupId = 0L;
        for(GroupMapper gm : groupMappers){
            if(gm.getGroupType().equals(GroupType.TEACHER_GROUP) && gm.getSubject().equals(Subject.MATH)){
                mathGroupId = gm.getId();
                break;
            }
        }
        if(hasUndoneNewHomework(mathGroupId, Subject.MATH, student.getId())){
            return true;
        }

        // chinese
        Long chineseGroupId = 0L;
        for(GroupMapper gm : groupMappers){
            if(gm.getGroupType().equals(GroupType.TEACHER_GROUP) && gm.getSubject().equals(Subject.CHINESE)){
                chineseGroupId = gm.getId();
                break;
            }
        }
        return hasUndoneNewHomework(chineseGroupId, Subject.CHINESE, student.getId());
    }

    private boolean hasUndoneNewHomework(Long groupId, Subject subject, Long studentId){
        List<NewHomework.Location> homeworks = newHomeworkLoaderClient.loadNewHomeworksByClazzGroupIds(groupId, subject);
        if(homeworks != null){
            NewHomework.Location location = homeworks.stream()
                    .filter(t -> !t.isChecked())
                    .filter(t -> t.getEndTime() > System.currentTimeMillis())
                    .sorted((o1, o2) -> Long.compare(o2.getCreateTime(), o1.getCreateTime()))
                    .findFirst()
                    .orElse(null);
            if (location != null) {
                NewAccomplishment accomplishment = newAccomplishmentLoaderClient.loadNewAccomplishment(location);
                if (accomplishment == null || accomplishment.getDetails() == null) {
                    return true;
                }
                if (!accomplishment.getDetails().containsKey(studentId.toString())) {
                    return true;
                }
            }
        }
        return false;
    }
}
