package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroup;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentGroupUser;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.user.consumer.CrmSummaryLoaderClient;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * NewTeacherMessageHandler
 *
 * @author song.wang
 * @date 2017/7/24
 */
@Named
public class NewTeacherMessageHandler extends SpringContainerSupport {

    @Inject
    private CrmSummaryLoaderClient crmSummaryLoaderClient;
    @Inject
    private BaseOrgService baseOrgService;
    @Inject
    private AgentNotifyService agentNotifyService;
    @Inject
    private TeacherLoaderClient teacherLoaderClient;


    public void handle(Integer dateInt){
        // 给专员发送新注册老师的消息
        Set<Long> bdIds = baseOrgService.getGroupUserByRole(AgentRoleType.BusinessDeveloper.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        sendToBusinessDeveloper(bdIds,dateInt);

        // 给市经理发送新注册老师的消息
        Set<Long> cmIds = baseOrgService.getGroupUserByRole(AgentRoleType.CityManager.getId()).stream().map(AgentGroupUser::getUserId).collect(Collectors.toSet());
        sendToCityManager(cmIds,dateInt);
    }

    private void sendToBusinessDeveloper(Collection<Long> userIds,Integer dateInt){
        if(CollectionUtils.isEmpty(userIds)){
            return;
        }

        Map<Long, List<AgentUserSchool>> userSchoolMap = baseOrgService.getUserSchoolByUsers(userIds);
        userSchoolMap.forEach((k, v) -> {
            if(CollectionUtils.isEmpty(v)){
                return;
            }
            List<Long> schoolIds = v.stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toList());
            sendMessage(k, schoolIds, dateInt);
        });

    }

    private void sendToCityManager(Collection<Long> userIds, Integer dateInt){
        if(CollectionUtils.isEmpty(userIds)){
            return;
        }

        userIds.stream().forEach(p -> {
            List<Long> schoolIds = baseOrgService.getManagedSchoolList(p);
            Map<Long, List<AgentUserSchool>> userSchoolMap = baseOrgService.getUserSchoolBySchools(schoolIds);
            Set<Long> targetSchoolIds = schoolIds.stream().filter(t -> !userSchoolMap.containsKey(t)).collect(Collectors.toSet());
            sendMessage(p, targetSchoolIds, dateInt);
        });

    }

    private void sendMessage(Long userId, Collection<Long> schoolIds, Integer dateInt){
        if(CollectionUtils.isEmpty(schoolIds)){
            return;
        }
        Date date = DateUtils.stringToDate(String.valueOf(dateInt), "yyyyMMdd");
        if (date == null) {
            date = new Date();
        }
        Date startTime = DateUtils.stringToDate(DateUtils.dateToString(DateUtils.calculateDateDay(date, -1), "yyyyMMdd"), "yyyyMMdd");
        Date endTime = DateUtils.stringToDate(DateUtils.dateToString(date, "yyyyMMdd"), "yyyyMMdd");
        Map<Long, List<CrmTeacherSummary>> schoolTeacherMap = crmSummaryLoaderClient.loadSchoolTeachers(schoolIds);
        //获取老师ID列表
        List<Long> teacherIdList = new ArrayList<>();
        schoolTeacherMap.forEach((k,v) -> {
            if (CollectionUtils.isNotEmpty(v)){
                v.forEach(item -> {
                    if (null != item){
                        teacherIdList.add(item.getTeacherId());
                    }
                });
            }
        });
        //获取老师主副账号
        Map<Long, Long> subMainTeacherIdMap = teacherLoaderClient.loadMainTeacherIds(teacherIdList);
        //增加过滤条件，创建副账号不需要重复发送消息给市场
        schoolTeacherMap.forEach((k, v) -> {
            List<CrmTeacherSummary> newTeachers = v.stream().filter(p -> p.getRegisterTime() != null && DateUtils.stringToDate(String.valueOf(p.getRegisterTime()),"yyyyMMddHHmmss").getTime() > startTime.getTime() && DateUtils.stringToDate(String.valueOf(p.getRegisterTime()),"yyyyMMddHHmmss").getTime() < endTime.getTime() && !subMainTeacherIdMap.containsKey(p.getTeacherId())).collect(Collectors.toList());
            if(CollectionUtils.isNotEmpty(newTeachers)){
                newTeachers.forEach(p -> {
                    agentNotifyService.sendNotify(AgentNotifyType.NEW_REGISTER_TEACHER.getType(), AgentNotifyType.NEW_REGISTER_TEACHER.getDesc(), p.getRealName() + "@" + ((StringUtils.isBlank(p.getSubject()) ? "" : p.getSubject())) + "@" + (StringUtils.isBlank(p.getSchoolName()) ? "" : p.getSchoolName()), Collections.singleton(userId), String.format("/view/mobile/crm/teacher/teacher_card.vpage?teacherId=%s", p.getTeacherId()));
                });
            }
        });
    }

}
