package com.voxlearning.utopia.agent.listener;

/**
 * @Auther: DELL7050
 * @Date: 2018/6/14 16:34
 * @Description:
 */

import com.voxlearning.alps.annotation.meta.UserType;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.workrecord.AgentRegisterTeacherStatisticsService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "utopia.user.plain.topic"),
                @PubsubDestination(system = PubsubSystem.KFK, connection = "main-backup", topic = "utopia.user.plain.topic")
        }
)
public class AgentTeacherRegisteredListener extends SpringContainerSupport implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject private AgentNotifyService agentNotifyService;
    @Inject private TeacherLoaderClient teacherLoaderClient;
    @Inject private BaseOrgService baseOrgService;
    @Inject private SchoolLoaderClient schoolLoaderClient;
    @Inject private AgentGroupSupport agentGroupSupport;
    @Inject private AgentRegisterTeacherStatisticsService agentRegisterTeacherStatisticsService;
    @Override
    public void onMessage(Message message) {
        Map map = null;
        Object decoded = message.decodeBody();
        if (decoded instanceof String) {
            String messageText = (String) decoded;
            map = JsonUtils.fromJson(messageText);
        }else if (decoded instanceof Map)
            map = (Map) decoded;

        if (map == null) {
            logger.error("AgentTeacherRegisteredListener error message {}", JsonUtils.toJson(message.decodeBody()));
            return;
        }

        if("USER_SCHOOL_CHANGE".equals(map.get("behaviorType"))){
//            emailServiceClient.createPlainEmail().body("新注册老师收到消息 解析 后参数：" + JsonUtils.toJson(map)).subject("新注册老师").to("xianlong.zhang@17zuoye.com;").send();
            Map<String,Object> dataMap = (Map<String,Object>)map.get("data");
            Long teacherId = SafeConverter.toLong(dataMap.get("userId"));
            Teacher teacher = teacherLoaderClient.loadTeacher(teacherId);

            if(teacher == null || teacher.getUserType() != UserType.TEACHER.getType()) return;

            Long schoolId = SafeConverter.toLong(dataMap.get("schoolId"));
            //根据学校id查询学校所属专员
            List<AgentUserSchool> agentUserSchools = baseOrgService.getUserSchoolBySchool(schoolId);
            Set<Long> userIds = agentUserSchools.stream().map(AgentUserSchool::getUserId).collect(Collectors.toSet());
            if(CollectionUtils.isEmpty(userIds)){//学校没有分配给专员时判断有没有市经理  如果有发消息给市经理
                List<Long> groupIds = agentGroupSupport.getGroupIdsBySchool(schoolId, Collections.singletonList(AgentGroupRoleType.City));
                if(CollectionUtils.isNotEmpty(groupIds)){
                    Long cityManager = baseOrgService.getGroupManager(groupIds.get(0));
                    if(cityManager != null){
                        userIds.add(cityManager);
                    }
                }
            }
            School school = schoolLoaderClient.getSchoolLoader().loadSchool(schoolId).getUninterruptibly();
            String subject = "";
            if( teacher!= null && teacher.getSubject() != null){
                subject =  teacher.getSubject().toString();
            }
            //判断主账号  如果主账号为空证明是新老师   新老师需要发送消息
            Long mainTeacherId = teacherLoaderClient.loadMainTeacherId(teacherId);
            if(mainTeacherId == null){
                agentNotifyService.sendNotify(AgentNotifyType.NEW_REGISTER_TEACHER.getType(), AgentNotifyType.NEW_REGISTER_TEACHER.getDesc(), teacher.fetchRealname() + "@" + subject +
                        "@" + (school != null && StringUtils.isNotBlank(school.getCname()) ? school.getCname() : "") + "@" + teacherId, userIds, String.format("/view/mobile/crm/teacher/teacher_card.vpage?teacherId=%s", teacherId));
                agentRegisterTeacherStatisticsService.generateRegisterTeacherData(subject,schoolId);
            }

        }
    }
}
