package com.voxlearning.utopia.agent.listener.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.agent.constants.AgentNotifyType;
import com.voxlearning.utopia.agent.mapper.ClazzAlterMapper;
import com.voxlearning.utopia.agent.service.common.BaseDictService;
import com.voxlearning.utopia.agent.service.common.BaseOrgService;
import com.voxlearning.utopia.agent.service.mobile.resource.AgentResourceMapperService;
import com.voxlearning.utopia.agent.service.montortool.MonitorToolService;
import com.voxlearning.utopia.agent.service.notify.AgentNotifyService;
import com.voxlearning.utopia.agent.service.sysconfig.AgentDictSchoolService;
import com.voxlearning.utopia.agent.support.AgentGroupSupport;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentGroupRoleType;
import com.voxlearning.utopia.service.crm.api.entities.agent.AgentUserSchool;
import com.voxlearning.utopia.service.user.api.constants.ClazzTeacherAlterationState;
import com.voxlearning.utopia.service.user.consumer.TeacherLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * AlterationRemindHandler
 *
 * @author Yuechen.Wang 2016/9/9
 * @author yaguang.wang 2017/7/26
 */
@Named
public class AlterationRemindHandler extends SpringContainerSupport {

    @Inject private BaseOrgService baseOrgService;
    @Inject private AgentDictSchoolService agentDictSchoolService;
    @Inject private AgentNotifyService agentNotifyService;
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject private MonitorToolService monitorToolService;
    @Inject private BaseDictService baseDictService;
    @Inject private AgentGroupSupport agentGroupSupport;


    public void executeCommand(Long time) {

        List<ClazzAlterMapper> mapperList = monitorToolService.loadBeginSomeDayClassAlterData(time,10)
                .stream().filter(p -> p.getState() == ClazzTeacherAlterationState.PENDING).collect(Collectors.toList());
        // 过滤掉字典表之后结果根据学校分组
        Map<Long, List<ClazzAlterMapper>> schoolMapper = mapperList
                .stream()
                .filter(mapper -> baseDictService.isDictSchool(mapper.getSchoolId()))
                .collect(Collectors.groupingBy(ClazzAlterMapper::getSchoolId));

        // 根据学校找到对应专员
        Map<Long, List<AgentUserSchool>> schoolUsers = baseOrgService.getUserSchoolBySchools(schoolMapper.keySet())
                .values().stream()
                .flatMap(List::stream)
                .collect(Collectors.groupingBy(AgentUserSchool::getUserId));
        // 所有bd的学校
        Set<Long> allBdSchoolIds = new HashSet<>();
        // 整合信息
        for (Map.Entry<Long, List<AgentUserSchool>> entry : schoolUsers.entrySet()) {
            Long userId = entry.getKey();
            Set<Long> schools = entry.getValue().stream().map(AgentUserSchool::getSchoolId).collect(Collectors.toSet());
            if (CollectionUtils.isEmpty(schools)) {
                continue;
            }
            allBdSchoolIds.addAll(schools);
            List<ClazzAlterMapper> userAlters = new ArrayList<>();
            schools.forEach(p -> {
                List<ClazzAlterMapper> clazzAlterMappers = schoolMapper.get(p);
                if (CollectionUtils.isNotEmpty(clazzAlterMappers)) {
                    userAlters.addAll(clazzAlterMappers);
                }
            });
            sendNotify(userId, userAlters);
            // 发送信息
            //schools.stream().filter(schoolMapper::containsKey).forEach(schoolId -> sendNotify(userId, schoolId, schoolMapper.get(schoolId)));
        }
        Set<Long> allCmSchoolIds = schoolMapper.keySet().stream().filter(s -> !allBdSchoolIds.contains(s)).collect(Collectors.toSet());
        for(Long schoolId : allCmSchoolIds){
            // 获取负责该学校的分区列表
            List<Long> groupIds = agentGroupSupport.getGroupIdsBySchool(schoolId, Collections.singletonList(AgentGroupRoleType.City));
            if(CollectionUtils.isNotEmpty(groupIds)){
                Long cityManager = baseOrgService.getGroupManager(groupIds.get(0));
                if(cityManager != null){
                    sendNotify(cityManager, schoolMapper.get(schoolId));
                }
            }
        }
    }

    private void sendNotify(Long userId, List<ClazzAlterMapper> userAlters) {
        if (userId == null || CollectionUtils.isEmpty(userAlters)) {
            return;
        }
        //CrmSchoolSummary school = crmSummaryLoaderClient.loadSchoolSummary(schoolId);
        agentNotifyService.sendNotify(
                AgentNotifyType.ALTERATION_REMIND_NEW.getType(),
                "换班提醒",
                StringUtils.formatMessage("尚有{}条换班申请未处理，请及时处理！", userAlters.size()),
                Collections.singleton(userId),
                "/mobile/performance/clazz_alter.vpage"
        );
    }


}
