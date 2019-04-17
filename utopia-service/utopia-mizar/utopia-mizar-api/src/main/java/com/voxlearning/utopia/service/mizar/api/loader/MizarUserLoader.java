package com.voxlearning.utopia.service.mizar.api.loader;

import com.voxlearning.alps.annotation.cache.CacheMethod;
import com.voxlearning.alps.annotation.cache.CacheParameter;
import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.user.*;

import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeUnit;

/**
 * Mizar User Loader
 * Created by alex on 2016/8/16.
 */
@ServiceVersion(version = "1.4.DEV")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface MizarUserLoader extends IPingable {

    List<MizarUser> loadByShopId(final String shopId);

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            key = "ALL",
            type = MizarUser.class,
            writeCache = false
    )
    List<MizarUser> loadAllUsers();

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = MizarUser.class,
            writeCache = false
    )
    MizarUser loadUser(@CacheParameter String userId);

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = MizarUser.class,
            writeCache = false
    )
    Map<String, MizarUser> loadUsers(@CacheParameter(multiple = true) Collection<String> userIds);

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = MizarUser.class,
            writeCache = false
    )
    MizarUser loadUserByMobile(@CacheParameter("MOBILE") String mobile);

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = MizarUser.class,
            writeCache = false
    )
    MizarUser loadUserByAccount(@CacheParameter("ACCOUNT") String account);

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = MizarUserShop.class,
            writeCache = false
    )
    List<MizarUserShop> loadUserShopId(@CacheParameter(value = "uid") String userId);

    List<MizarUserOfficialAccounts> loadUserOfficialAccounts(String userId);

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = MizarDepartment.class,
            writeCache = false
    )
    default MizarDepartment loadDepartment(@CacheParameter String departmentId) {
        return loadDepartments(Collections.singletonList(departmentId)).get(departmentId);
    }

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            type = MizarDepartment.class,
            writeCache = false
    )
    Map<String, MizarDepartment> loadDepartments(@CacheParameter(multiple = true) Collection<String> departmentIds);

    @CacheMethod(
            cacheSystem = CacheSystem.CBS,
            cacheName = "flushable",
            key = "ALL",
            type = MizarDepartment.class,
            writeCache = false
    )
    List<MizarDepartment> loadAllDepartments();

    List<MizarDepartment> loadValidDepartments();

    List<MizarDepartment> loadUserDepartments(String userId, MizarUserRoleType role);

    default List<MizarUser> loadDepartmentUsers(String departmentId) {
        return loadDepartmentUsers(Collections.singleton(departmentId)).get(departmentId);
    }

    List<MizarUser> loadSameDepartmentUsers(String userId, MizarUserRoleType role);

    Map<String, List<MizarUser>> loadDepartmentUsers(Collection<String> departmentIds);

    List<String> loadUserRolesInAllDepartments(String userId);

    List<MizarUserDepartment> loadDepartmentByUserId(String userId);

    List<MizarUserDepartment> loadUserByDepartmentId(String departmentId);

    List<MizarUserDepartment> loadDepartmentByUserIds(Collection<String> userIds);

}
