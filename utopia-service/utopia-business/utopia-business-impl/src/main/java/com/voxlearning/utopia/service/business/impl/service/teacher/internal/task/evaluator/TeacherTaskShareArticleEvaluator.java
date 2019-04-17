package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.*;

/**
 * Created by zhouwei on 2018/9/6
 **/
@Slf4j
@Named
public class TeacherTaskShareArticleEvaluator extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        //获取上线文中的[shareList]变量信息
        List<String> shareArticle = (List<String>) orgVarMap.computeIfAbsent("shareArticle", k -> new ArrayList<>());
        long date = MapUtils.getLongValue(newVarMap, "date");
        if (date == 0) {
            return orgVarMap;
        }
        String dateString = DateUtils.dateToString(new Date(MapUtils.getLongValue(newVarMap, "date")), DateUtils.FORMAT_SQL_DATETIME);
        if (!shareArticle.contains(dateString)) {//如果这个日期不存在，这添加进去
            shareArticle.add(dateString);
        }
        orgVarMap.put("shareArticle", shareArticle);
        return orgVarMap;
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        return new HashMap<>();
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.SHARE_ARTICLE;
    }

}
