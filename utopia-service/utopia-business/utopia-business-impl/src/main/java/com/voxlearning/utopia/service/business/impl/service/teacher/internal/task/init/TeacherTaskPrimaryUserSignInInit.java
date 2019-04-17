package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
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
 * 老师签到任务的初始化
 *
 * Created by zhouwei on 2018/9/3
 **/
@Named
@Slf4j
public class TeacherTaskPrimaryUserSignInInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    @Override
    public boolean isNeedRestart(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress) {
        List<TeacherTaskProgress.SubTaskProgress> subTaskProgresses = teacherTaskProgress.getSubTaskProgresses();
        if (CollectionUtils.isEmpty(subTaskProgresses)) {
            return true;
        }
        List<TeacherTaskProgress.SubTaskProgress> subTaskProgressList = teacherTaskProgress.getSubTaskProgresses();
        List<String> signInDateList = new ArrayList<>();
        subTaskProgressList.forEach(subTaskProgress -> {
            Boolean signIn = MapUtils.getBoolean(subTaskProgress.getVarMap(), "signIn");
            if (signIn != null && signIn) {
                signInDateList.add(MapUtils.getString(subTaskProgress.getVarMap(), "date"));
            }
        });

        Date now = new Date();
        if (signInDateList.size() == 0) {//如果当前还没有签过一次到
            String firstDate = MapUtils.getString(teacherTaskProgress.getSubTaskProgress(1L).getVarMap(), "date");
            String nowDate = DateUtils.dateToString(now, DateUtils.FORMAT_SQL_DATE);
            if (!Objects.equals(firstDate, nowDate)) {//如果第一天需要签到的日期与今天不符，则需要重新初始化
                return true;
            }
            return false;
        }

        /**
         * 判断是否是连续签到，如果是连续的，则不用重新开始，否则需要重新开始
         */
        Date mark = now;
        String markString = DateUtils.dateToString(mark, DateUtils.FORMAT_SQL_DATE);
        if (signInDateList.contains(markString)) {//表示今天已经签到，把mark后移一天，避免下面的for循环异常
            mark = DateUtils.nextDay(mark, 1);
        }

        for (int i = 0; i < signInDateList.size(); i++) {
            mark = DateUtils.nextDay(mark, -1);
            markString = DateUtils.dateToString(mark, DateUtils.FORMAT_SQL_DATE);
            if (!signInDateList.contains(markString)) {//如果中间断签了，则需要初始化，重头开始签
                return true;
            }
        }
        return false;
    }

    @Override
    public String rewardSubTaskComment(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress, TeacherTaskProgress.Reward reward) {
        return "";
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        List<TeacherTaskEntry.Reward> teacherTaskEntryRewardCommon = this.createTeacherTaskEntryRewardCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.setRewardList(teacherTaskEntryRewardCommon);

        Map<String, Object> taskParams = new HashMap<>();
        if (teacherTaskTpl.getTpl() == TeacherTaskTpl.Tpl.PRIMARY_USER_SIGN_IN) {
            taskParams.put("is_sign_in", false);
            String now = DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATE);//当前时间
            List<TeacherTaskProgress.SubTaskProgress> subTaskProgressList = teacherTaskProgress.getSubTaskProgresses();
            for (TeacherTaskProgress.SubTaskProgress sub : subTaskProgressList) {
                if (Objects.equals(sub.getStatus(), TeacherTask.Status.FINISHED.name())){
                    if (Objects.equals(now,MapUtils.getString(sub.getVarMap(), "date"))) {
                        taskParams.put("is_sign_in", true);
                    }
                }
            }
        }
        teacherTaskEntry.setTaskParams(taskParams);
        teacherTaskEntry.setCrmIsDisplay(false);
        return teacherTaskEntry;
    }

    /**
     * 签到的数据量很大，如果用户每天都不签到，则会每天存入一条数据，所以判断是否有签到信息
     * @param teacherDetail
     * @param teacherTask
     * @param teacherTaskProgress
     * @return
     */
    @Override
    public boolean isStoreLog(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        //遍历所有子任务，如果有任意一个签到成功，则可以存入历史数据中
        for (TeacherTaskProgress.SubTaskProgress subTaskProgress : teacherTaskProgress.getSubTaskProgresses()) {
            if (subTaskProgress.isFinish()) {
                return true;
            }
        }
        return false;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.PRIMARY_USER_SIGN_IN;
    }

}