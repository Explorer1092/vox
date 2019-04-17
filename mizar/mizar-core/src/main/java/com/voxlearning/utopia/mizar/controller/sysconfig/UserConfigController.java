/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.mizar.controller.sysconfig;

import com.voxlearning.alps.annotation.cache.CacheSystem;
import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MobileRule;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.SingletonMap;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.oa.OfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.shop.MizarShop;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserDepartment;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserOfficialAccounts;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUserSchool;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserSchoolLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarUserSchoolServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.OfficialAccountsServiceClient;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;
import lombok.extern.slf4j.Slf4j;
import org.bson.types.ObjectId;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType.*;

/**
 * Mizar User Config class
 * Created by alex on 2016/9/19.
 */
@Controller
@RequestMapping("/config/user")
@Slf4j
public class UserConfigController extends AbstractMizarController {

    @Inject private OfficialAccountsServiceClient officialAccountsServiceClient;
    @Inject private MizarUserSchoolLoaderClient mizarUserSchoolLoaderClient;
    @Inject private MizarUserSchoolServiceClient mizarUserSchoolServiceClient;
    @Inject private SchoolLoaderClient schoolLoaderClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    String userIndex(Model model) {
        String searchUserName = getRequestString("searchUserName");
        Integer userStatus = getRequestInt("searchUserStatus", 1);
        model.addAttribute("allRoleMap", MizarUserRoleType.getAllMizarUserRoles());

        Map<String, String> allGroupMap = mizarUserLoaderClient
                .loadAllDepartments()
                .stream()
                .filter(d -> !CollectionUtils.isEmpty(d.getOwnRoles()))
                .flatMap(d -> d.getOwnRoles()
                        .stream()
                        .map(role -> {
                            MizarUserRoleType roleType = MizarUserRoleType.of(role);
                            String roleDesc = "";
                            if (roleType != null)
                                roleDesc = roleType.getDesc();

                            return new SingletonMap<>(
                                    d.getId() + "-" + role,
                                    d.getDepartmentName() + "(" + roleDesc + ")"
                            );
                        }))
                .collect(Collectors.toMap(
                        SingletonMap::getKey,
                        SingletonMap::getValue
                ));

        model.addAttribute("allGroupMap", allGroupMap);

        List<MizarUser> users = mizarUserLoaderClient.loadAllUsers(searchUserName, userStatus);
        users.forEach(u -> {
            u.setGroupRoleIds(mizarUserLoaderClient.loadUserRolesInAllDepartments(u.getId()));
            // 将部门角色列表转化成角色列表
            u.setRoleIds(u.getGroupRoleIds()
                    .stream()
                    .map(gr -> Integer.parseInt(gr.split("-")[1]))
                    .distinct()
                    .collect(Collectors.toList()));
        });

        MizarAuthUser curUser = getCurrentUser();

        if (curUser.isAdmin()) {
        } else if (curUser.isOperator()) {
            // 运营人员可以看到机构主和市场人员
            users = users.stream()
                    .filter(u -> u.getRoleIds().contains(MizarAdmin.getId()) ||
                            u.getRoleIds().contains(Operator.getId()))
                    .collect(Collectors.toList());
        } else if (curUser.isBD()) {
            // 市场人员只能看到机构主
            users = users.stream()
                    .filter(user -> user.getRoleIds().contains(ShopOwner.getId()))
                    .collect(Collectors.toList());
        } else if (curUser.isInfantOp()) {
            users = users.stream()
                    .filter(user -> user.getRoleIds().contains(INFANT_SCHOOL.getId()))
                    .collect(Collectors.toList());
        }
        if (curUser.getRoleList() != null && curUser.getRoleList().contains(CREATE_CURRENT_DEPARTMENT_USER.getId())){
            users = new ArrayList<>();
        }

        model.addAttribute("userList", splitList(users, 10));
        model.addAttribute("searchUserName", searchUserName);
        model.addAttribute("searchUserStatus", userStatus);
        return "sysconfig/userlist";
    }

    @RequestMapping(value = "addindex.vpage", method = RequestMethod.GET)
    String addUserIndex(Model model) {
        String userId = getRequestString("id");
        List<School> successSchools = new ArrayList<>();
        if (StringUtils.isNoneBlank(userId)) {
            model.addAttribute("isNew", false);
            MizarUser user = mizarUserLoaderClient.loadUserIncludesDisabled(userId);
            if (user != null) {
                model.addAttribute("userId", userId);
                model.addAttribute("userInfo", user);

                // 判断是否显示公众号信息
                boolean showOA = mizarUserLoaderClient
                        .loadUserDepartments(user.getId())
                        .stream()
                        .filter(d -> !CollectionUtils.isEmpty(d.getOwnRoles()))
                        .flatMap(d -> d.getOwnRoles().stream())
                        .anyMatch(t -> Objects.equals(t, MizarUserRoleType.OfficialAccounts.getId()));
                model.addAttribute("showOA", showOA);

                List<String> userShops = mizarUserLoaderClient.loadUserShopId(userId);
                List<MizarUserOfficialAccounts> userOfficialAccountsList = mizarUserLoaderClient.loadUserOfficialAccounts(userId);
                model.addAttribute("officialAccountsList", userOfficialAccountsList);
                if (CollectionUtils.isNotEmpty(userShops)) {
                    Map<String, MizarShop> shopMap = mizarLoaderClient.loadShopByIds(userShops);
                    model.addAttribute("shopList", shopMap.values());
                }
                List<MizarUserSchool> mizarUserSchools = mizarUserSchoolLoaderClient.loadByUserId(userId);
                if (CollectionUtils.isNotEmpty(mizarUserSchools)) {
                    Set<Long> schoolIds = mizarUserSchools.stream().map(MizarUserSchool::getSchoolId).collect(Collectors.toSet());
                    Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                            .loadSchools(schoolIds)
                            .getUninterruptibly();
                    if (schoolMap != null && !schoolMap.isEmpty()) {
                        successSchools = new ArrayList<>(schoolMap.values());
                    }
                }
            }
        } else {
            model.addAttribute("isNew", true);
        }
        model.addAttribute("successSchools", createSchoolInfo(successSchools));
        Map<String, MizarUserRoleType> roleTypeMap = MizarUserRoleType.getAllMizarUserRoles();
        MizarAuthUser currentUser = getCurrentUser();

        if (StringUtils.isNotBlank(userId)) {
            List<Integer> userRoleTypes = mizarUserLoaderClient.loadUserRoleByUserId(userId);
            model.addAttribute("useRole", userRoleTypes.contains(INFANT_SCHOOL.getId()) || userRoleTypes.contains(TangramJury.getId()));
            model.addAttribute("tangram", userRoleTypes.contains(TangramJury.getId()));
        } else {
            model.addAttribute("useRole", currentUser.isInfantOp() || currentUser.isTangramJury());
        }
        model.addAttribute("allRoleMap", roleTypeMap);
        return "sysconfig/userupd";
    }

    @RequestMapping(value = "add.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage addUser() {
        String userId = getRequestString("id");
        String accountName = getRequestString("accountName");
        String realName = getRequestString("realName");
        String mobile = getRequestString("mobile");
        String comment = getRequestString("userComment");
        String roleGroupIds = getRequestString("roleGroupIds");
        Integer status = getRequestInt("userStatus");
        //Set<Long> schoolSet = requestLongSet("schoolList");

        String schoolsJson = requestString("schoolsJson");
        List<MizarUserSchool> mizarUserSchools = generateMizarUserSchoolFromJson(schoolsJson);

        if (StringUtils.isAnyBlank(accountName, realName, mobile, roleGroupIds)) {
            return MapMessage.errorMessage("请输入必要项");
        }

        if (realName.length() > 10){
            return MapMessage.errorMessage("姓名长度不能超过10位");
        }

        if (accountName.length() > 20){
            return MapMessage.errorMessage("账号长度不能超过20位");
        }

        // 账号不推荐用中文
        if (!accountName.matches("^[0-9A-Za-z_@.]+$")) {
            return MapMessage.errorMessage("账号只能输入字母、数字以及下划线以及@符号");
        }

        if (!MobileRule.isMobile(mobile)) {
            return MapMessage.errorMessage("无效的【手机号码】");
        }

        MapMessage resultMsg;
        String[] groupIds = roleGroupIds.split(",");
        // 通过组的id获得所属的权限列表
        Map<String, List<Integer>> userRoles = Arrays.stream(groupIds)
                .map(id -> {
                    String[] idParts = id.split("-");
                    String departmentId = idParts[0];
                    Integer roleId = Integer.parseInt(idParts[1]);
                    return (SingletonMap<String, Integer>) new SingletonMap(departmentId, roleId);
                })
                .collect(Collectors.groupingBy(
                        SingletonMap::getKey,
                        Collectors.mapping(SingletonMap::getValue, Collectors.toList()
                        )));

        // 将用户在各个部门下面的角色汇总
        Set<Integer> allRoles = userRoles.values()
                .stream()
                .flatMap(Collection::stream)
                .collect(Collectors.toSet());

        if (mizarUserLoaderClient.checkAccountAndMobile(accountName, mobile, userId)) {
            return MapMessage.errorMessage("重复的账号名 or 手机号码");
        }
        try {

            MizarUser user;
            // 默认是新增模式
            if (StringUtils.isBlank(userId)) { // add new user

                user = new MizarUser();
                user.setAccountName(accountName);
                user.setRealName(realName);
                user.setMobile(mobile);
                user.setUserComment(comment);
                if (RuntimeMode.le(Mode.TEST)) {
                    user.setPassword("1");
                } else {
                    user.setPassword("mizar@17zy");
                }

                resultMsg = mizarUserServiceClient.addMizarUser(user);
                if (resultMsg.isSuccess()) {
                    user.setId(resultMsg.get("newId").toString());
                }
            } else {
                user = mizarUserLoaderClient.loadUserIncludesDisabled(userId);
                if (user == null) {
                    return MapMessage.errorMessage("无效的用户信息！");
                }

                // 检查用户的角色和配置的 机构/公众号是否一致
                List<String> userShops = mizarUserLoaderClient.loadUserShopId(user.getId());
                if (CollectionUtils.isNotEmpty(userShops)
                        && (!allRoles.contains(BusinessDevelopment.getId())
                        && !allRoles.contains(ShopOwner.getId()))) {
                    return MapMessage.errorMessage("用户配置了机构但是角色不是机构业主也不是市场人员");
                }

                // 检查公众号角色的配置
                List<MizarUserOfficialAccounts> userOfficials = mizarUserLoaderClient.loadUserOfficialAccounts(user.getId());
                if (CollectionUtils.isNotEmpty(userOfficials) && !allRoles.contains(OfficialAccounts.getId())) {
                    return MapMessage.errorMessage("用户配置了公众号但角色不是公众号");
                }

                user.setAccountName(accountName);
                user.setRealName(realName);
                user.setMobile(mobile);
                user.setUserComment(comment);
                user.setStatus(status);
                //如果用户要操作的状态为关闭，则逻辑删除用户关联的学校
                if (9 == status) {
                    mizarUserSchoolServiceClient.deleteUserSchool(userId);
                }
                resultMsg = mizarUserServiceClient.editMizarUser(user);
            }

            if (!resultMsg.isSuccess()) {
                return resultMsg;
            }

            mizarUserServiceClient.addDepartment(user.getId(), userRoles);

            //用户状态不为删除状态时，做后续添加用户学校操作
            if (9 != status) {
                mizarUserSchools = mizarUserSchools.stream().filter(p -> null != p.getContractEndMonth() && null != p.getContractStartMonth()).collect(Collectors.toList());
                List<Integer> temp = mizarUserLoaderClient.loadUserRoleByUserId(user.getId());
                if (mizarUserSchools.size() > 0 && (temp.contains(INFANT_SCHOOL.getId()) || temp.contains(TangramJury.getId()))) {
                    mizarUserSchools.forEach(p -> p.setUserId(user.getId()));
                    mizarUserSchoolServiceClient.addUserSchools(mizarUserSchools);
                }
            }
            String cacheKey = MizarAuthUser.ck_user(userId);
            CacheSystem.CBS.getCache("unflushable").delete(cacheKey);
            return resultMsg;
        } catch (Exception ex) {
            log.error("保存用户信息失败", ex);
            return MapMessage.errorMessage("保存用户信息失败" + ex.getMessage());
        }
    }

    @RequestMapping(value = "deluser.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delUser() {
        String userId = getRequestString("id");
        try {
            MizarUser user = mizarUserLoaderClient.loadUserIncludesDisabled(userId);
            if (user == null) {
                return MapMessage.errorMessage("无效的用户信息！");
            }
            //删除用户学校关联关系
            mizarUserSchoolServiceClient.deleteUserSchool(userId);
            MapMessage msg = mizarUserServiceClient.closeAccount(userId);
            if (msg.isSuccess()) {
                String cacheKey = MizarAuthUser.ck_user(userId);
                CacheSystem.CBS.getCache("unflushable").delete(cacheKey);
            }
            return msg;
        } catch (Exception ex) {
            log.error("删除用户信息失败,id:{}", userId, ex);
            return MapMessage.errorMessage("操作失败! ex:{}", ex.getMessage());
        }
    }

    /**
     * 添加众号
     */
    @RequestMapping(value = "addofficialaccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage addUserOfficialAccount() {
        String userId = getRequestString("id");
        String officialAccountKeyStr = getRequestString("officialAccountKeys");
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(officialAccountKeyStr)) {
            return MapMessage.errorMessage("请输入公众号标识和用户Id!");
        }

        String[] officialAccountsKeys = officialAccountKeyStr.split(",");
        try {
            MizarUser user = mizarUserLoaderClient.loadUser(userId);
            if (user == null) {
                return MapMessage.errorMessage("无效的用户信息！");
            }
            //用户已有的公众号
            List<MizarUserOfficialAccounts> userOfficialAccountsList = mizarUserLoaderClient.loadUserOfficialAccounts(userId);
            List<String> userOfficialAccountsKeyList = userOfficialAccountsList.stream()
                    .map(MizarUserOfficialAccounts::getAccountsKey)
                    .collect(Collectors.toList());
            List<OfficialAccounts> officialAccountsList = new ArrayList<>();
            List<String> offlineAccounts = new ArrayList<>();
            List<String> existAccounts = new ArrayList<>();
            List<String> errorAccounts = new ArrayList<>();
            for (String accountsKey : officialAccountsKeys) {

                if (StringUtils.isBlank(accountsKey)) {
                    continue;
                }

                OfficialAccounts officialAccounts = officialAccountsServiceClient.loadAccountsByKeyIncludeOffline(accountsKey);
                if (officialAccounts == null) {
                    errorAccounts.add(accountsKey);
                    continue;
                }

                // 下线的和已经存在的不能添加，但需要提示
                if (officialAccounts.getStatus().toString().equals("Offline")) {
                    offlineAccounts.add(accountsKey);
                    continue;
                }

                if (userOfficialAccountsKeyList.contains(accountsKey)) {
                    existAccounts.add(accountsKey);
                    continue;
                }

                officialAccountsList.add(officialAccounts);
            }

            MapMessage resultMessage = MapMessage.successMessage();
            if (CollectionUtils.isNotEmpty(officialAccountsList)) {
                resultMessage = mizarUserServiceClient.saveOfficialAccounts(userId, officialAccountsList);
            }

            if (!resultMessage.isSuccess())
                return resultMessage;
            else
                return resultMessage.add("addList", officialAccountsList)
                        .add("offlineAccounts", offlineAccounts)
                        .add("existAccounts", existAccounts)
                        .add("errorAccounts", errorAccounts);

        } catch (Exception ex) {
            log.error("添加用户公众号信息失败,id:{}", userId, ex);
            return MapMessage.errorMessage("操作失败! ex:{}", ex.getMessage());
        }
    }


    @RequestMapping(value = "addusershop.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage addUserShop() {
        String userId = getRequestString("id");
        String shopIdStr = getRequestString("shopIds");
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(shopIdStr)) {
            return MapMessage.errorMessage("非法操作!");
        }

        String[] shopIds = shopIdStr.split(",");
        try {
            MizarUser user = mizarUserLoaderClient.loadUser(userId);
            if (user == null) {
                return MapMessage.errorMessage("无效的用户信息！");
            }

            List<String> userShops = mizarUserLoaderClient.loadUserShopId(userId);
            List<String> newUserShops = new ArrayList<>();
            for (String shopId : shopIds) {
                if (!userShops.contains(shopId) && ObjectId.isValid(shopId)) {
                    newUserShops.add(shopId);
                }
            }

            MapMessage retMsg = mizarUserServiceClient.saveUserShops(userId, newUserShops);
            // 分配成功之后需要更新缓存里的信息
            if (retMsg.isSuccess()) {
                String cacheKey = MizarAuthUser.ck_user(userId);
                CacheSystem.CBS.getCache("unflushable").delete(cacheKey);
            }
            return retMsg;
        } catch (Exception ex) {
            log.error("添加用户所属机构信息失败,id:{}", userId, ex);
            return MapMessage.errorMessage("操作失败! ex:{}", ex.getMessage());
        }
    }

    @RequestMapping(value = "delusershop.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delUserShop() {
        String userId = getRequestString("id");
        String shopId = getRequestString("shopId");
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(shopId)) {
            return MapMessage.errorMessage("非法操作!");
        }

        try {
            MizarUser user = mizarUserLoaderClient.loadUser(userId);
            if (user == null) {
                return MapMessage.errorMessage("无效的用户信息！");
            }

            List<String> userShops = mizarUserLoaderClient.loadUserShopId(userId);
            if (userShops.contains(shopId)) {
                MapMessage retMsg = mizarUserServiceClient.disableUserShop(userId, shopId);
                // 分配成功之后需要更新缓存里的信息
                if (retMsg.isSuccess()) {
                    String cacheKey = MizarAuthUser.ck_user(userId);
                    CacheSystem.CBS.getCache("unflushable").delete(cacheKey);
                }
                return retMsg;
            } else {
                return MapMessage.successMessage();
            }
        } catch (Exception ex) {
            log.error("删除用户所属机构信息失败,id:{}", userId, ex);
            return MapMessage.errorMessage("操作失败! ex:{}", ex.getMessage());
        }
    }

    /*************删除用户所有的公众号**************/
    @RequestMapping(value = "deluserofficialaccount.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delUserofficialaccount() {
        String userId = getRequestString("id");
        String officialAccountKey = getRequestString("officialAccountKey");
        if (StringUtils.isBlank(userId) || StringUtils.isBlank(officialAccountKey)) {
            return MapMessage.errorMessage("非法操作,请指定公众号标识!");
        }
        try {
            MizarUser user = mizarUserLoaderClient.loadUser(userId);
            if (user == null) {
                return MapMessage.errorMessage("无效的用户信息！");
            }
            List<MizarUserOfficialAccounts> officialAccountsList = mizarUserLoaderClient.loadUserOfficialAccounts(userId);
            List<String> userOfficialAccountsKeysList = officialAccountsList.stream().map(o -> o.getAccountsKey()).collect(Collectors.toList());
            if (userOfficialAccountsKeysList.contains(officialAccountKey)) {
                return mizarUserServiceClient.disableOfficialAccounts(userId, officialAccountKey);
            } else {
                return MapMessage.successMessage("删除用户公众号成功");
            }
        } catch (Exception ex) {
            log.error("删除用户公众号信息失败,id:{}", userId, ex);
            return MapMessage.errorMessage("操作失败! ex:{}", ex.getMessage());
        }
    }

    @RequestMapping(value = "loadroletree.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Object loadRoleTree() {
        // 是否检查用户已经存在的角色，并check
        List<String> ownRoleGroups = new ArrayList<>();
        // 如果是从用户端查找
        String userId = getRequestString("userId");
        List<Integer> userRoleTypes = getCurrentUser().getRoleList();
        if (!StringUtils.isEmpty(userId)) {
            ownRoleGroups = mizarUserLoaderClient.loadUserRolesInAllDepartments(userId);
            userRoleTypes = mizarUserLoaderClient.loadUserRoleByUserId(userId);
        }

        //如果存在创建当前用户下角色
        List<Map<String, Object>> roleTreeList = mizarUserLoaderClient.buildRoleTree(ownRoleGroups, userRoleTypes);

        boolean isCreateCurrentDeparementUser = userRoleTypes.contains(MizarUserRoleType.CREATE_CURRENT_DEPARTMENT_USER.getId());
        if (isCreateCurrentDeparementUser) {
            Set<String> departmentIds = mizarUserLoaderClient
                    .loadDepartmentByUserId(getCurrentUser().getUserId())
                    .stream()
                    .filter(e -> e.containsRole(MizarUserRoleType.CREATE_CURRENT_DEPARTMENT_USER))
                    .map(MizarUserDepartment::getDepartmentId).collect(Collectors.toSet());
            if (CollectionUtils.isNotEmpty(departmentIds)) {
                roleTreeList = roleTreeList.stream().filter(e -> departmentIds.contains(e.get("key").toString())).collect(Collectors.toList());
                roleTreeList.forEach(e -> {
                            List<Map> childrenList = (List<Map>) e.get("children");
                            childrenList = childrenList.stream()
                                    .filter(l -> Objects.equals(MizarUserRoleType.BusinessDevelopment.getRoleName(), l.get("title")) || Objects.equals(Operator.getRoleName(), l.get("title")))
                                    .collect(Collectors.toList());
                            e.put("children", childrenList);
                        }
                );
            }
        }
        return roleTreeList;
    }

    @RequestMapping(value = "finduserschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage findUserSchool() {
        boolean ignore = getRequestBool("ignore");
        Set<Long> schoolIds = requestStringList("schoolIds").stream().map(SafeConverter::toLong).collect(Collectors.toSet());
        return generateMapMessageBySchoolIds(schoolIds, ignore);
    }

    @RequestMapping(value = "deleteuserschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage deleteUserSchool() {
        String userId = requestString("userId");
        Long schoolId = requestLong("schoolId");
        return mizarUserSchoolServiceClient.deleteUserSchool(userId, schoolId);
    }

    @RequestMapping(value = "adduserschool.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage addUserSchool() {
        String userId = requestString("userId");
        String schoolsJson = requestString("schoolsJson");
        List<MizarUserSchool> mizarUserSchoolsTemp = generateMizarUserSchoolFromJson(schoolsJson);
        List<MizarUserSchool> mizarUserSchools = mizarUserSchoolsTemp.stream().filter(p -> null != p.getContractEndMonth() && null != p.getContractStartMonth()).collect(Collectors.toList());
        Set<Long> schoolIds = new HashSet<>();
        mizarUserSchools.forEach(p -> {
            p.setUserId(userId);
            schoolIds.add(p.getSchoolId());
        });
        MapMessage mapMessage = generateMapMessageBySchoolIds(schoolIds, false);
        List<Map<String, Object>> successSchools = (List<Map<String, Object>>) mapMessage.get("successSchools");
        List<MizarUserSchool> canAddList = new ArrayList<>();
        successSchools.forEach(item -> {
            Long tempSchoolId = SafeConverter.toLong(item.get("id"));
            MizarUserSchool temp = mizarUserSchools.stream().filter(p -> tempSchoolId.equals(p.getSchoolId())).findFirst().orElse(null);
            if (null != temp) {
                canAddList.add(temp);
            }
        });
        MapMessage result = mizarUserSchoolServiceClient.addUserSchools(canAddList);
        if (result.containsKey("bindedSchoolIds")) {
            List<Long> bindedSchoolIds = (List<Long>) result.get("bindedSchoolIds");
            Map<Long, String> bindedIdMap = (Map<Long, String>) mapMessage.get("bindedIdMap");
            bindedSchoolIds.forEach(item -> {
                MizarUserSchool mizarUserSchool = mizarUserSchoolLoaderClient.loadBySchoolId(item);
                if (null != mizarUserSchool) {
                    String accountName = "";
                    MizarUser user = mizarUserLoaderClient.loadUser(mizarUserSchool.getUserId());
                    if (null != user) {
                        accountName = user.getAccountName();
                    }
                    bindedIdMap.put(item, accountName);
                }
            });
        }
        mapMessage.remove("successSchools");
        return mapMessage;
    }

    private List<MizarUserSchool> generateMizarUserSchoolFromJson(String schoolsJson) {
        List<MizarUserSchool> tempList = JsonUtils.fromJsonToList(schoolsJson, MizarUserSchool.class);
        return null == tempList ? new ArrayList<>() : tempList;
    }

    private MapMessage generateMapMessageBySchoolIds(Set<Long> schoolIds, boolean ignore) {
        Map<Long, School> schoolMap = schoolLoaderClient.getSchoolLoader()
                .loadSchools(schoolIds)
                .getUninterruptibly();
        List<School> successSchools = new ArrayList<>();
        List<Long> notInfantIds = new ArrayList<>();
        Map<Long, String> bindedIdMap = new HashMap<>();
        List<Long> errorIds = new ArrayList<>();
        if (ignore) {
            successSchools.addAll(schoolMap.values());
        } else {
            schoolIds.forEach(p -> {
                if (schoolMap.containsKey(p)) {
                    School item = schoolMap.get(p);
                    if (null == item.getLevel() || (item.getLevel() != SchoolLevel.INFANT.getLevel() && item.getLevel() != SchoolLevel.JUNIOR.getLevel())) {
                        notInfantIds.add(p);
                    } else {
                        MizarUserSchool mizarUserSchool = mizarUserSchoolLoaderClient.loadBySchoolId(p);
                        if (null != mizarUserSchool) {
                            String userAccountName = "";
                            MizarUser user = mizarUserLoaderClient.loadUser(mizarUserSchool.getUserId());
                            if (null != user) {
                                userAccountName = user.getAccountName();
                            }
                            bindedIdMap.put(p, userAccountName);
                        } else {
                            successSchools.add(schoolMap.get(p));
                        }
                    }
                } else {
                    errorIds.add(p);
                }
            });
        }
        MapMessage mapMessage = MapMessage.successMessage();
        mapMessage.set("successSchools", createSchoolInfo(successSchools));
        mapMessage.set("notInfantIds", notInfantIds);
        mapMessage.set("bindedIdMap", bindedIdMap);
        mapMessage.set("errorIds", errorIds);
        return mapMessage;
    }

    private List<Map<String, Object>> createSchoolInfo(List<School> schools) {
        if (CollectionUtils.isEmpty(schools)) {
            return Collections.emptyList();
        }
        List<Map<String, Object>> result = new ArrayList<>();

        Integer currentYearMonth = Integer.valueOf(DateUtils.nowToString("yyyyMM"));
        schools.forEach(p -> {
            MizarUserSchool mizarUserSchool = mizarUserSchoolLoaderClient.loadBySchoolId(p.getId());
            Map<String, Object> schoolInfo = new HashMap<>();
            schoolInfo.put("id", p.getId());
            schoolInfo.put("cname", p.loadSchoolFullName());
            schoolInfo.put("level", SchoolLevel.safeParse(p.getLevel()).getDescription());
            if (null != mizarUserSchool) {
                if (null != mizarUserSchool.getContractStartMonth()) {
                    String startStr = String.valueOf(mizarUserSchool.getContractStartMonth());
                    if (startStr.length() == 6) {
                        StringBuilder sbStart = new StringBuilder(startStr);
                        sbStart.insert(4, '-');
                        schoolInfo.put("contractStartMonth", sbStart.toString());
                    }
                }
                if (null != mizarUserSchool.getContractEndMonth()) {
                    String endStr = String.valueOf(mizarUserSchool.getContractEndMonth());
                    if (endStr.length() == 6) {
                        StringBuilder sbEnd = new StringBuilder(endStr);
                        sbEnd.insert(4, '-');
                        schoolInfo.put("contractEndMonth", sbEnd.toString());
                    }
                    if (mizarUserSchool.getContractEndMonth() < currentYearMonth) {
                        schoolInfo.put("expired", true);
                    }
                }
            }
            result.add(schoolInfo);
        });
        return result;
    }
}
