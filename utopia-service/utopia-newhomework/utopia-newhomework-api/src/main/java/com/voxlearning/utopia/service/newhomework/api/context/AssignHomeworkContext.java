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
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTag;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkType;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkBookInfo;
import com.voxlearning.utopia.service.newhomework.api.entity.base.NewHomeworkPracticeContent;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkSource;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.question.api.constant.ObjectiveConfigType;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import lombok.Getter;
import lombok.Setter;

import java.util.*;

/**
 * @author tanguohong
 * @version 0.1
 * @since 2016/1/6
 */
@Getter
@Setter
public class AssignHomeworkContext extends AbstractContext<AssignHomeworkContext> {
    private static final long serialVersionUID = -2233185755049321605L;

    private HomeworkSource source;
    private Teacher teacher;
    private HomeworkSourceType homeworkSourceType;
    private Date homeworkStartTime;
    private Date homeworkEndTime;
    private Long duration;
    private String remark;
    private NewHomeworkType newHomeworkType;
    private HomeworkTag homeworkTag;

    private boolean includeSubjective = false;
    private Set<Long> groupIdsWithHomeworkPrize = new HashSet<>(); // 这些组布置的作业是有奖励的
    private Set<Long> groupIds = new LinkedHashSet<>();
    // 组id， 作业实体
    private final LinkedHashMap<Long, NewHomework> assignedGroupHomework = new LinkedHashMap<>();
    private List<String> homeworkIds;
    private Map<Long, List<NewHomeworkPracticeContent>> groupPractices = new LinkedHashMap<>();
    private Map<Long, LinkedHashMap<ObjectiveConfigType, List<NewHomeworkBookInfo>>> groupPracticesBooksMap = new LinkedHashMap<>();
    private Map<Long, Long> groupDurations = new LinkedHashMap<>();
}
