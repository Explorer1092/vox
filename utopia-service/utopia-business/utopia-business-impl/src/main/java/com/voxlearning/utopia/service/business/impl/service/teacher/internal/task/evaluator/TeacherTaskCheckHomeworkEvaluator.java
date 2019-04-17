package com.voxlearning.utopia.service.business.impl.service.teacher.internal.task.evaluator;

import com.voxlearning.alps.annotation.dao.jdbc.P;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.entity.task.TeacherTaskTpl;
import com.voxlearning.utopia.service.clazz.client.GroupLoaderClient;
import com.voxlearning.utopia.service.user.api.TeacherLoader;
import com.voxlearning.utopia.service.user.api.entities.Group;
import com.voxlearning.utopia.service.user.api.entities.extension.TeacherDetail;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.concurrent.atomic.AtomicInteger;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.groupingBy;

/**
 *
 * 检查作业事件
 *
 * 检查作业的任务都是以HWID为单位的，要求，必须是满足某一次作业大于20人，而不是一个组大于20人，即：
 *
 * 假设：同一个group同一天布置多次作业，则会有多个hwId，我们要求是其中一个hwId大于20人，而不是班下面各个hwId的总和大于20人
 *
 * @author zhouwei
 */
@Named
@Slf4j
public class TeacherTaskCheckHomeworkEvaluator extends AbstractTeacherTaskEvaluator {

    @SuppressWarnings({"unchecked"})
    public Map<String, Object> mergeVar(Map<String,Object> orgVarMap, Map<String, Object> newVarMap) {
        String hwIdNew = MapUtils.getString(newVarMap,"hwId");
        if(StringUtils.isEmpty(hwIdNew)) {
            return null;
        }
        //获取上线文中的[hwList]变量信息
        List<Map<String, Object>> hwList = (List<Map<String, Object>>) orgVarMap.computeIfAbsent("hwList", k -> new ArrayList<>());
        Map<String, Map<String,Object>> hwIdMap = hwList.stream().collect(Collectors.toMap(m -> MapUtils.getString(m, "hwId"), m -> m, (m1, m2) -> {if (MapUtils.getLong(m1,"checkAt") > MapUtils.getLong(m2, "checkAt")) {return m1;} else {return m2;}}));
        int finishNum = MapUtils.getIntValue(newVarMap, "finishNum");//本次完成作业的人数
        //从下面这个逻辑可以看出，每个hwId在HwList里面只有一条，并且以最后一次检查为准
        if (hwIdMap.containsKey(hwIdNew)) {
            Map<String,Object> hwInfo = hwIdMap.get(hwIdNew);
            hwInfo.put("checkAt", newVarMap.get("checkAt"));//用新的检查作业时间
            hwInfo.put("checkAtDate", DateUtils.dateToString(new Date(MapUtils.getLongValue(newVarMap,"checkAt")),DateUtils.FORMAT_SQL_DATETIME));
            hwInfo.put("finishNum", finishNum);//每次都更新最新的完成人数
        } else {
            newVarMap.put("firstCheckAt", newVarMap.get("checkAt"));//新增时，设置第一次检查该作业ID的时间
            newVarMap.put("firstCheckAtDate", DateUtils.dateToString(new Date(MapUtils.getLongValue(newVarMap,"checkAt")),DateUtils.FORMAT_SQL_DATETIME));
            newVarMap.put("checkAtDate", DateUtils.dateToString(new Date(MapUtils.getLongValue(newVarMap,"checkAt")),DateUtils.FORMAT_SQL_DATETIME));
            newVarMap.put("firstFinishNum", finishNum);//新增时，设置第一次检查该作业ID的完成人数
            hwList.add(newVarMap);
        }
        orgVarMap.put("currFinishNum", finishNum);//当前检查作业事件，学生的完成人数
        this.homeWorkMaxFinishNumVarMap(orgVarMap);
        this.hameWorkFirshFinishNumVarMap(orgVarMap);
        return orgVarMap;
    }

    /**
     * 每组每天检查第一次人的完成人数数组
     *
     * {if(main_teacher_subject eq 'ENGLISH'){return 20 * currFinishNum;}else if(main_teacher_subject eq 'MATN' || main_teacher_subject eq 'CHINESES'){return 30 * currFinishNum;}else{return 0;}}
     * {return currFinishNum * 1.5;}
     * @param orgVarMap
     */
    private void hameWorkFirshFinishNumVarMap(Map<String,Object> orgVarMap) {
        List<Map<String, Object>> hwList = (List<Map<String, Object>>) orgVarMap.computeIfAbsent("hwList", k -> new ArrayList<>());
        //每次都需要重置一下，因为hwList的作业检查可能会改变时间等。
        List<Map<String, Object>> groupHwFirstView = new ArrayList<>();
        //根据组ID对作业进行分组
        Map<String, List<Map<String, Object>>> groupIdHwMap = hwList.stream().collect(groupingBy(k -> MapUtils.getString(k, "groupId")));
        groupIdHwMap.forEach((gId, list) -> {
            Map<String, Map<String, Object>> groupByDay = list.stream().collect(Collectors.toMap(
                    d -> {
                        long checkAt = MapUtils.getLongValue(d, "firstCheckAt");
                        Date date = new Date(checkAt);
                        return DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE);
                    },
                    m -> m,
                    (m1, m2) -> {//同一组，同一天多次的检查作业，用检查作业时间最早的那个
                        if (MapUtils.getIntValue(m1, "firstCheckAt") < MapUtils.getIntValue(m2, "firstCheckAt")) {
                            return m1;
                        } else {
                            return m2;
                        }
                    }));

            List<Integer> finishNums = new ArrayList<>();
            List<String> hwIds = new ArrayList<>();
            List<Map<String, Object>> hwInfosList = new ArrayList<>();
            for (Map<String, Object> hwInfos : groupByDay.values()) {
                hwInfosList.add(hwInfos);
            }
            Collections.sort(hwInfosList, (m1, m2) -> {
                long checkAtM1 = MapUtils.getLongValue(m1, "firstCheckAt");
                long checkAtM2 = MapUtils.getLongValue(m2, "firstCheckAt");
                return checkAtM1 == checkAtM2 ? 0 : checkAtM1 > checkAtM2 ? 1 : -1;
            });
            for (Map<String, Object> hwInfos : hwInfosList) {
                int firstFinishNum = MapUtils.getIntValue(hwInfos, "firstFinishNum");//取第一次的完成人数
                finishNums.add(firstFinishNum);
                hwIds.add(MapUtils.getString(hwInfos, "hwId"));
            }
            //每个班不同天的作业检查情况
            Map<String, Object> info =  new HashMap<>();
            info.put("finishNum", finishNums);//每个班第一次作业第一次检查的完成人数，数组维度表示不同的天
            info.put("hwIds", hwIds);//作业的一些信息
            info.put("groupId", gId);//组ID
            groupHwFirstView.add(info);
        });
        orgVarMap.put("groupHwFirstView", groupHwFirstView);
    }

    /**
     * 每组每天的最大完成数
     * 1、自领取任务日当天起，15天内给名下任意班级布置且检查3次作业
     * 2、每次有20人以上完成，即可获得奖励
     * 3、当天给多班检查，仅算一次
     *
     * expression:
     * for(var ghw : groupHwView) {
     *     var num = 0;
     *     for(var fNum : ghw.finishNum) {
     *         if(fNum >= 20){
     *             num = num + 1;
     *         }
     *     }
     *     if(num >= 3){
     *         result = true;
     *         break;
     *     } else {
     *         num = 0;
     *     }
     * }
     * @return
     */
    private void homeWorkMaxFinishNumVarMap(Map<String,Object> orgVarMap) {
        List<Map<String, Object>> hwList = (List<Map<String, Object>>) orgVarMap.computeIfAbsent("hwList", k -> new ArrayList<>());
        //每次都需要重置一下，因为hwList的作业检查可能会改变时间等。
        List<Map<String, Object>> groupHwView = new ArrayList<>();
        AtomicInteger maxFinishNum = new AtomicInteger();
        //根据组ID对作业进行分组
        Map<String, List<Map<String, Object>>> groupIdHwMap = hwList.stream().collect(groupingBy(k -> MapUtils.getString(k, "groupId")));
        groupIdHwMap.forEach((gId, list) -> {
            //1.按日期分组，某一天的只取最大的那次作业人数的作业记录
            //2.这个维度是HWID，因为在hwList，同一个HWID只有一个
            //3.同一天只有一条hw的记录信息，多天会有多条
            //4.如果同一天同一个班检查了多次作业，只取其中一次，取人数多的那次
            /**
             *
             * Demo：某组的数据如下
             * 组1： 2018-10-01 布置一次(hwId:1123)作业，完成20，2018-10-01 布置一次(hwId:11231)作业，完成25，2018-10-02 布置一次(hwId:1456)作业，完成25，2018-10-03 布置一次(hwId:1789)作业，完成30*
             * 结果数据如下：
             * {
             *     "2018-10-01": {"hwId":"11231","checkAt":2018-10-01,"finishNum":25},
             *     "2018-10-02": {"hwId":"1456","checkAt":2018-10-02,"finishNum":25},
             *     "2018-10-03": {"hwId":"1789","checkAt":2018-10-02,"finishNum":30},
             * }
             *
             */
            Map<String, Map<String, Object>> groupByDay = list.stream().collect(Collectors.toMap(
            d -> {
                long checkAt = MapUtils.getLongValue(d, "checkAt");
                Date date = new Date(checkAt);
                return DateUtils.dateToString(date, DateUtils.FORMAT_SQL_DATE);
            },
            m -> m,
            (m1, m2) -> {//同一组，同一天多次的检查作业，用完成数最大的那个
                if (MapUtils.getIntValue(m1, "finishNum") > MapUtils.getIntValue(m2, "finishNum")) {
                    return m1;
                } else {
                    return m2;
                }
            }));

            List<Integer> finishNums = new ArrayList<>();
            List<String> hwIds = new ArrayList<>();
            for (Map<String, Object> hwInfos : groupByDay.values()) {
                int finishNum = MapUtils.getIntValue(hwInfos, "finishNum");
                finishNums.add(finishNum);
                maxFinishNum.getAndUpdate(prev -> prev < finishNum ? finishNum : prev);
                hwIds.add(MapUtils.getString(hwInfos, "hwId"));
            }
            //每个班不同天的作业检查情况
            Map<String, Object> info =  new HashMap<>();
            info.put("finishNum", finishNums);//每个班每天最大作业的完成人数数组
            info.put("hwIds", hwIds);//每个班每天最大数组的基本信息
            info.put("groupId", gId);//组ID
            groupHwView.add(info);
        });
        orgVarMap.put("maxFinishNum", maxFinishNum.get());//注意：这里需要的是某一个hwId的最大人数，而不是所有hwList的总数
        orgVarMap.put("groupHwView", groupHwView);
    }

    @Override
    public Map<String,Object> initVars(TeacherTaskTpl teacherTaskTpl, TeacherTaskTpl.SubTask subTask) {
        Map<String, Object> vars = new HashMap<>();
        vars.put("currFinishNum", 0);
        return vars;
    }

    public TeacherTaskTpl.TplEvaluatorEvent getTplEvaluatorEvent() {
        return TeacherTaskTpl.TplEvaluatorEvent.CHECK_HOMEWORK;
    }

}
