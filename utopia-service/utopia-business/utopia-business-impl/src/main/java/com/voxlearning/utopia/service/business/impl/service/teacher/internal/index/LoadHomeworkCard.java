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

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.index;

import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.spi.monitor.FlightRecorder;
import com.voxlearning.utopia.mapper.HomeworkMapper;
import com.voxlearning.utopia.service.business.impl.service.teacher.TeacherHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkState;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.List;

/**
 * 教师端首页的班级作业列表
 *
 * @author RuiBao
 * @version 0.1
 * @since 13-8-7
 */
@Named
public class LoadHomeworkCard extends AbstractTeacherIndexDataLoader {
    @Inject private TeacherHomeworkServiceImpl teacherHomeworkService;

    @Override
    protected TeacherIndexDataContext doProcess(TeacherIndexDataContext context) {
        FlightRecorder.dot("LCH_START");

        if (!context.isSkipNextAll()) {
            Teacher teacher = context.getTeacher();
            List<HomeworkMapper> assignHomeworkList = new ArrayList<>();
            List<HomeworkMapper> checkHomeworkList = new ArrayList<>();
            List<HomeworkMapper> ongingHomeworkList = new ArrayList<>();

            // 试试新的方法
            //List<HomeworkMapper> homeworkList = teacherHomeworkService.getHomeworkMapperList(teacher.getId(), teacher.getSubject());
            List<HomeworkMapper> homeworkList = teacherHomeworkService.getClazzGroupHomeworkMappers(teacher.getId(), teacher.getSubject());
            for (HomeworkMapper mp : homeworkList) {
                if (HomeworkState.ASSIGN_HOMEWORK.equals(mp.getState())) {
                    assignHomeworkList.add(mp);
                }
                if (HomeworkState.CHECK_HOMEWORK.equals(mp.getState())) {
                    checkHomeworkList.add(mp);
                }
                if (HomeworkState.ADJUST_DELETE_HOMEWORK.equals(mp.getState())) {
                    ongingHomeworkList.add(mp);
                }
            }

            FlightRecorder.dot("LCH_JSON");

            context.getParam().put("assignHomeworkList", JsonUtils.toJson(assignHomeworkList));
            context.getParam().put("checkHomeworkList", JsonUtils.toJson(checkHomeworkList));
            context.getParam().put("ongingHomeworkList", JsonUtils.toJson(ongingHomeworkList));
        }

        FlightRecorder.dot("LCH_END");
        return context;
    }
}
