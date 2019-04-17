/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.StudentHomeworkAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkResultAnswer;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkResult;
import com.voxlearning.utopia.service.newhomework.impl.dao.NewAccomplishmentDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.StudentHomeworkAccomplishmentPersistence;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import org.springframework.dao.DuplicateKeyException;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Objects;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/15
 */
@Named
public class FH_CheckHomeworkFinished extends SpringContainerSupport implements FinishHomeworkTask {
    @Inject private NewAccomplishmentDao newAccomplishmentDao;
    @Inject private StudentHomeworkAccomplishmentPersistence studentHomeworkAccomplishmentPersistence;

    @Override
    public void execute(FinishHomeworkContext context) {
        NewHomeworkResult result = context.getResult();
        ObjectiveConfigType objectiveConfigType = context.getObjectiveConfigType();

        for (ObjectiveConfigType type : context.getHomework().findPracticeContents().keySet()) {
            if (Objects.equals(objectiveConfigType, type)) continue; // 当前练习类型肯定是已经完成了的
            if (MapUtils.isEmpty(result.getPractices())) {
                return;
            }
            NewHomeworkResultAnswer answer = result.getPractices().get(type);
            if (answer == null)
                return; // 这个类型还没有答题记录
            if (!answer.isFinished())
                return; // 未完成返回
        }

        context.setHomeworkFinished(true);

        NewHomework.Location location = context.getHomework().toLocation();
        NewAccomplishment.ID id = NewAccomplishment.ID.build(location.getCreateTime(), result.getSubject(), result.getHomeworkId());
        newAccomplishmentDao.studentFinished(id,
                context.getUserId(),
                new Date(),
                context.getIpImei(),
                context.getHomework().isHomeworkTerminated(),
                context.getClientType(),
                context.getClientName());
        saveMySqlStudentHomeworkAccomplishment(location, context);
    }

    /**
     * 保存mysql的那张特别大的表
     */
    private void saveMySqlStudentHomeworkAccomplishment(NewHomework.Location location, FinishHomeworkContext context) {
        try {
            StudentHomeworkAccomplishment accomplishment = new StudentHomeworkAccomplishment();
            accomplishment.setSubject(location.getSubject());
            accomplishment.setHomeworkType(HomeworkType.of(location.getType() != null ? location.getType().name() : NewHomeworkType.Normal.name()));
            accomplishment.setHomeworkId(location.getId());
            accomplishment.setStudentId(context.getUserId());
            accomplishment.setAccomplishTime(new Date());
            String ip = SafeConverter.toString(context.getIpImei(), "");
            if (ip.length() > 255) {
                ip = StringUtils.substring(ip, 0, 255);
            }
            accomplishment.setIp(ip);
            accomplishment.setRepair(context.getHomework().isHomeworkTerminated());

            studentHomeworkAccomplishmentPersistence.persist(accomplishment);
        } catch (Exception ex) {
            if (ex instanceof DuplicateKeyException) {
                logger.warn("Failed to persist StudentHomeworkAccomplishment duplicated，userId:{} homeworkId:{}", context.getUserId(), context.getHomeworkId());
            } else {
                logger.error("Failed to persist StudentHomeworkAccomplishment!userId:{} homeworkId:{}", context.getUserId(), context.getHomeworkId(), ex);
            }
        }
    }
}
