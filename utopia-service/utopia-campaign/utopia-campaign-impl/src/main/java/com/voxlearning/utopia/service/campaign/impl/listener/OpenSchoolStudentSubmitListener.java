package com.voxlearning.utopia.service.campaign.impl.listener;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.alps.spi.pubsub.*;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageEncodeMode;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.raikou.sdk.api.RaikouSDK;
import com.voxlearning.raikou.service.clazz.api.entity.GroupTeacherTuple;
import com.voxlearning.utopia.service.campaign.impl.service.OpenSchoolTestServiceImpl;
import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import com.voxlearning.utopia.service.user.consumer.DeprecatedGroupLoaderClient;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@Slf4j
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.newexam.student.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.newexam.student.topic")
        }
)
public class OpenSchoolStudentSubmitListener extends SpringContainerSupport implements MessageListener {

    @AlpsPubsubPublisher(topic = "utopia.newexam.student.topic", messageEncodeMode = MessageEncodeMode.PLAIN_TXT)
    private MessagePublisher messagePublisher;

    @Inject private RaikouSDK raikouSDK;

    @Inject
    private DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject
    private OpenSchoolTestServiceImpl openSchoolTestService;

    @Override
    public void onMessage(Message message) {
        Map<String, Object> msgMap = new HashMap<>();

        Object body = message.decodeBody();
        if (body instanceof String) {
            msgMap = JsonUtils.fromJson((String) body);
        } else if (body instanceof Map) {
            msgMap = (Map) body;
        } else {
            logger.warn("utopia.newexam.student.topic msg decode message failed!", JsonUtils.toJson(message.decodeBody()));
        }

        if (RuntimeMode.le(Mode.STAGING)) {
            logger.info("utopia.newexam.student.topic msg {}", JsonUtils.toJson(msgMap));
        }

        String messageType = MapUtils.getString(msgMap, "messageType");

        if (!Objects.equals(messageType, "submitApply")) {
            return;
        }

        String newExamId = MapUtils.getString(msgMap, "newExamId");
        if (!OpenSchoolTestServiceImpl.newExamId.contains(newExamId)) {
            return;
        }

        Long studentId = MapUtils.getLong(msgMap, "studentId");

        List<GroupMapper> studentGroupList = deprecatedGroupLoaderClient.loadStudentGroups(studentId, false);
        Set<GroupMapper> mathGroupList = studentGroupList.stream()
                .filter(i -> Objects.equals(i.getSubject(), Subject.MATH))
                .collect(Collectors.toSet());

        for (GroupMapper itemGroup : mathGroupList) {
            Long groupId = itemGroup.getId();
            Set<Long> teacherSet = raikouSDK.getClazzClient()
                    .getGroupTeacherTupleServiceClient()
                    .findByGroupId(groupId)
                    .stream()
                    .filter(GroupTeacherTuple::isValidTrue)
                    .map(i -> openSchoolTestService.getMainTeacher(i.getTeacherId()))
                    .collect(Collectors.toSet());

            for (Long teacherIdItem : teacherSet) {
                // 这里不判断老师是否报名了, 直接加
                Long submitCount = openSchoolTestService.incrStudentSubmit(teacherIdItem);

                // 必须是等于, 利用 incr 的原子性判定是否可以发奖励
                if (submitCount == OpenSchoolTestServiceImpl.SEND_REWARD_LIMIT) {
                    openSchoolTestService.sendTeacherReward(teacherIdItem, 150, "新学期开学第一课奖励", "OPEN_SCHOOL_ASSIGN_");
                }
            }
        }
    }
}