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
 * 老师分享作业报告事件
 *
 * @author zhouwei
 */
@Named
@Slf4j
public class TeacherTaskReportHomeworkEvaluator extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        String hwIdNew = MapUtils.getString(newVarMap,"hwId");
        if(StringUtils.isEmpty(hwIdNew)) {
            return null;
        }
        //获取上线文中的[hwList]变量信息
        List<Map<String, Object>> hwList = (List<Map<String, Object>>) orgVarMap.computeIfAbsent("hwList", k -> new ArrayList<>());
        newVarMap.put("reportHomeworkTimeDate", DateUtils.dateToString(new Date(MapUtils.getLongValue(newVarMap,"reportHomeworkTime")),DateUtils.FORMAT_SQL_DATETIME));
        hwList.add(newVarMap);
        orgVarMap.put("shareReportNum", hwList.size());//评论奖励的次数
        return orgVarMap;
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        return new HashMap<>();
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.REPORT_HOMEWORK;
    }

}
