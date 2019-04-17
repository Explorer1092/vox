package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 老师签到事件
 *
 * @author zhouwei
 */
@Named
@Slf4j
public class TeacherTaskUserSignInEvaluator extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        Long signInDateAction = MapUtils.getLong(newVarMap, "date");//这次的签到日期
        Date signInDateActionDate = new Date(signInDateAction);
        String signInDateActionString = DateUtils.dateToString(signInDateActionDate, DateUtils.FORMAT_SQL_DATE);

        String signDateMark = MapUtils.getString(orgVarMap,"date");//当前子任务需要签到的日期
        if (Objects.equals(signDateMark, signInDateActionString)) {//如果该子任务的签到日期与当前相等
            orgVarMap.put("signIn", true);
        }

        return orgVarMap;
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        List<Map<String,Object>> signInList = new ArrayList<>();
        Map<String, Object> map = new HashMap<>();
        Date now = new Date();
        Long id = subTask.getId();
        //将签到每一个子任务的签到日期初始化vars中
        Date date = DateUtils.nextDay(now, id.intValue() - 1);
        String dateString = DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE);

        map.put("date", dateString);
        map.put("dateSimple", dateToSimple(date));
        map.put("signIn", false);
        return map;
    }

    private String dateToSimple(Date date) {
        String dateMonth = DateUtils.dateToString(date, "MM");
        String dateDay = DateUtils.dateToString(date, "dd");
        if (dateMonth.length() == 2 && dateMonth.startsWith("0")) {
            dateMonth = dateMonth.replace("0","");
        }
        if (dateDay.length() == 2 && dateDay.startsWith("0")) {
            dateDay = dateDay.replace("0","");
        }
        return dateMonth + "." + dateDay;
    }

    @Override
    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.USER_SIGN_IN;
    }

}
