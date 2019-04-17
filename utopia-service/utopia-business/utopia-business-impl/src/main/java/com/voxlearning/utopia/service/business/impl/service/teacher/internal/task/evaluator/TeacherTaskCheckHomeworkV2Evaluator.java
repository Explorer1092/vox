package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.entity.task.week.ClazzDetail;
import com.voxlearning.utopia.service.business.impl.utils.TeacherWeekTaskRewardCalc;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

@Named
@Slf4j
public class TeacherTaskCheckHomeworkV2Evaluator extends AbstractTeacherTaskEvaluator {

    public Map<String, Object> mergeVar(Map<String, Object> orgVarMap, Map<String, Object> newVarMap) {
        Long clazzId = MapUtils.getLong(newVarMap, "clazzId");
        String hwId = MapUtils.getString(newVarMap, "hwId");
        Integer finishNum = MapUtils.getInteger(newVarMap, "finishNum");

        Integer cityCode = MapUtils.getInteger(orgVarMap, "city_code");

        // 这里还需要城市、学科
        Object clazzList = orgVarMap.get("clazzList");
        String clazzListJson = JsonUtils.toJson(clazzList);
        List<ClazzDetail> clazzDetails = JsonUtils.fromJsonToList(clazzListJson, ClazzDetail.class);
        Map<Long, ClazzDetail> clazzMap = clazzDetails.stream().collect(Collectors.toMap(ClazzDetail::getClazzId, Function.identity()));

        int integralRewardNum = 0;
        int expRewardNum = 0;

        if (clazzMap.containsKey(clazzId)) {
            // 之所以用循环, 不用 clazzMap, 是为了更改后利用引用生效
            for (ClazzDetail clazzDetail : clazzDetails) {
                if (Objects.equals(clazzId, clazzDetail.getClazzId())) {
                    List<ClazzDetail.HomeworkDetail> homeworkList = clazzDetail.getHomeworkList();

                    // 超过3不处理
                    if (homeworkList.size() < 3) {
                        boolean content = homeworkList.stream().anyMatch(i -> Objects.equals(i.getHomeworkId(), hwId));
                        if (!content) {
                            ClazzDetail.HomeworkDetail homeworkDetail = new ClazzDetail.HomeworkDetail();
                            homeworkDetail.setHomeworkId(hwId);
                            homeworkDetail.setFinishNum(finishNum);

                            integralRewardNum = TeacherWeekTaskRewardCalc.getIntegralNum(cityCode, finishNum, homeworkList.size());
                            expRewardNum = TeacherWeekTaskRewardCalc.getExpNum(cityCode, homeworkList.size());

                            homeworkDetail.setIntegralNum(integralRewardNum);
                            homeworkDetail.setExpNum(expRewardNum);

                            clazzDetail.getHomeworkList().add(homeworkDetail);// 添加进去
                        }
                    }
                    break;
                }
            }
        } else {
            ClazzDetail.HomeworkDetail homeworkDetail = new ClazzDetail.HomeworkDetail();
            homeworkDetail.setHomeworkId(hwId);
            homeworkDetail.setFinishNum(finishNum);

            integralRewardNum = TeacherWeekTaskRewardCalc.getIntegralNum(cityCode, finishNum, 0);
            expRewardNum = TeacherWeekTaskRewardCalc.getExpNum(cityCode, 0);

            homeworkDetail.setIntegralNum(integralRewardNum);
            homeworkDetail.setExpNum(expRewardNum);

            ClazzDetail clazzDetail = new ClazzDetail();
            clazzDetail.setClazzId(clazzId);
            clazzDetail.setHomeworkList(Collections.singletonList(homeworkDetail));
            clazzDetails.add(clazzDetail);
        }

        int hwListSize = clazzDetails.stream().mapToInt(i -> i.getHomeworkList().size()).sum();

        orgVarMap.put("clazzList", clazzDetails);
        orgVarMap.put("nextIntegralReward", integralRewardNum);
        orgVarMap.put("nextExpReward", expRewardNum);
        orgVarMap.put("hwListSize", hwListSize);
        return orgVarMap;
    }

    @Override
    public Map<String, Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("clazzList", new ArrayList<>());
        vars.put("nextReward", 0);
        vars.put("nextIntegralReward", 0);
        vars.put("nextExpReward", 0);
        vars.put("hwListSize", 0);
        return vars;
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.CHECK_HOMEWORK_V2;
    }

}
