package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.zone.api.DPPersonalZoneService;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.impl.loader.PersonalZoneLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Objects;

@Named
@ExposeService(interfaceClass = DPPersonalZoneService.class)
public class DPPersonalZoneServiceImpl implements DPPersonalZoneService{

    @Inject private PersonalZoneServiceImpl personalZoneSrv;
    @Inject private PersonalZoneLoaderImpl personalZoneLoader;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;
    @Inject private PrivilegeServiceClient privilegeServiceClient;

    @Override
    public MapMessage changeHeadWear(Long studentId, String headWearId) {
        return personalZoneSrv.changeHeadWear(studentId,headWearId);
    }

    @Override
    public MapMessage grantHeadWearByCode(Long studentId, String headWearCode, Date expiryDate) {
        Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(headWearCode);
        if (Objects.isNull(privilege)) {
            return MapMessage.errorMessage("头饰不存在！");
        }
        return privilegeServiceClient.getPrivilegeService().grantPrivilege(studentId, privilege, expiryDate);
    }

    @Override
    public MapMessage changeHeadWearByCode(Long studentId, String headWearCode) {
        Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(headWearCode);
        if (Objects.isNull(privilege)) {
            return MapMessage.errorMessage("头饰不存在！");
        }
        return personalZoneSrv.changeHeadWear(studentId, privilege.getId());
    }

    @Override
    public StudentInfo loadStudentInfo(Long studentId) {
        return personalZoneLoader.loadStudentInfo(studentId);
    }

}
