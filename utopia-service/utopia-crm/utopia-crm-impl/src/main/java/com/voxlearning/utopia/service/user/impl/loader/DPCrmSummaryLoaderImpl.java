package com.voxlearning.utopia.service.user.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.entity.crm.CrmTeacherSummary;
import com.voxlearning.utopia.service.user.api.DPCrmSummaryLoader;
import com.voxlearning.utopia.service.user.api.SensitiveUserDataService;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@Service(interfaceClass = DPCrmSummaryLoader.class)
@ExposeService(interfaceClass = DPCrmSummaryLoader.class)
public class DPCrmSummaryLoaderImpl implements DPCrmSummaryLoader {

    @Inject private SensitiveUserDataService sensitiveUserDataService;

    @Inject
    private CrmSummaryLoaderImpl crmSummaryLoader;
    @Override
    public CrmTeacherSummary loadTeacherSummary(Long teacherId) {
        return crmSummaryLoader.loadTeacherSummary(teacherId);
    }

    @Override
    public CrmTeacherSummary loadTeacherSummaryByMobile(String teacherMobile) {
        String encMobile = sensitiveUserDataService.encodeMobile(teacherMobile);
        return crmSummaryLoader.loadTeacherSummaryByMobile(encMobile);
    }
}
