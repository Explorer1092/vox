package com.voxlearning.utopia.service.campaign.impl.listener;

import com.alibaba.fastjson.JSON;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.campaign.api.mapper.NewTermStudentPlanMapper;
import com.voxlearning.utopia.service.campaign.api.mapper.WarmHeartPlanMapper;
import com.voxlearning.utopia.service.campaign.impl.service.ParentNewTermPlanServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.service.TeacherNewTermPlanServiceImpl;
import com.voxlearning.utopia.service.campaign.impl.service.WarmHeartPlanServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "galaxy.study.planning.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "galaxy.study.planning.topic")
        }
)
public class TeacherWinterPlanListener extends SpringContainerSupport implements MessageListener {

    @AlpsPubsubPublisher(topic = "galaxy.study.planning.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;
    @Inject
    private ParentNewTermPlanServiceImpl parentNewTermPlanService;
    @Inject
    private TeacherNewTermPlanServiceImpl teacherNewTermPlanService;
    @Inject
    private WarmHeartPlanServiceImpl warmHeartPlanService;

    @Override
    @SuppressWarnings("ALL")
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            logger.warn("galaxy.study.planning.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }
        String messageType = MapUtils.getString(msgMap, "messageType");


        if ("finishWarmHeartActivity".equals(messageType)) {
            handlerWHP(msgMap);
        } else if ("newTermActivity".equals(messageType)) {
            handler(msgMap);
        } else if ("deleteWarmHeartActivity".equals(messageType)) {
            handlerDelWHP(msgMap);
        }
    }

    private void handlerDelWHP(Map<String, Object> msgMap) {
        try {
            Long studentId = MapUtils.getLong(msgMap, "studentId");
            String planId = MapUtils.getString(msgMap, "planId");
            if (RuntimeMode.lt(Mode.STAGING)) {
                log.info("galaxy.study.planning.topic deleteWarmHeartActivity msg:{}", JSON.toJSONString(msgMap));
            }
            warmHeartPlanService.delStudentTargets(studentId, planId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handlerWHP(Map<String, Object> msgMap) {
        try {
            Long studentId = MapUtils.getLong(msgMap, "studentId");
            String planId = MapUtils.getString(msgMap, "planId");

            if (RuntimeMode.lt(Mode.STAGING)) {
                log.info("galaxy.study.planning.topic finishWarmHeartActivity msg:{}", JSON.toJSONString(msgMap));
            }

            WarmHeartPlanMapper studentTargets = warmHeartPlanService.getStudentTargets(studentId);
            if (studentTargets == null || CollectionUtils.isEmpty(studentTargets.getPlans())) {
                return;
            }

            WarmHeartPlanMapper.Plan plan = studentTargets.getPlans().stream()
                    .filter(p -> p.getId().equals(planId) && p.getEndDate().after(new Date()))
                    .findFirst().orElse(null);
            if (plan == null) return;

            warmHeartPlanService.setStudentWarmHeartTargetCache(studentId, planId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }

    private void handler(Map<String, Object> msg) {
        try {
            Long studentId = MapUtils.getLong(msg, "studentId");
            String planId = MapUtils.getString(msg, "planId");

            if (RuntimeMode.lt(Mode.STAGING)) {
                log.info("utopia.assign.new.term.plan.topic msg:{}", JSON.toJSONString(msg));
            }

            NewTermStudentPlanMapper studentTargets = parentNewTermPlanService.getStudentTargets(studentId);
            if (studentTargets == null || studentTargets.getEndDate().before(new Date())) {
                return;
            }

            teacherNewTermPlanService.setStudentNewTermTargetCache(studentId, planId);

        } catch (Exception e) {
            log.error(e.getMessage(), e);
        }
    }
}
