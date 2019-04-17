package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.lang.calendar.WeekRange;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.ActivateInfoMapper;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.api.entity.TeacherActivateTeacherHistory;
import com.voxlearning.utopia.service.business.impl.dao.TeacherActivateTeacherHistoryDao;
import com.voxlearning.utopia.service.business.impl.service.BusinessTeacherServiceImpl;
import com.voxlearning.utopia.service.invitation.api.TeacherActivateService;
import com.voxlearning.utopia.service.invitation.entity.TeacherActivate;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 小学老师唤醒任务的初始化操作
 * <p>
 * Created by zhouwei on 2018/9/3
 **/
@Named
@Slf4j
public class TeacherTaskPrimaryAwakeInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private BusinessTeacherServiceImpl businessTeacherService;

    @Inject
    private TeacherActivateTeacherHistoryDao teacherActivateTeacherHistoryDao;

    @ImportService(interfaceClass = TeacherActivateService.class)
    private TeacherActivateService teacherActivateService;

    @Override
    public void afterPropertiesSet() throws Exception {
        teacherTaskTpl = this.loadTaskTpl(getTeacherTaskTpl().getTplId());
    }

    private Date[] getDate() {
        Date now = new Date();
        String year = DateUtils.dateToString(now, "yyyy");
        String dateStringOne = year + "-01-01 00:00:01";
        String dateStringTwo = year + "-08-31 23:59:59";
        String dateStringThree = year + "-12-31 23:59:59";
        Date dateOne = DateUtils.stringToDate(dateStringOne, DateUtils.FORMAT_SQL_DATETIME);
        Date dateTwo = DateUtils.stringToDate(dateStringTwo, DateUtils.FORMAT_SQL_DATETIME);
        Date dateThree = DateUtils.stringToDate(dateStringThree, DateUtils.FORMAT_SQL_DATETIME);
        if (now.getTime() < dateTwo.getTime()) {
            return new Date[]{dateOne, dateTwo};
        } else {
            return new Date[]{dateTwo, dateThree};
        }
    }

    @Override
    public void initTaskAndProgress(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, TeacherTask oldTeacherTask, TeacherTaskProgress oldTeacherTaskProgress) {
        //任务开始前，需要清空老师之前唤醒的老师
        List<ActivateInfoMapper> activateInfoMappers = businessTeacherService.getActivatingTeacher(teacherTaskProgress.getTeacherId());
        activateInfoMappers.stream().forEach(t ->
                businessTeacherService.deleteTeacherActivateTeacherHistory(teacherTaskProgress.getTeacherId(), t.getHistoryId())
        );
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = this.createTeacherTaskEntryCommon(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.setRewardPrefix("每唤醒1人奖励");
        teacherTaskEntry.addReward(SafeConverter.toString(30), TeacherTaskTpl.RewardUnit.integral.name(), "人");
        TeacherTaskEntry.Progress entryProgress = new TeacherTaskEntry.Progress();
        entryProgress.setCurr(0);

        Date[] date = this.getDate();

        int count = 0;
        Map<Long, List<TeacherActivateTeacherHistory>> byInviterIds = teacherActivateTeacherHistoryDao.findByInviterIds(Collections.singletonList(teacherDetail.getId()));
        if (byInviterIds != null && !byInviterIds.isEmpty()) {
            List<TeacherActivateTeacherHistory> byInviterId = byInviterIds.values().stream().flatMap(List::stream).collect(Collectors.toList());
            for (TeacherActivateTeacherHistory teacherActivateTeacherHistory : byInviterId) {
                if (teacherActivateTeacherHistory.getCreateTime() == null || teacherActivateTeacherHistory.getSuccess() == null
                        || !teacherActivateTeacherHistory.getSuccess() || teacherActivateTeacherHistory.getOver() == null
                        || !teacherActivateTeacherHistory.getOver()) {
                    continue;
                }
                Long createTime = teacherActivateTeacherHistory.getCreateTime().getTime();
                if (createTime < date[0].getTime() || createTime > date[1].getTime()) {
                    continue;
                }
                count++;
            }
        }

        List<TeacherActivate> newVersionSuccess = teacherActivateService.loadTeacherActivateStatus(teacherDetail.getId(), TeacherActivate.Status.SUCCESS.getCode());
        List<TeacherActivate> newVersionIng = teacherActivateService.loadTeacherActivateStatus(teacherDetail.getId(), TeacherActivate.Status.ING.getCode());

        List<TeacherActivate> teacherActivates = newVersionSuccess
                .stream().filter(i -> {
                    long createTime = i.getCreateDatetime().getTime();
                    return createTime >= date[0].getTime() && createTime <= date[1].getTime();
                }).collect(Collectors.toList());
        count += teacherActivates.size();

        entryProgress.setCurr(count);

        entryProgress.setQ("人");
        teacherTaskEntry.setProgress(entryProgress);
        teacherTaskEntry.setActionName("已唤醒");
        teacherTaskEntry.setTipText("限时奖励翻倍");

        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);

        int countGoing = 0;     // 正在唤醒的数量
        int countSuccess = 0;   // 唤醒成功的数量

        WeekRange week = WeekRange.current();

        if (byInviterIds != null && !byInviterIds.isEmpty()) {
            List<TeacherActivateTeacherHistory> byInviterId = byInviterIds.values().stream().flatMap(List::stream).collect(Collectors.toList());
            for (TeacherActivateTeacherHistory teacherActivateTeacherHistory : byInviterId) {
                if (teacherActivateTeacherHistory.getCreateTime() == null) {
                    continue;
                }
                long createTime = teacherActivateTeacherHistory.getCreateTime().getTime();
                if (createTime < week.getStartTime() || createTime > week.getEndTime()) {//只统计本周的数据
                    continue;
                }
                if (teacherActivateTeacherHistory.getOver() == null || teacherActivateTeacherHistory.getOver()) {//唤醒成功的人数
                    countSuccess++;
                } else if (teacherActivateTeacherHistory.getSuccess() == null || teacherActivateTeacherHistory.getSuccess()) {//正在唤醒中的人数
                    countGoing++;
                }
            }
        }

        List<TeacherActivate> success = newVersionSuccess.stream().filter(i -> week.contains(i.getSuccessTime())).collect(Collectors.toList());
        List<TeacherActivate> ing = newVersionIng.stream().filter(i -> week.contains(i.getRefTime())).collect(Collectors.toList());

        countSuccess += success.size();
        countGoing += ing.size();

        crmProgressList.add(new TeacherTaskEntry.CrmProgress("本周成功唤醒", null, countSuccess, "人"));
        crmProgressList.add(new TeacherTaskEntry.CrmProgress("本周唤醒中", null, countGoing, "人"));

        return teacherTaskEntry;
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.PRIMARY_AWAKE;
    }
}
