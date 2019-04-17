package com.voxlearning.utopia.service.campaign.impl.service;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.remote.hydra.client.generic.client.HydraGenericInvokerClient;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningCount;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningWeekInfo;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

@Named
@SuppressWarnings("ALL")
public class TeacherWinterPlanDPBridge {

    private static final String INTERFACE = "com.voxlearning.galaxy.service.studyplanning.api.DPStudyPlanningLoader";
    private static final String VERSION = "20190119";

    public List<StudentPlanningCount> loadStudentPlanningCountInfo(Collection<Long> teacherIds) {
        List<List<StudentPlanningCount>> list = new ArrayList<>();
        for (Long teacherId : teacherIds) {
            String json = HydraGenericInvokerClient.build()
                    .group(runtimeGroup())
                    .version(VERSION)
                    .interfaceName(INTERFACE)
                    .methodName("loadStudentPlanningCountInfo")
                    .invoke(teacherId)
                    .originalResult();
            List<StudentPlanningCount> planningCounts = JSON.parseArray(json, StudentPlanningCount.class);
            if (CollectionUtils.isNotEmpty(planningCounts)) {
                list.add(planningCounts);
            }
        }
        List<StudentPlanningCount> collect = list.stream().flatMap(Collection::stream).collect(Collectors.toList());

        return collect;
    }

    public List<StudentPlanningWeekInfo> loadStudentPlanningWeekInfo(Long studentId) {
        String json = HydraGenericInvokerClient.build()
                .group(runtimeGroup())
                .version(VERSION)
                .interfaceName(INTERFACE)
                .methodName("loadStudentPlanningWeekInfo")
                .invoke(studentId)
                .originalResult();

        List<StudentPlanningWeekInfo> studentPlanningWeekInfos = JSON.parseArray(json, StudentPlanningWeekInfo.class);
        return studentPlanningWeekInfos;
    }

    private String runtimeGroup() {
        String currentStage = RuntimeMode.getCurrentStage();
        if (Objects.equals(currentStage, Mode.PRODUCTION.getStageMode())) {
            return "alps-hydra-production";
        }
        if (Objects.equals(currentStage, Mode.STAGING.getStageMode())) {
            return "alps-hydra-staging";
        }
        return "alps-hydra-test";
        //return "alps-hydra-junbao.zhang";
    }
}
