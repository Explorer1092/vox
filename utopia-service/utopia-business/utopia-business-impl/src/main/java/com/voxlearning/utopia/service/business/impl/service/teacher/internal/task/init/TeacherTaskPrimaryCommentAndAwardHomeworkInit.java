package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Named;
import java.util.*;

/**
 *
 * 小学老师奖励与评论作业的任务处理
 *
 *  progress.extAttr结构如下：
 *
 * {
 *     "times" : 0,                             //总完成次数
 *     "nextInitDate" : "2018-09-01 12:00:00"   //下一次初始化时间
 * }
 *
 * Created by zhouwei on 2018/9/6
 **/
@Named
@Slf4j
public class TeacherTaskPrimaryCommentAndAwardHomeworkInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    private String getNextInitDate() {
        Date now = new Date();
        String year = DateUtils.dateToString(now, "yyyy");
        String dateStringOne = year + "-08-31 23:59:59";
        String dateStringTwo = year + "-12-31 23:59:59";
        Date dateOne = DateUtils.stringToDate(dateStringOne, DateUtils.FORMAT_SQL_DATETIME);
        Date dateTwo = DateUtils.stringToDate(dateStringTwo, DateUtils.FORMAT_SQL_DATETIME);
        if (now.getTime() < dateOne.getTime()) {
            return dateStringOne;
        } else {
            return dateStringTwo;
        }
    }

    @Override
    public void initTaskAndProgress(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, TeacherTask oldTeacherTask, TeacherTaskProgress oldTeacherTaskProgress) {
        if (oldTeacherTaskProgress != null) {
            teacherTaskProgress.setExtAttr(oldTeacherTaskProgress.getExtAttr());
        } else {
            Map<String, Object> extAttr = new HashMap<>();
            extAttr.put("nextInitDate", this.getNextInitDate());
            extAttr.put("times", 0);
            teacherTaskProgress.setExtAttr(extAttr);
        }
    }

    @Override
    public boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress){
        Map<String, Object> extAttr = teacherTaskProgress.getExtAttr();
        if (extAttr == null) {
            extAttr = new HashMap<>();
            teacherTaskProgress.setExtAttr(extAttr);
        }
        return isOfflineOldWeekTask(teacherDetail, teacherTask, teacherTaskProgress);
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.addReward(SafeConverter.toString(1), TeacherTaskTpl.RewardUnit.integral.name(), "次");
        teacherTaskEntry.addReward(SafeConverter.toString(5), TeacherTaskTpl.RewardUnit.exp.name(), "次");
        TeacherTaskEntry.Progress entryProgress = new TeacherTaskEntry.Progress();

        Map<String, Object> extAttr = teacherTaskProgress.getExtAttr();
        Integer times = 0;
        if (extAttr != null) {
            times = MapUtils.getInteger(extAttr, "times", 0);
        }
        entryProgress.setCurr(times);
        entryProgress.setQ("次");
        teacherTaskEntry.setProgress(entryProgress);
        teacherTaskEntry.setActionName("已点评");

        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);
        List<Map<String, Object>> hwList = (List<Map<String, Object>>) teacherTaskProgress.getSubTaskProgresses().get(0).getVars().get("hwList");
        int timesWeek = 0;
        if (CollectionUtils.isNotEmpty(hwList)) {
            timesWeek = hwList.size();
        }
        crmProgressList.add(new TeacherTaskEntry.CrmProgress("本周点评奖励学生", null, timesWeek, "次"));
        return teacherTaskEntry;
    }

    @Override
    public void oneProcessComplete(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress){
        Map<String, Object> extAttr = teacherTaskProgress.getExtAttr();
        if (null == extAttr) {
            extAttr = new HashMap<>();
            teacherTaskProgress.setExtAttr(extAttr);
        }
        Integer times = MapUtils.getInteger(extAttr, "times", 0);
        times = times + 1;
        extAttr.put("times", times);
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.PRIMARY_COMMENT_AND_AWARD_HOMEWORK;
    }
}
