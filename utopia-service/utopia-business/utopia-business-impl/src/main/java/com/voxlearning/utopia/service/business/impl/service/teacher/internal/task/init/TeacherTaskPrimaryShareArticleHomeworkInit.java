package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateUtils;
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
 * 小学老师分享文章的事件
 *
 * progress.extAttr结构如下：
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
public class TeacherTaskPrimaryShareArticleHomeworkInit extends AbstractTeacherTaskInit implements InitializingBean {


    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        return "";
    }

    @Override
    public String rewardProgressComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.ProgressReward reward) {
        return "";
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
    public boolean onGoingTaskInit(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        Map<String, Object> extAttr = teacherTaskProgress.getExtAttr();
        String nextInitDate = this.getNextInitDate();
        if (extAttr == null) {
            extAttr = new HashMap<>();
            teacherTaskProgress.setExtAttr(extAttr);
        }
        if (!Objects.equals(extAttr.get("nextInitDate"), nextInitDate)) {
            extAttr.put("nextInitDate", nextInitDate);
            extAttr.put("times", 0);
            return true;
        }
        return false;
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.addReward(SafeConverter.toString(1), TeacherTaskTpl.RewardUnit.integral.name(), "次");
        TeacherTaskEntry.Progress entryProgress = new TeacherTaskEntry.Progress();

        List<String> shareArticle = (List<String>) teacherTaskProgress.getSubTaskProgresses().get(0).getVars().computeIfAbsent("shareArticle", k -> new ArrayList<>());

        /*Map<String, Object> extAttr = teacherTaskProgress.getExtAttr();
        Integer times = 0;
        if (extAttr != null) {
            times = MapUtils.getInteger(extAttr, "times", 0);
        }*/
        entryProgress.setCurr(Math.min(1, shareArticle.size()));
        entryProgress.setQ("次");
        teacherTaskEntry.setProgress(entryProgress);
        teacherTaskEntry.setActionName("已分享");

        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);
        crmProgressList.add(new TeacherTaskEntry.CrmProgress("当天分享成功", null, shareArticle.size(), "次"));
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
        return TeacherTaskTpl.Tpl.PRIMARY_SHARE_ARTICLE;
    }
}
