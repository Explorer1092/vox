package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

import static com.voxlearning.alps.lang.convert.SafeConverter.toInt;

/**
 * 老师任务的建班事件
 *
 * @author zhouwei
 *
 */
@Named
@Slf4j
public class TeacherTaskCreateClazzEvaluator extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        int updateGroup = MapUtils.getIntValue(newVarMap, "updateGroup");
        if (orgVarMap.containsKey("updateGroup")) {
            updateGroup = updateGroup + MapUtils.getIntValue(orgVarMap, "updateGroup");
        }
        orgVarMap.put("updateGroup", updateGroup);
        return orgVarMap;
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        return new HashMap<>();
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.CREATE_CLAZZ;
    }

}
