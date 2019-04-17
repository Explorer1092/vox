package com.voxlearning.utopia.service.mizar.api.service;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarDepartment;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by wangshichao on 16/9/6.
 */

@ServiceVersion(version = "1.3.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarUserService {

    MapMessage editMizarUserPassWord(String userId, String passWord);

    MizarUser login(String name, String passWord);

    MapMessage addMizarUser(MizarUser mizarUser);

    MapMessage editMizarUser(MizarUser mizarUser);

    MapMessage closeAccount(String userId);

    MapMessage disableUserShop(String userId, String shopId);

    MapMessage disableUserOfficialAccounts(String userId, String accountsKey);

    MapMessage saveUserShops(String userId, Collection<String> shopIds);

    MapMessage saveUserOfficialAccounts(String userId,  List<OfficialAccounts> officialAccountsList);

    MapMessage upsertDepartment(MizarDepartment department);

    MapMessage closeDepartment(String departmentId);

    MapMessage addDepartmentUser(String departmentId, String userId,List<Integer> roles);

    MapMessage removeDepartmentUser(String departmentId, String userId);

}
