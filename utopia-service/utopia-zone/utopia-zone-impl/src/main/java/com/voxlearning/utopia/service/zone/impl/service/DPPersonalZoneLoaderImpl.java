package com.voxlearning.utopia.service.zone.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.document.UserPrivilege;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeLoaderClient;
import com.voxlearning.utopia.service.privilege.client.PrivilegeServiceClient;
import com.voxlearning.utopia.service.zone.api.DPPersonalZoneLoader;
import com.voxlearning.utopia.service.zone.api.DPPersonalZoneService;
import com.voxlearning.utopia.service.zone.api.entity.StudentInfo;
import com.voxlearning.utopia.service.zone.impl.loader.PersonalZoneLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.List;
import java.util.Objects;

@Named
@ExposeService(interfaceClass = DPPersonalZoneLoader.class)
public class DPPersonalZoneLoaderImpl implements DPPersonalZoneLoader{

    @Inject private PrivilegeLoaderClient privilegeLoaderClient;
    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @Override
    public Boolean existValidHeadWearByCode(Long studentId, String headWearCode) {
        Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(headWearCode);
        if (Objects.isNull(privilege)) {
            return false;
        }
        return privilegeLoaderClient.existValidPrivilege(studentId, privilege.getId());
    }
}
