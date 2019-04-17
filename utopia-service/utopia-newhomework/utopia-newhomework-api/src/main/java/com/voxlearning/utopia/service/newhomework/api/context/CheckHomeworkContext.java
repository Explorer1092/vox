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

package com.voxlearning.utopia.service.newhomework.api.context;

import com.voxlearning.utopia.service.homework.api.constant.HomeworkSourceType;
import com.voxlearning.utopia.service.homework.api.constant.HomeworkType;
import com.voxlearning.utopia.service.homework.api.mapper.CheckHomeworkIntegralDetail;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * @author Ruib
 * @version 0.1
 * @since 2016/1/25
 */
@Getter
@Setter
public class CheckHomeworkContext extends AbstractContext<CheckHomeworkContext> {
    private static final long serialVersionUID = -548590441484167802L;

    // in
    private Teacher teacher;
    private String homeworkId;
    private HomeworkSourceType checkHomeworkSource;

    // middle
    private Long teacherId;
    private NewHomework homework;
    private HomeworkType homeworkType;
    private Long clazzId;
    private Clazz clazz;
    private Long groupId;
    private GroupMapper group;

    private List<StudentDetail> students; // 班级中的所有学生
    private NewAccomplishment accomplishment; // 完成作业实体，可能为null
    private boolean cheated = false; // 是否作弊
    private boolean homeworkQuantityNotEnough = false; // 作业题量过少，教师没有金币奖励
    private CheckHomeworkIntegralDetail detail; // 积分计算结果
    private int weekCheckTime = -1; // 本周检查作业次数（不包括本次）
}
