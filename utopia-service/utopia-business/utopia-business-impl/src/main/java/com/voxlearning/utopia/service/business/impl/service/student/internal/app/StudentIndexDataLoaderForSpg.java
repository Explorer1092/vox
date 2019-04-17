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

package com.voxlearning.utopia.service.business.impl.service.student.internal.app;

import com.voxlearning.utopia.service.business.impl.service.student.internal.LoadStudentGroup;
import com.voxlearning.utopia.service.business.impl.service.student.internal.LoadStudentHomeworkLocation;
import com.voxlearning.utopia.service.business.impl.service.student.internal.StudentIndexDataChain;
import com.voxlearning.utopia.service.business.impl.service.student.internal.StudentIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.card.LoadStudentAppHomeworkCard_English;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.card.LoadStudentAppHomeworkCard_Math;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.card.LoadStudentAppMakeUpHomeworkCard_English;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.card.LoadStudentAppMakeUpHomeworkCard_Math;

import javax.inject.Named;

/**
 * 给SPG使用的作业卡数据
 * Created by Shuai Huan on 2016/1/20.
 */
@Named
@StudentIndexDataChain({
        LoadStudentGroup.class,
        LoadStudentHomeworkLocation.class,
        LoadStudentAppHomeworkCard_English.class,
        LoadStudentAppHomeworkCard_Math.class,
        LoadStudentAppMakeUpHomeworkCard_English.class,
        LoadStudentAppMakeUpHomeworkCard_Math.class,
})
public class StudentIndexDataLoaderForSpg extends StudentIndexDataLoader {
}
