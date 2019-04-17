package com.voxlearning.utopia.service.mizar.consumer.loader;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.user.*;
import com.voxlearning.utopia.service.mizar.api.loader.MizarUserLoader;
import lombok.Getter;

import java.util.*;
import java.util.stream.Collectors;

/**
 * Mizar User Loader Client
 * Created by alex on 2016/8/16.
 */
public class MizarUserLoaderClient {

    @Getter
    @ImportService(interfaceClass = MizarUserLoader.class)
    private MizarUserLoader remoteReference;

    public List<MizarUser> loadAllUsers() {
        return loadAllUsers(null, null);
    }

    public List<MizarUser> loadByShopId(final String shopId) {
        if (StringUtils.isBlank(shopId)) {
            return Collections.emptyList();
        }
        return remoteReference.loadByShopId(shopId);
    }

    public List<MizarUser> loadAllUsers(String userName) {
        return loadAllUsers(userName, null);
    }

    public List<MizarUser> loadAllUsers(String userName, Integer userStatus) {
        List<MizarUser> allUsers = remoteReference.loadAllUsers();
        if (StringUtils.isNoneBlank(userName)) {
            allUsers = allUsers.stream().filter(p -> p.getAccountName().contains(userName)).collect(Collectors.toList());
        }
        if (userStatus != null && userStatus >= 0) {
            allUsers = allUsers.stream().filter(p -> Objects.equals(p.getStatus(), userStatus)).collect(Collectors.toList());
        }
        return allUsers;
    }

    public List<MizarUser> loadAllUsersWithGroup(String userName, Integer userStatus) {
        List<MizarUser> allUsers = loadAllUsers(userName, userStatus);
        for (MizarUser user : allUsers) {
            user.setDepartments(loadUserDepartments(user.getId()));
        }

        return allUsers;
    }

    public List<MizarUser> loadSameDepartmentUser(String userId, MizarUserRoleType role) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        return remoteReference.loadSameDepartmentUsers(userId, role);
    }

    public MizarUser loadUser(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        return remoteReference.loadUser(userId);
    }

    public Map<String, MizarUser> loadUsers(Collection<String> userIds) {
        if (CollectionUtils.isEmpty(userIds)) {
            return Collections.emptyMap();
        }
        return remoteReference.loadUsers(userIds);
    }

    public MizarUser loadUserIncludesDisabled(String userId) {
        if (StringUtils.isBlank(userId)) {
            return null;
        }
        List<MizarUser> allUsers = loadAllUsers();
        return allUsers.stream().filter(p -> Objects.equals(userId, p.getId())).findFirst().orElse(null);
    }

    public List<String> loadUserShopId(String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        return remoteReference.loadUserShopId(userId)
                .stream()
                .map(MizarUserShop::getShopId)
                .collect(Collectors.toList());
    }

    public List<MizarUserOfficialAccounts> loadUserOfficialAccounts(String userId) {
        if (StringUtils.isBlank(userId)) {
            return Collections.emptyList();
        }
        return remoteReference.loadUserOfficialAccounts(userId);
    }

    public MizarUser loadUserByToken(String token) {
        if (StringUtils.isBlank(token)) {
            return null;
        }
        if (MobileRule.isMobile(token)) {
            return remoteReference.loadUserByMobile(token);
        }
        return remoteReference.loadUserByAccount(token);
    }

    public boolean checkAccountAndMobile(String account, String mobile, String ignoreId) {
        List<MizarUser> allUsers = loadAllUsers();
        boolean ignored = StringUtils.isBlank(ignoreId);
        return allUsers.stream()
                .filter(p -> StringUtils.equals(account, p.getAccountName()) || StringUtils.equals(mobile, p.getMobile()))
                .anyMatch(p -> ignored || !Objects.equals(ignoreId, p.getId()));
    }

    public List<MizarDepartment> loadAllDepartments() {
        return remoteReference.loadAllDepartments();
    }

    public List<MizarDepartment> loadAllDepartments(String name, int status) {
        return loadAllDepartments().stream()
                .filter(d -> StringUtils.isEmpty(name) || d.getDepartmentName().contains(name))
                .filter(d -> {
                    if (status == -1)
                        return true;
                    else if (status == 1)
                        return !d.getDisabled();
                    else if (status == 0)
                        return d.getDisabled();
                    else
                        return false;
                })
                .collect(Collectors.toList());
    }

    public boolean checkDepartmentName(String name, String excludeId) {
        List<MizarDepartment> allDepartments = loadAllDepartments();
        return allDepartments.stream()
                .filter(d -> Objects.equals(d.getDepartmentName(), name))
                .filter(d -> !Objects.equals(d.getId(), excludeId))
                .count() > 0;
    }

    public MizarDepartment loadDepartment(String departmentId) {
        return remoteReference.loadDepartment(departmentId);
    }

    public List<MizarUser> loadDepartmentUsers(String departmentId) {
        return remoteReference.loadDepartmentUsers(departmentId);
    }

    public Map<String, List<MizarUser>> loadDepartmentUsers(Collection<String> departmentIds) {
        return remoteReference.loadDepartmentUsers(departmentIds);
    }

    public List<MizarDepartment> loadUserDepartments(String userId) {
        return loadUserDepartments(userId, null);
    }

    public List<MizarDepartment> loadUserDepartments(String userId, MizarUserRoleType role) {
        return remoteReference.loadUserDepartments(userId, role);
    }

    public List<Map<String, Object>> buildRoleTree(List<String> roleCheckList, List<Integer> userRoleTypes) {

        boolean isInfantOp = userRoleTypes.contains(MizarUserRoleType.INFANT_OP.getId());
        boolean isInfantSchool = userRoleTypes.contains(MizarUserRoleType.INFANT_SCHOOL.getId());
        boolean isNotAdmin = !userRoleTypes.contains(MizarUserRoleType.MizarAdmin.getId());
        // 加载有效部门
        return loadAllDepartments(null, 1)
                .stream()
                .filter(p -> {
                    boolean hasInfantSchool = CollectionUtils.isNotEmpty(p.getOwnRoles()) && p.getOwnRoles().contains(MizarUserRoleType.INFANT_SCHOOL.getId());
                    if ((isInfantOp || isInfantSchool)) {
                        // 学前运行和学前学校 不能有学前运行角色
                        p.getOwnRoles().remove(MizarUserRoleType.INFANT_OP.getId());
                        return hasInfantSchool;
                    } else
                        return !isNotAdmin || !hasInfantSchool;
                })
                .map(d -> createDNode(d, roleCheckList))
                .filter(Objects::nonNull)
                .collect(Collectors.toList());
    }

    private Map<String, Object> createDNode(MizarDepartment d, List<String> roleCheckList) {
        Map<String, Object> dNode = new HashMap<>();
        dNode.put("title", d.getDepartmentName());
        dNode.put("key", d.getId());
        dNode.put("hideCheckbox", true);
        dNode.put("type", "group");

        if (CollectionUtils.isEmpty(d.getOwnRoles()))
            return null;
        dNode.put("children", d.getOwnRoles().stream().map(a -> {
            MizarUserRoleType roleType = MizarUserRoleType.of(a);
            if (roleType == null)
                return null;
            String groupRoleId = d.getId() + "-" + roleType.getId();

            Map<String, Object> roleNode = new HashMap<>();
            roleNode.put("title", roleType.getDesc());
            roleNode.put("key", groupRoleId);

            if (roleCheckList.contains(groupRoleId)) {
                roleNode.put("selected", true);
                // 有一个子结点被选中的话，父结点就展开
                dNode.put("expanded", true);
            }

            roleNode.put("type", "role");
            return roleNode;
        }).filter(Objects::nonNull).collect(Collectors.toList()));
        return dNode;
    }

    /**
     * 获得用户在所有组下面的角色列表，id格式是“部门id-角色数字”
     *
     * @param userId
     * @return
     */
    public List<String> loadUserRolesInAllDepartments(String userId) {
        return remoteReference.loadUserRolesInAllDepartments(userId);
    }

    public List<MizarUserDepartment> loadDepartmentByUserId(String userId) {
        return remoteReference.loadDepartmentByUserId(userId);
    }

    public List<Integer> loadUserRoleByUserId(String userId) {
        List<Integer> roles = new ArrayList<>();
        remoteReference.loadDepartmentByUserId(userId).forEach(p -> {
            if (CollectionUtils.isNotEmpty(p.getRoles())) {
                roles.addAll(p.getRoles());
            }
        });
        return roles;
    }

    public MizarUser loadUserByMobile(String mobile){
        return remoteReference.loadUserByMobile(mobile);
    }

    public List<MizarUserDepartment> loadUserByDepartmentId(String departmentId){
        return remoteReference.loadUserByDepartmentId(departmentId);
    }
}
