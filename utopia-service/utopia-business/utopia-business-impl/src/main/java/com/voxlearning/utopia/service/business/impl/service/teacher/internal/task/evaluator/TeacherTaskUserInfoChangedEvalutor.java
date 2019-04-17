package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * 用户信息更新事件
 *
 * Created by zhouwei on 2018/9/6
 **/
@Named
@Slf4j
public class TeacherTaskUserInfoChangedEvalutor extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        orgVarMap.putAll(newVarMap);
        return orgVarMap;
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        return new HashMap<>();
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.USER_INFO_CHANGED;
    }
}
