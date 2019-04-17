package com.voxlearning.utopia.service.mizar.consumer.service;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarDepartment;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.loader.MizarUserLoader;
import com.voxlearning.utopia.service.mizar.api.service.MizarUserService;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by wangshichao on 16/9/6.
 */
public class MizarUserServiceClient {

    @Getter
    @ImportService(interfaceClass = MizarUserService.class)
    private MizarUserService remoteReference;

    @ImportService(interfaceClass = MizarUserLoader.class)
    private MizarUserLoader mizarUserLoader;

    public MapMessage addMizarUser(MizarUser user) {
        if (user == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.addMizarUser(user);
    }

    public MapMessage editMizarUser(MizarUser user) {
        if (user == null) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.editMizarUser(user);
    }

    public MapMessage resetPassword(String userId, String pwd) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(pwd)) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.editMizarUserPassWord(userId, pwd);
    }

    public MapMessage closeAccount(String userId) {
        if (StringUtils.isBlank(userId)) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.closeAccount(userId);
    }

    public MapMessage disableUserShop(String userId, String shopId) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.disableUserShop(userId, shopId);
    }

    public MapMessage disableOfficialAccounts(String userId, String accountsKey) {
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(accountsKey)) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.disableUserOfficialAccounts(userId, accountsKey);
    }

    public MapMessage saveUserShops(String userId, Collection<String> shopIds) {
        if (StringUtils.isBlank(userId) || CollectionUtils.isEmpty(shopIds)) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.saveUserShops(userId, shopIds);
    }

    public MapMessage saveOfficialAccounts(String userId, List<OfficialAccounts> officialAccountsList) {
        if (StringUtils.isBlank(userId) || CollectionUtils.isEmpty(officialAccountsList)) {
            return MapMessage.errorMessage("参数错误");
        }
        return remoteReference.saveUserOfficialAccounts(userId, officialAccountsList);
    }

    public MapMessage editMizarDepartment(MizarDepartment department) {
        if (department == null)
            return MapMessage.errorMessage("参数错误");

        return remoteReference.upsertDepartment(department);
    }

    public MapMessage addDepartmentUser(String departmentId, String userId, List<Integer> roles) {
        return remoteReference.addDepartmentUser(departmentId, userId, roles);
    }

    public MapMessage addDepartment(String userId, Map<String, List<Integer>> groupRoles) {
        // 先移除用户所有的组信息，再加上新的
        MapMessage resultMsg = MapMessage.successMessage();
        mizarUserLoader.loadUserDepartments(userId, null)
                .forEach(d -> removeDepartmentUser(d.getId(), userId));

        groupRoles.forEach((id, roles) -> {
            MapMessage msg = addDepartmentUser(id, userId, roles);
            resultMsg.putAll(msg);
        });

        return resultMsg;
    }

    public MapMessage removeDepartmentUser(String departmentId, String userId) {
        return remoteReference.removeDepartmentUser(departmentId, userId);
    }

    public MapMessage closeDepartment(String departmentId) {
        return remoteReference.closeDepartment(departmentId);
    }

}
