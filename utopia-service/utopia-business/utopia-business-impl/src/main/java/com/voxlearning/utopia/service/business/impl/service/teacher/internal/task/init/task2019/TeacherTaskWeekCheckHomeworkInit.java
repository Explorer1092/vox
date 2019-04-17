package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.task2019;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.EventUserType;
import com.voxlearning.utopia.entity.task.TeacherTask;
import com.voxlearning.utopia.entity.task.TeacherTaskProgress;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.mapper.TeacherTaskEntry;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskDao;
import com.voxlearning.utopia.service.business.impl.dao.TeacherTaskProgressDao;
import com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.init.AbstractTeacherTaskInit;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.apache.commons.jexl2.JexlContext;
import org.springframework.beans.factory.InitializingBean;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static java.util.stream.Collectors.toList;

@Named
public class TeacherTaskWeekCheckHomeworkInit extends AbstractTeacherTaskInit implements InitializingBean {

    @Inject
    private TeacherLoaderClient teacherLoaderClient;
    @Inject
    private TeacherTaskDao teacherTaskDao;
    @Inject
    private TeacherTaskProgressDao teacherTaskProgressDao;
    @Inject
    private DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;

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

    @Override
    public void progressAddVars(TeacherDetail teacherDetail, TeacherTaskProgress teacherTaskProgress, TeacherTaskProgress.SubTaskProgress subTaskProgress) {
        subTaskProgress.getVars().put("main_teacher_subject", teacherDetail.getSubject().name());
        subTaskProgress.getVars().put("city_code", teacherDetail.getCityCode());
    }

    @Override
    public void initAddJexlContext(TeacherDetail teacherDetail, JexlContext jexlContext) {
        //老师名下非毕业班的数量
        jexlContext.set("clazzNum", this.getClazzNum(teacherDetail.getId()));
        jexlContext.set("main_teacher_subject", teacherDetail.getSubject().name());
        jexlContext.set("city_code", teacherDetail.getCityCode());
    }

    @Override
    public void updateTaskInfo(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress, Map<String, Object> varMap) {
        //只处理与班级相关的事件
        if (null == varMap.get("event_type") || !Objects.equals(SafeConverter.toString(varMap.get("event_type")), EventUserType.TEACHER_CLAZZ_UPDATED.getEventType())) {
            return;
        }
        List<TeacherTaskProgress.SubTaskProgress> subTaskProgressList = teacherTaskProgress.getSubTaskProgresses();
        int target = subTaskProgressList.get(0).getProgress().getTarget();
        int groupNum = this.getClazzNum(teacherDetail.getId());
        if (groupNum <= target) { //组的数量小于目标值，则不处理
            return;
        }
        teacherTaskDao.upsert(teacherTask);
        teacherTaskProgressDao.upsert(teacherTaskProgress);
    }

    @Override
    public TeacherTaskEntry getTeacherTaskEntry(TeacherDetail teacherDetail, TeacherTask teacherTask, TeacherTaskProgress teacherTaskProgress) {
        TeacherTaskEntry teacherTaskEntry = super.getTeacherTaskEntry(teacherDetail, teacherTask, teacherTaskProgress);
        teacherTaskEntry.setRewardPrefix("完成人数越多,随机奖励园丁豆越多");
        teacherTaskEntry.setProgressPrefix("");
        teacherTaskEntry.getRewardList().clear();
        teacherTaskEntry.setProgress(null);

        teacherTaskEntry.setButtonName("去完成");

        if (Objects.equals(teacherTask.getStatus(), TeacherTask.Status.INIT.name())) {
            return teacherTaskEntry;
        }
        List<TeacherTaskEntry.CrmProgress> crmProgressList = new ArrayList<>();
        teacherTaskEntry.setCrmProgressList(crmProgressList);

        TeacherTaskEntry.CrmProgress crmProgress = new TeacherTaskEntry.CrmProgress();
        TeacherTaskProgress.SubTaskProgress firstSubTask = teacherTaskProgress.getSubTaskProgresses().get(0);
        crmProgress.setTarget(firstSubTask.getProgress().getTarget());
        crmProgress.setCurr(firstSubTask.getProgress().getCurr());
        crmProgressList.add(crmProgress);

        if (StringUtils.isNotEmpty(teacherTaskProgress.getReceiveDate())) {
            teacherTaskEntry.setReceiveDate(teacherTaskProgress.getReceiveDate());
        }

        return teacherTaskEntry;
    }

    private int getClazzNum(Long teacherId) {
        Set<Long> allTeacherIds = teacherLoaderClient.loadRelTeacherIds(teacherId);
        List<Clazz> clazzList = deprecatedClazzLoaderClient.getRemoteReference()
                .loadTeacherClazzs(allTeacherIds).values().stream()
                .flatMap(Collection::stream)
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .sorted(Comparator.comparing(Clazz::getClazzLevel).thenComparing(Clazz::formalizeClazzName))
                .distinct()
                .collect(toList());
        return clazzList.size();
    }

    @Override
    public TeacherTaskTpl.Tpl getTeacherTaskTpl() {
        return TeacherTaskTpl.Tpl.WEEK_CHECK_HOMEWORK_2019;
    }
}
