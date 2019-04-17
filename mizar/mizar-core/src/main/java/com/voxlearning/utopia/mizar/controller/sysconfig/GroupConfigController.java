package com.voxlearning.utopia.mizar.controller.sysconfig;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.mizar.auth.MizarAuthUser;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarDepartment;
import com.voxlearning.utopia.service.mizar.api.entity.user.MizarUser;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarUserServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.Arrays;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 用户组 Controller
 * Created by haitian.gan on 2016/11/30.
 */
@Controller
@RequestMapping("/config/group")
public class GroupConfigController extends AbstractMizarController {

    @Inject private MizarUserLoaderClient mizarUserLoaderClient;
    @Inject private MizarUserServiceClient mizarUserServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    public String index(Model model) {
        String searchGroupName = getRequestString("searchGroupName");
        int disabledStatus = getRequestInt("searchStatus", -1);

        model.addAttribute("groupList", splitList(mizarUserLoaderClient.loadAllDepartments(
                searchGroupName, disabledStatus), 10));
        model.addAttribute("searchGroupName", searchGroupName);
        model.addAttribute("disabledStatus", disabledStatus);

        // 装入角色列表
        Map<String, String> roleMap = Arrays.stream(MizarUserRoleType.values())
                .collect(Collectors.toMap(role -> role.getId().toString(), MizarUserRoleType::getDesc));
        model.addAttribute("roleMap", roleMap);

        return "sysconfig/groupconfig";
    }

    @RequestMapping(value = "update.vpage", method = RequestMethod.GET)
    public String updateGroup(Model model) {
        String groupId = getRequestString("id");
        MizarDepartment department;
        if (StringUtils.isNotEmpty(groupId)) {
            model.addAttribute("isNew", false);
            model.addAttribute("groupId", groupId);

            department = mizarUserLoaderClient.loadDepartment(groupId);
            model.addAttribute("groupInfo", department);
        } else
            model.addAttribute("isNew", true);

        Map<String, MizarUserRoleType> roleTypeMap = MizarUserRoleType.getAllMizarUserRoles();
        MizarAuthUser currentUser = getCurrentUser();
        /*if (!currentUser.isAdmin()) {
            //如果当前登录者不是管理角色,则用户角色编辑页面看不到管理其角色色
            roleTypeMap.remove(SafeConverter.toString(MizarAdmin.getId()));
        }*/
        model.addAttribute("allRoleMap", roleTypeMap);

        List<MizarUser> users = mizarUserLoaderClient.loadDepartmentUsers(groupId);
        model.addAttribute("users", users);
        model.addAttribute("currentUser", currentUser);

        return "sysconfig/groupedit";
    }

    @RequestMapping(value = "updategroup.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> updateGroup() {

        String groupId = getRequestString("id");
        String groupName = getRequestString("name");
        String description = getRequestString("description");
        String roleStr = getRequestString("roles");

        if (StringUtils.isEmpty(groupName))
            return MapMessage.errorMessage("组名不能为空!");

        if (mizarUserLoaderClient.checkDepartmentName(groupName, groupId))
            return MapMessage.errorMessage("已存在相同的组名!");

        if (StringUtils.isEmpty(roleStr))
            return MapMessage.errorMessage("角色不能为空!");

        try {
            MizarDepartment department;
            if (StringUtils.isNotEmpty(groupId))
                department = mizarUserLoaderClient.loadDepartment(groupId);
            else {
                department = new MizarDepartment();
            }

            List<Integer> ownRoles = Arrays.stream(roleStr.split(","))
                    .map(Integer::parseInt)
                    .collect(Collectors.toList());

            department.setDescription(description);
            department.setDepartmentName(groupName);
            // department.setRole(SafeConverter.toInt(roleStr));
            department.setOwnRoles(ownRoles);
            department.setDisabled(false);

            return mizarUserServiceClient.editMizarDepartment(department);
        } catch (Exception e) {
            logger.error("保存组信息失败", e);
            return MapMessage.errorMessage("保存组信息失败" + e.getMessage());
        }
    }

    /**
     * 从组中移除用户
     */
    @RequestMapping(value = "removeuser.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> removeUserFromGroup() {
        String groupId = getRequestString("groupId");
        String userId = getRequestString("userId");

        MapMessage resultMsg = mizarUserServiceClient.removeDepartmentUser(
                groupId, userId);

        if (resultMsg.isSuccess()) {
            List<MizarUser> usersLeft = mizarUserLoaderClient.loadDepartmentUsers(groupId);
            resultMsg.add("users", usersLeft);
        }

        return resultMsg;
    }

    @RequestMapping(value = "close.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Map<String, Object> closeGroup() {
        String groupId = getRequestString("groupId");
        return mizarUserServiceClient.closeDepartment(groupId);
    }

}
