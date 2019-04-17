package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.callback;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.newhomework.api.client.callback.PostFinishHomework;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskStatus;
import com.voxlearning.utopia.service.newhomework.api.constant.HomeworkTaskType;
import com.voxlearning.utopia.service.newhomework.api.constant.NewHomeworkConstants;
import com.voxlearning.utopia.service.newhomework.api.context.FinishHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkTaskRecord;
import com.voxlearning.utopia.service.newhomework.api.entity.NewAccomplishment;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkTask;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomework;
import com.voxlearning.utopia.service.newhomework.api.util.HomeworkTaskUtils;
import com.voxlearning.utopia.service.newhomework.impl.dao.HomeworkTaskRecordDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewAccomplishmentLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.support.HomeworkCardTaskFilter;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * @author guoqiang.li
 * @since 2017/4/20
 */
@Named
public class PostFinishHomeworkUpdateHomeworkTask extends SpringContainerSupport implements PostFinishHomework {
    @Inject private HomeworkCardTaskFilter homeworkCardTaskFilter;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private NewAccomplishmentLoaderImpl newAccomplishmentLoader;
    @Inject private HomeworkTaskRecordDao homeworkTaskRecordDao;

    @Override
    public void afterHomeworkFinished(FinishHomeworkContext context) {
        NewHomework newHomework = context.getHomework();
        Long teacherId = newHomework.getTeacherId();
        Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
        if (mainTeacherId == null) {
            mainTeacherId = teacherId;
        }
        Teacher mainTeacher = teacherLoaderClient.loadTeacher(mainTeacherId);
        if (mainTeacher == null) {
            return;
        }
        // 获取任务列表
        List<HomeworkTask> taskList = homeworkCardTaskFilter.loadValidHomeworkTaskList();
        for (HomeworkTask homeworkTask : taskList) {
            HomeworkTaskType taskType = homeworkTask.getHomeworkTaskType();
            switch (taskType) {
                case DAILY_HOMEWORK:
                    break;
                case WEEKEND_HOMEWORK:
                    updateWeekendHomeworkTask(homeworkTask, mainTeacher, taskType, newHomework);
                    break;
                case VACATION_HOMEWORK:
                    break;
                case ACTIVITY_HOMEWORK:
                    updateActivityHomeworkTask(homeworkTask, mainTeacher, taskType, newHomework);
                    break;
            }
        }
    }

    private void updateWeekendHomeworkTask(HomeworkTask homeworkTask, Teacher mainTeacher, HomeworkTaskType taskType, NewHomework newHomework) {
        // 判断是否在当前任务周期周五布置的作业
        String taskPeriod = HomeworkTaskUtils.calculateWeekendTaskPeriod();
        Date createDate = newHomework.getCreateAt();
        String createDateStr = DateUtils.dateToString(createDate, FORMAT_SQL_DATE);
        if (!StringUtils.equalsIgnoreCase(taskPeriod, createDateStr)) {
            return;
        }
        // 判断当前时间(也就是作业完成时间)是否在周五，周六，周日这三天
        int currentDay = HomeworkTaskUtils.calculateDayOfWeek();
        if (currentDay < 4) {
            return;
        }
        // 判断作业完成人数是否大于等于10人
        int finishedCount = 0;
        NewAccomplishment newAccomplishment = newAccomplishmentLoader.loadNewAccomplishment(newHomework.toLocation());
        if (newAccomplishment != null && MapUtils.isNotEmpty(newAccomplishment.getDetails())) {
            finishedCount = newAccomplishment.getDetails().size();
        }
        if (finishedCount < 10) {
            return;
        }
        Integer taskId = homeworkTask.getTaskId();
        String recordId = HomeworkTaskRecord.generateId(mainTeacher.getId(), taskId, taskType, taskPeriod);
        HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
        if (taskRecord != null) {
            String groupId = newHomework.getClazzGroupId().toString();
            Map<String, Boolean> details = taskRecord.getDetails();
            if (details != null && details.containsKey(groupId)) {
                details.put(groupId, true);
                if (taskRecord.getTaskStatus() == HomeworkTaskStatus.UNFINISHED) {
                    taskRecord.setTaskStatus(HomeworkTaskStatus.FINISHED);
                }
                homeworkTaskRecordDao.upsert(taskRecord);
            }
        }
    }

    private void updateActivityHomeworkTask(HomeworkTask homeworkTask, Teacher mainTeacher, HomeworkTaskType taskType, NewHomework newHomework) {
        // 获取任务id
        int taskId = SafeConverter.toInt(homeworkTask.getTaskId());
        Long mainTeacherId = mainTeacher.getId();
        String taskPeriod = HomeworkTaskUtils.calculateActivityTaskPeriod(homeworkTask.getStartTime(), homeworkTask.getEndTime());
        String recordId = HomeworkTaskRecord.generateId(mainTeacherId, taskId, taskType, taskPeriod);
        switch (taskId) {
            // 2017端午小长假作业任务
            case 3:
                Date homeworkEndTime = newHomework.getEndTime();
                Date now = new Date();
                // 判断作业截止时间是否在5月28至5月30
                // 判断作业完成时间（当前时间）是否在5月30前
                if (homeworkEndTime.after(NewHomeworkConstants.DRAGON_BOAT_FESTIVAL_DATE_EARLIEST)
                        && homeworkEndTime.before(NewHomeworkConstants.DRAGON_BOAT_FESTIVAL_DATE_LATEST)
                        && now.before(NewHomeworkConstants.DRAGON_BOAT_FESTIVAL_DATE_LATEST)) {
                    // 判断作业完成人数是否大于等于10人
                    int finishedCount = 0;
                    NewAccomplishment newAccomplishment = newAccomplishmentLoader.loadNewAccomplishment(newHomework.toLocation());
                    if (newAccomplishment != null && MapUtils.isNotEmpty(newAccomplishment.getDetails())) {
                        finishedCount = newAccomplishment.getDetails().size();
                    }
                    if (finishedCount < 10) {
                        return;
                    }
                    HomeworkTaskRecord taskRecord = homeworkTaskRecordDao.load(recordId);
                    if (taskRecord != null) {
                        String groupId = newHomework.getClazzGroupId().toString();
                        Map<String, Boolean> details = taskRecord.getDetails();
                        if (details != null && details.containsKey(groupId)) {
                            details.put(groupId, true);
                            if (taskRecord.getTaskStatus() == HomeworkTaskStatus.UNFINISHED) {
                                taskRecord.setTaskStatus(HomeworkTaskStatus.FINISHED);
                            }
                            homeworkTaskRecordDao.upsert(taskRecord);
                        }
                    }
                }
                break;
            default:
                break;
        }
    }
}
