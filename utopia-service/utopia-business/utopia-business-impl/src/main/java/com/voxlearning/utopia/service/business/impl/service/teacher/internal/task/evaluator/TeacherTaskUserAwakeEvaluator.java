package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.*;

/**
 * 老师唤醒事件处理
 *
 * @author zhouwei
 *
 */
@Slf4j
@Named
public class TeacherTaskUserAwakeEvaluator extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        Long inviteeId = MapUtils.getLong(newVarMap, "inviteeId");
        if (!orgVarMap.containsKey("inviteeIdSet")) {
            orgVarMap.put("inviteeIdSet", new ArrayList<>());

        }
        if (!orgVarMap.containsKey("inviteeTimeSet")){
            orgVarMap.put("inviteeTimeSet", new ArrayList<>());
        }
        List<Long> inviteeIdSet = (ArrayList<Long>)orgVarMap.get("inviteeIdSet");
        List<String> inviteeTimeSet = (ArrayList<String>)orgVarMap.get("inviteeTimeSet");
        if (inviteeIdSet.contains(inviteeId)) {//之前已经有了，则不再处理
            return orgVarMap;
        }
        int activeNum = MapUtils.getIntValue(newVarMap, "activeNum");
        if (orgVarMap.containsKey("activeNum")) {
            activeNum = activeNum + MapUtils.getIntValue(orgVarMap, "activeNum");
        }
        inviteeIdSet.add(inviteeId);
        inviteeTimeSet.add(DateUtils.dateToString(new Date(), DateUtils.FORMAT_SQL_DATETIME));
        orgVarMap.put("activeNum", activeNum);
        orgVarMap.put("inviteeTimeSet", inviteeTimeSet);
        orgVarMap.put("inviteeIdSet", inviteeIdSet);//被唤醒成功的老师ID集合
        return orgVarMap;
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        return new HashMap<>();
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.USER_AWAKE;
    }

}
