package com.voxlearning.utopia.service.mizar.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.user.*;
import com.voxlearning.utopia.service.mizar.api.loader.MizarUserLoader;
import com.voxlearning.utopia.service.mizar.impl.dao.user.*;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static java.util.stream.Collectors.toList;
import static java.util.stream.Collectors.toSet;

/**
 * Mizar User Loader Implementation Class
 * Created by alex on 2016/8/16.
 */
@Named
@Service(interfaceClass = MizarUserLoader.class)
@ExposeService(interfaceClass = MizarUserLoader.class)
public class MizarUserLoaderImpl implements MizarUserLoader {

    @Inject private MizarUserDao mizarUserDao;
    @Inject private MizarUserShopDao mizarUserShopDao;
    @Inject private MizarDepartmentDao mizarDepartmentDao;
    @Inject private MizarUserDepartmentDao mizarUserDepartmentDao;

    @Inject private MizarUserOfficialAccountsDao mizarUserOfficialAccountsDao;

    @Override
    public List<MizarUser> loadAllUsers() {
        return mizarUserDao.findAll();
    }

    @Override
    public MizarUser loadUser(String userId) {
        return mizarUserDao.findById(userId);
    }

    @Override
    public Map<String, MizarUser> loadUsers(Collection<String> userIds) {
        return mizarUserDao.findByIds(userIds);
    }

    @Override
    public MizarUser loadUserByMobile(String mobile) {
        return mizarUserDao.findByMobile(mobile);
    }

    @Override
    public MizarUser loadUserByAccount(String account) {
        return mizarUserDao.findByAccount(account);
    }

    @Override
    public List<MizarUserShop> loadUserShopId(String userId) {
        return mizarUserShopDao.findByUser(userId);
    }

    public List<MizarUserOfficialAccounts> loadUserOfficialAccounts(String userId) {
        return mizarUserOfficialAccountsDao.loadByUserId(userId);
    }

    @Override
    public Map<String, MizarDepartment> loadDepartments(Collection<String> departmentIds) {
        return mizarDepartmentDao.loads(departmentIds);
    }

    public List<MizarUser> loadByShopId(final String shopId) {
        List<MizarUserShop> mizarUserShopList = mizarUserShopDao.loadByShopId(shopId);
        if (CollectionUtils.isEmpty(mizarUserShopList)) {
            return Collections.emptyList();
        }
        Set<String> userIdSet = mizarUserShopList.stream().filter(o -> StringUtils.isNotBlank(o.getUserId()))
                .map(MizarUserShop::getUserId).collect(toSet());
        if (CollectionUtils.isEmpty(userIdSet)) {
            return Collections.emptyList();
        }
        Map<String, MizarUser> resultMap = mizarUserDao.loads(userIdSet);
        return resultMap.values().stream().collect(toList());
    }

    @Override
    public List<MizarDepartment> loadAllDepartments() {
        return mizarDepartmentDao.findAll();
    }

    @Override
    public List<MizarDepartment> loadValidDepartments() {
        List<MizarDepartment> allDepartments = mizarDepartmentDao.findAll();
        return allDepartments.stream()
                .filter(MizarDepartment::isValid)
                .collect(Collectors.toList());
    }

    @Override
    public List<MizarDepartment> loadUserDepartments(String userId, MizarUserRoleType role) {
        List<String> userDepartments = mizarUserDepartmentDao.findByUser(userId)
                .stream()
                .filter(u -> u.containsRole(role))
                .map(MizarUserDepartment::getDepartmentId)
                .distinct()
                .collect(Collectors.toList());

        return mizarDepartmentDao.loads(userDepartments).values()
                .stream()
                .filter(MizarDepartment::isValid)
                .collect(toList());
    }

    @Override
    public List<MizarUser> loadSameDepartmentUsers(String userId, MizarUserRoleType role) {
        List<String> deptIds = mizarUserDepartmentDao.findByUser(userId)
                .stream()
                .map(MizarUserDepartment::getDepartmentId)
                .distinct()
                .collect(toList());

        List<String> users = mizarUserDepartmentDao.findByDepartments(deptIds).values()
                .stream()
                .flatMap(List::stream)
                .filter(u -> u.containsRole(role))
                .map(MizarUserDepartment::getUserId)
                .filter(u -> !userId.equals(u))
                .distinct()
                .collect(toList());

        return mizarUserDao.loads(users).values()
                .stream()
                .filter(MizarUser::isValid)
                .collect(toList());
    }

    @Override
    public Map<String, List<MizarUser>> loadDepartmentUsers(Collection<String> departmentIds) {
        Map<String, List<MizarUserDepartment>> byDepartments = mizarUserDepartmentDao.findByDepartments(departmentIds);

        List<MizarUser> allUsers = mizarUserDao.findAll().stream().filter(MizarUser::isValid).collect(toList());

        Map<String, List<MizarUser>> departmentUsers = new HashMap<>();

        byDepartments.entrySet().forEach(e -> {
            Set<String> userIds = e.getValue().stream()
                    .map(MizarUserDepartment::getUserId)
                    .collect(toSet());
            List<MizarUser> users = allUsers.stream().filter(u -> userIds.contains(u.getId())).collect(toList());
            departmentUsers.put(e.getKey(), users);
        });
        return departmentUsers;
    }

    /**
     * 获得用户在所有组下面的角色列表，id格式是“部门id-角色数字”
     *
     * @param userId
     * @return
     */
    @Override
    public List<String> loadUserRolesInAllDepartments(String userId) {
        return mizarUserDepartmentDao.findByUser(userId)
                .stream()
                .filter(d -> CollectionUtils.isNotEmpty(d.getRoles()))
                .flatMap(d -> d.getRoles()
                        .stream()
                        .map(role -> d.getDepartmentId() + "-" + role)
                ).collect(Collectors.toList());
    }

    @Override
    public List<MizarUserDepartment> loadDepartmentByUserId(String userId) {
        return mizarUserDepartmentDao.findByUser(userId);
    }

    @Override
    public List<MizarUserDepartment> loadUserByDepartmentId(String departmentId) {
        return mizarUserDepartmentDao.findByDepartment(departmentId);
    }

    @Override
    public List<MizarUserDepartment> loadDepartmentByUserIds(Collection<String> userIds) {
        return mizarUserDepartmentDao.findByUserIds(userIds);
    }

}
