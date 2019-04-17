package com.voxlearning.utopia.service.campaign.impl.service;

import com.alibaba.fastjson.JSON;
import com.alibaba.fastjson.JSONArray;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.remote.hydra.client.generic.client.HydraGenericInvokerClient;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningCount;
import com.voxlearning.utopia.service.campaign.api.mapper.dp.StudentPlanningWeekInfo;

import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@SuppressWarnings("ALL")
public class NewTermPlanDPBridge {

    private static final String INTERFACE = "com.voxlearning.galaxy.service.studyplanning.api.DPStudyPlanningService";
    private static final String VERSION = "20190319";

    public List<Map<String, Object>> saveNewTermActivityPlans(Long studentId, String plans) {
        String json = HydraGenericInvokerClient.build()
                .group(runtimeGroup())
                .version(VERSION)
                .interfaceName(INTERFACE)
                .methodName("saveNewTermActivityPlans")
                .invoke(studentId, plans)
                .originalResult();

        return toListMap(json);

    }

    public List<Map<String, Object>> saveActivityPlans(Long studentId, String plans) {
        String json = HydraGenericInvokerClient.build()
                .group(runtimeGroup())
                .version(VERSION)
                .interfaceName(INTERFACE)
                .methodName("saveActivityPlans")
                .invoke(studentId, plans)
                .originalResult();

        return toListMap(json);

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

    private List<Map<String, Object>> toListMap(String json) {
        List<Object> list = JSON.parseArray(json);
        List<Map<String, Object>> result = new ArrayList<>();
        for (Object object : list) {
            Map<String, Object> ret = (Map<String, Object>) object;
            result.add(ret);
        }
        return result;
    }
}
