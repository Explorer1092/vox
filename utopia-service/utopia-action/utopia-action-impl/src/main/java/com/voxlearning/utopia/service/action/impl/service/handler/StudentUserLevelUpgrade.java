package com.voxlearning.utopia.service.action.impl.service.handler;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.service.action.api.document.Privilege;
import com.voxlearning.utopia.service.action.api.document.UserPrivilege;
import com.voxlearning.utopia.service.action.api.event.ActionEvent;
import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import com.voxlearning.utopia.service.action.api.support.PrivilegeType;
import com.voxlearning.utopia.service.action.impl.service.AbstractActionEventHandler;
import com.voxlearning.utopia.service.privilege.client.PrivilegeBufferServiceClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * @author xinxin
 * @since 1/22/18
 */
@Named(value = "actionEventHandler.studentUserLevelUpgrade")
public class StudentUserLevelUpgrade extends AbstractActionEventHandler {

    @Inject private PrivilegeBufferServiceClient privilegeBufferServiceClient;

    @Override
    public ActionEventType getEventType() {
        return ActionEventType.StudentUserLevelUpgrade;
    }

    @Override
    public void handle(ActionEvent event) {
        if (null == event) {
            return;
        }

        if (MapUtils.isNotEmpty(event.getAttributes()) && event.getAttributes().containsKey("level")) {
            int level = SafeConverter.toInt(event.getAttributes().get("level"));
            String headWearCode = getHeadWearCode(level);
            if (StringUtils.isBlank(headWearCode)) {
                return;
            }
            Privilege privilege = privilegeBufferServiceClient.getPrivilegeBuffer().loadByCode(headWearCode);

            Boolean userHasPrivilege = userHasPrivilege(event.getUserId(), privilege);
            if (!userHasPrivilege) {
                // userId-type-privilegeId 作为唯一主键
                String id = event.getUserId() + "-" + PrivilegeType.Head_Wear.name() + "-" + privilege.getId();

                UserPrivilege userPrivilege = new UserPrivilege();
                userPrivilege.setId(id);
                userPrivilege.setUserId(event.getUserId());
                userPrivilege.setType(PrivilegeType.Head_Wear.name());
                userPrivilege.setPrivilegeId(privilege.getId());
                privilegeServiceClient.getPrivilegeService().insertUserPrivilegeWithoutResponse(userPrivilege);
            }
        }
    }

    private Boolean userHasPrivilege(Long userId, Privilege privilege) {
        UserPrivilege userPrivilege = null;
        List<UserPrivilege> userPrivileges = privilegeLoaderClient.findUserPrivileges(userId, PrivilegeType.Head_Wear);
        if (CollectionUtils.isNotEmpty(userPrivileges)) {
            userPrivilege = userPrivileges.stream().filter(up -> up.getPrivilegeId().equals(privilege.getId())).findFirst().orElse(null);
        }
        return null != userPrivilege;
    }

    private String getHeadWearCode(Integer level) {
        switch (level) {
            case 1:
                return Privilege.SpecialPrivileges.阿呜.getCode();
            case 2:
                return Privilege.SpecialPrivileges.阿飘.getCode();
            case 3:
                return Privilege.SpecialPrivileges.阿目.getCode();
            case 4:
                return Privilege.SpecialPrivileges.阿章.getCode();
            case 5:
                return Privilege.SpecialPrivileges.毛斯.getCode();
            case 6:
                return Privilege.SpecialPrivileges.阿龙.getCode();
            default:
                return null;
        }
    }
}
