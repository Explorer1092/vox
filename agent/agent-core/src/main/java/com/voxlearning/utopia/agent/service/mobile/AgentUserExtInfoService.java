package com.voxlearning.utopia.agent.service.mobile;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.agent.dao.mongo.AgentUserExtInfoDao;
import com.voxlearning.utopia.agent.persist.entity.AgentUserExtInfo;
import com.voxlearning.utopia.agent.service.AbstractAgentService;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * 用户扩展信息服务
 * Created by yaguang.wang on 2016/9/27.
 */
@Named
public class AgentUserExtInfoService extends AbstractAgentService {
    @Inject private AgentUserExtInfoDao agentUserExtInfoDao;
    @Inject private TeacherSummaryService teacherSummaryService;

    private AgentUserExtInfo loadAgentUserExtInfoByUserId(Long userId) {
        return agentUserExtInfoDao.load(userId);
    }

    public Set<Long> loadHideTeacherIdsByUserId(Long userId) {
        AgentUserExtInfo info = loadAgentUserExtInfoByUserId(userId);
        if (info == null) {
            return Collections.emptySet();
        }
        Set<Long> teacherIds = info.getHideTeachers();
        if (CollectionUtils.isEmpty(teacherIds)) {
            return Collections.emptySet();
        }
        return teacherIds;
    }

    public MapMessage addHideTeacher(Long userId, Long teacherId) {
        CrmTeacherSummary teacherSummary = teacherSummaryService.load(teacherId);
        if (teacherSummary == null) {
            return MapMessage.errorMessage("未找到该老师信息");
        }
        if (teacherSummary.getAuthState() == 1) {
            return MapMessage.errorMessage("该老师已经认证不能隐藏");
        }
        Long lastUserTime = teacherSummary.getLatestAssignHomeworkTime();
        if (lastUserTime != null && DateUtils.nextDay(new Date(), -30).before(new Date(lastUserTime))) {
            return MapMessage.errorMessage("该老师30天内使用过不能隐藏");
        }
        AgentUserExtInfo info = loadAgentUserExtInfoByUserId(userId);
        if (info == null) {
            info = new AgentUserExtInfo();
            info.setId(userId);
            Set<Long> teacherIds = new HashSet<>();
            teacherIds.add(teacherId);
            info.setHideTeachers(teacherIds);
            agentUserExtInfoDao.insert(info);
            return MapMessage.successMessage();
        }
        info.getHideTeachers().add(teacherId);
        info = agentUserExtInfoDao.upsert(info);
        if (info == null) {
            return MapMessage.errorMessage("用户ID:" + userId + "隐藏老师(ID" + teacherId + ")失败");
        }
        return MapMessage.successMessage();
    }

    public MapMessage showTeacher(Long userId, Long teacherId) {

        AgentUserExtInfo info = loadAgentUserExtInfoByUserId(userId);
        if (info == null) {
            return MapMessage.errorMessage("用户ID:" + userId + "还没有隐藏的老师");
        }
        Set<Long> hideTeacherIds = info.getHideTeachers();
        if (CollectionUtils.isEmpty(hideTeacherIds)) {
            return MapMessage.errorMessage("用户ID:" + userId + "还没有隐藏的老师");
        }
        if (!hideTeacherIds.contains(teacherId)) {
            return MapMessage.errorMessage("老师ID:" + teacherId + "不再用户用户ID:" + userId + "的隐藏老师列表中");
        }
        if (hideTeacherIds.remove(teacherId)) {
            info.setHideTeachers(hideTeacherIds);
            agentUserExtInfoDao.upsert(info);
            return MapMessage.successMessage();
        }
        return MapMessage.errorMessage("用户ID:" + userId + "隐藏老师ID:" + teacherId + "失败");
    }

    public List<AgentUserExtInfo> findByPage(int page,int size){
        return agentUserExtInfoDao.findByPage(page,size);
    }
}
