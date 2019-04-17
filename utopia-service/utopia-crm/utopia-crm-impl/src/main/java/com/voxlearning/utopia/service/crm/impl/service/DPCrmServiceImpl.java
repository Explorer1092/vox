package com.voxlearning.utopia.service.crm.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.crm.api.DPCrmService;
import com.voxlearning.utopia.service.crm.impl.loader.agent.AgentOrgLoaderImpl;
import com.voxlearning.utopia.service.crm.impl.loader.agent.TeacherAgentLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;


/**
 * CRM dubbo proxy service implementation of {@link DPCrmService}.
 *
 * @author yuechen.wang
 * @since 2017-08-04
 */
@Named
@Service(interfaceClass = DPCrmService.class)
@ExposeService(interfaceClass = DPCrmService.class)
public class DPCrmServiceImpl extends SpringContainerSupport implements DPCrmService {

    @Inject private AgentOrgLoaderImpl agentOrgLoader;
    @Inject private TeacherAgentLoaderImpl teacherAgentLoader;

    @Override
    public MapMessage isDictSchool(Long schoolId) {
        if (schoolId == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return MapMessage.successMessage()
                .add("isDictSchool", Boolean.TRUE.equals(agentOrgLoader.isDictSchool(schoolId)));
    }

    @Override
    public Map<String, Object> getSchoolManager(Long schoolId) {
        return teacherAgentLoader.getSchoolManager(schoolId);
    }

}
