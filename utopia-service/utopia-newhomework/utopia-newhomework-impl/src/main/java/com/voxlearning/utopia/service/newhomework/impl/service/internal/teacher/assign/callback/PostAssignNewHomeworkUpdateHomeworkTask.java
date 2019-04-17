package com.voxlearning.utopia.service.newhomework.impl.service.internal.teacher.assign.callback;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostAssignHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.AssignHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkTaskRecord;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkTask;
import com.voxlearning.utopia.service.newhomework.api.util.HomeworkTaskUtils;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkCardTaskFilter;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.api.mappers.ClazzGroup;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * @author guoqiang.li
 * @since 2017/4/20
 */
@Named
public class PostAssignNewHomeworkUpdateHomeworkTask extends NewHomeworkSpringBean implements PostAssignHomework {
    @Inject private HomeworkCardTaskFilter homeworkCardTaskFilter;

    @Override
    public void afterHomeworkAssigned(Teacher teacher, AssignHomeworkContext context) {
        // 获取任务列表
        List<HomeworkTask> taskList = homeworkCardTaskFilter.loadValidHomeworkTaskList();
        // 获取老师主学科账号
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacher.getId());
        if (mainTeacherId == null) {
            mainTeacherId = teacher.getId();
        }
        Teacher mainTeacher = teacherLoaderClient.loadTeacher(mainTeacherId);
        if (mainTeacher == null) {
            return;
        }
        Set<Long> groupIds = context.getAssignedGroupHomework().keySet();
        if (CollectionUtils.isEmpty(groupIds)) {
            return;
        }
        for (HomeworkTask homeworkTask : taskList) {
            HomeworkTaskType taskType = homeworkTask.getHomeworkTaskType();
            switch (taskType) {
                case DAILY_HOMEWORK:
                    updateDailyHomeworkTask(homeworkTask, mainTeacher, taskType, groupIds);
                    break;
                case WEEKEND_HOMEWORK:
                    updateWeekendHomeworkTask(homeworkTask, mainTeacher, taskType, groupIds);
                    break;
                case VACATION_HOMEWORK:
                    break;
                case ACTIVITY_HOMEWORK:
                    updateActivityHomeworkTask(homeworkTask, mainTeacher, taskType, groupIds, context.getHomeworkEndTime());
                    break;
            }
        }
    }

    private void updateDailyHomeworkTask(HomeworkTask homeworkTask, Teacher mainTeacher, HomeworkTaskType taskType, Set<Long> groupIds) {
        Subject subject = mainTeacher.getSubject();
        int taskNeedDays = subject == Subject.ENGLISH ? 4 : 3;

        Long mainTeacherId = mainTeacher.getId();
        Integer taskId = homeworkTask.getTaskId();
        String taskPeriod = HomeworkTaskUtils.calculateDailyTaskPeriod();
        String recordId = HomeworkTaskRecord.generateId(mainTeacherId, taskId, taskType, taskPeriod);
        int currentDay = HomeworkTaskUtils.calculateDayOfWeek();

        // 查询库里面有没有这条数据，没有则创建
        HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
        if (taskRecord == null) {
            taskRecord = initTaskRecord(recordId, mainTeacherId, taskPeriod, taskId, homeworkTask.getIntegralCount(), taskType);
        }
        Map<String, Boolean> details = taskRecord.getDetails();
        for (Long groupId : groupIds) {
            String key = currentDay + "|" + groupId;
            details.put(key, true);
        }
        Map<Integer, List<Long>> dailyDetails = taskRecord.findDailyDetails();
        int assignedDayCount = dailyDetails.size();
        // 日常任务的完成状态直接由这里来更新
        if (assignedDayCount >= taskNeedDays && taskRecord.getTaskStatus() == HomeworkTaskStatus.UNFINISHED) {
            taskRecord.setTaskStatus(HomeworkTaskStatus.FINISHED);
        }
        homeworkTaskRecordDao.upsert(taskRecord);
    }

    private void updateWeekendHomeworkTask(HomeworkTask homeworkTask, Teacher mainTeacher, HomeworkTaskType taskType, Set<Long> groupIds) {
        int currentDay = HomeworkTaskUtils.calculateDayOfWeek();
        // 判断今天是不是周五，周五才更新数据
        if (currentDay == 4) {
            Long mainTeacherId = mainTeacher.getId();
            Integer taskId = homeworkTask.getTaskId();
            String taskPeriod = HomeworkTaskUtils.calculateWeekendTaskPeriod();
            String recordId = HomeworkTaskRecord.generateId(mainTeacherId, taskId, taskType, taskPeriod);

            // 查询库里面有没有这条数据，没有则创建
            HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
            if (taskRecord == null) {
                taskRecord = initTaskRecord(recordId, mainTeacherId, taskPeriod, taskId, homeworkTask.getIntegralCount(), taskType);
            }
            Map<String, Boolean> details = taskRecord.getDetails();
            for (Long groupId : groupIds) {
                details.put(groupId.toString(), false);
            }
            homeworkTaskRecordDao.upsert(taskRecord);
        }
    }

    private void updateActivityHomeworkTask(HomeworkTask homeworkTask, Teacher mainTeacher, HomeworkTaskType taskType, Set<Long> groupIds, Date homeworkEndTime) {
        // 获取任务id
        int taskId = SafeConverter.toInt(homeworkTask.getTaskId());
        Long mainTeacherId = mainTeacher.getId();
        String taskPeriod = HomeworkTaskUtils.calculateActivityTaskPeriod(homeworkTask.getStartTime(), homeworkTask.getEndTime());
        String recordId = HomeworkTaskRecord.generateId(mainTeacherId, taskId, taskType, taskPeriod);
        switch (taskId) {
            // 2017端午小长假作业任务
            case 3:
                // 判断作业截止时间是否在5月28至5月30
                if (homeworkEndTime.after(NewHomeworkConstants.DRAGON_BOAT_FESTIVAL_DATE_EARLIEST)
                        && homeworkEndTime.before(NewHomeworkConstants.DRAGON_BOAT_FESTIVAL_DATE_LATEST)) {
                    HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
                    if (taskRecord == null) {
                        taskRecord = initTaskRecord(recordId, mainTeacherId, taskPeriod, taskId, homeworkTask.getIntegralCount(), taskType);
                    }
                    Map<String, Boolean> details = taskRecord.getDetails();
                    for (Long groupId : groupIds) {
                        if (!details.containsKey(groupId.toString())) {
                            details.put(groupId.toString(), false);
                        }
                    }
                    homeworkTaskRecordDao.upsert(taskRecord);
                }
                break;
            default:
                break;
        }
    }

    private HomeworkTaskRecord initTaskRecord(String recordId,
                                              Long teacherId,
                                              String taskPeriod,
                                              Integer taskId,
                                              Integer integralCount,
                                              HomeworkTaskType taskType) {
        HomeworkTaskRecord taskRecord = new HomeworkTaskRecord();
        taskRecord.setId(recordId);
        taskRecord.setTeacherId(teacherId);
        taskRecord.setTaskPeriod(taskPeriod);
        taskRecord.setTaskId(taskId);
        taskRecord.setIntegralCount(integralCount);
        taskRecord.setTaskType(taskType);
        taskRecord.setTaskStatus(HomeworkTaskStatus.UNFINISHED);
        taskRecord.setDetails(new LinkedHashMap<>());
        return taskRecord;
    }
}
