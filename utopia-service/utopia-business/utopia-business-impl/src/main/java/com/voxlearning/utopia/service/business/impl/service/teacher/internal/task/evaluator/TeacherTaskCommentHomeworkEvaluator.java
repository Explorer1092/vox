package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 评论或者奖励事件
 */
@Named
@Slf4j
public class TeacherTaskCommentHomeworkEvaluator extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        String hwIdNew = MapUtils.getString(newVarMap,"hwId");
        if(StringUtils.isEmpty(hwIdNew)) {
            return null;
        }
        //获取上线文中的[hwList]变量信息
        List<Map<String, Object>> hwList = (List<Map<String, Object>>) orgVarMap.computeIfAbsent("hwList", k -> new ArrayList<>());
        Map<String, Map<String,Object>> hwIdMap = hwList.stream().collect(Collectors.toMap(m -> MapUtils.getString(m, "hwId"), m -> m, (m1, m2) -> {if (MapUtils.getLong(m1,"commentAndAwardTime") > MapUtils.getLong(m2, "commentAndAwardTime")) {return m1;} else {return m2;}}));
        if (hwIdMap.containsKey(hwIdNew)) {
            Map<String,Object> hwInfo = hwIdMap.get(hwIdNew);
            hwInfo.put("commentAndAwardTime", newVarMap.get("commentAndAwardTime"));//用新事件
            hwInfo.put("commentAndAwardTimeDate", DateUtils.dateToString(new Date(MapUtils.getLongValue(newVarMap,"commentAndAwardTime")),DateUtils.FORMAT_SQL_DATETIME));
        } else {
            newVarMap.put("commentAndAwardTimeDate", DateUtils.dateToString(new Date(MapUtils.getLongValue(newVarMap,"commentAndAwardTime")),DateUtils.FORMAT_SQL_DATETIME));
            hwList.add(newVarMap);
        }
        orgVarMap.put("commentAndAwardNum", hwList.size()); //评论奖励的次数

        // 获得点评、奖励的学生ID
        List<Long> studentList = (List<Long>) orgVarMap.computeIfAbsent("studentIds", k -> new ArrayList<>());
        List<Long> studentIds = (List<Long>) newVarMap.get("studentIds");
        studentList.addAll(new ArrayList<>(new HashSet<>(studentIds)));
        return orgVarMap;
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        return new HashMap<>();
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.COMMENT_AND_AWARD_HOMEWORK;
    }

}
