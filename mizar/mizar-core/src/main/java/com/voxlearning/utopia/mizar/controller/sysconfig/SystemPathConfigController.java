package com.voxlearning.utopia.mizar.controller.sysconfig;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.apache.commons.collections4.map.SingletonMap;
import com.voxlearning.utopia.mizar.controller.AbstractMizarController;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPath;
import com.voxlearning.utopia.service.mizar.api.entity.sys.MizarSysPathRole;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarSystemConfigLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarSystemConfigServiceClient;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Mizar User Role Path config controller
 * Created by alex on 2016/9/18.
 */
@Controller
@RequestMapping("/config/syspath")
public class SystemPathConfigController extends AbstractMizarController {

    @Inject private MizarSystemConfigLoaderClient mizarSystemConfigLoaderClient;
    @Inject private MizarSystemConfigServiceClient mizarSystemConfigServiceClient;

    @RequestMapping(value = "index.vpage", method = {RequestMethod.GET, RequestMethod.POST})
    String sysPathIndex(Model model) {
        String searchFunctionName = getRequestString("searchFunctionName");

        Map<String, String> allGroupMap = mizarUserLoaderClient
                .loadAllDepartments()
                .stream()
                .filter(d -> CollectionUtils.isNotEmpty(d.getOwnRoles()))
                .flatMap(d -> d.getOwnRoles()
                        .stream()
                        .map(role -> new SingletonMap<>(
                                d.getId() + "-" + role,
                                d.getDepartmentName() + "(" + MizarUserRoleType.of(role).getDesc() + ")"
                        )))
                .collect(Collectors.toMap(
                        SingletonMap::getKey,
                        SingletonMap::getValue
                ));

        List<MizarSysPath> pathList = mizarSystemConfigLoaderClient.loadSysPathByName(searchFunctionName)
                .stream()
                .sorted(Comparator.comparing(MizarSysPath::getAppName))
                .collect(Collectors.toList());
        model.addAttribute("allGroupMap", allGroupMap);
        model.addAttribute("allRoleMap", MizarUserRoleType.getAllMizarUserRoles());
        model.addAttribute("sysPathList", splitList(pathList, 10));
        model.addAttribute("searchFunctionName", searchFunctionName);
        return "sysconfig/syspathlist";
    }

    @RequestMapping(value = "addindex.vpage", method = RequestMethod.GET)
    String addSysPathIndex(Model model) {
        String sysPathId = getRequestString("id");
        if (StringUtils.isNoneBlank(sysPathId)) {
            model.addAttribute("isNew", false);
            model.addAttribute("pathId", sysPathId);
            model.addAttribute("sysPathInfo", mizarSystemConfigLoaderClient.loadSysPath(sysPathId));
        } else {
            model.addAttribute("isNew", true);
        }
        model.addAttribute("allRoleMap", MizarUserRoleType.getAllMizarUserRoles());
        return "sysconfig/syspathupd";
    }

    @RequestMapping(value = "add.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage addSysPath() {
        String sysPathId = getRequestString("id");
        // String[] roles = getRequestString("roles").split(",");
        String functionName = getRequestString("functionName");
        String pathName = getRequestString("pathName");
        String desc = getRequestString("desc");
        String roleGroupIdStr = getRequestString("roleGroupIds");

        if (StringUtils.isEmpty(roleGroupIdStr))
            return MapMessage.errorMessage("权限为空，无法保存!");

        //List<Integer> roleList = StringUtils.toIntegerList(StringUtils.join(roles, ","));
        List<String> roleGroupIds = Arrays.asList(roleGroupIdStr.split(","));

        try {
            if (StringUtils.isBlank(sysPathId)) {
                if (mizarSystemConfigLoaderClient.sysPathExist(functionName, pathName)) {
                    return MapMessage.errorMessage("此权限路径已存在!");
                }
                return mizarSystemConfigServiceClient.addSysPath(functionName, pathName, desc, roleGroupIds);
            } else {
                MizarSysPath existPath = mizarSystemConfigLoaderClient.loadSysPath(functionName, pathName);
                if (existPath != null && !Objects.equals(sysPathId, existPath.getId())) {
                    return MapMessage.errorMessage("此权限路径已存在!");
                }
                return mizarSystemConfigServiceClient.updateSysPath(sysPathId, functionName, pathName, desc, roleGroupIds);
            }
        } catch (Exception ex) {
            logger.error("添加/编辑权限失败,functionName:{},pathName:{},desc:{},roles:{},msg:{}",
                    functionName, pathName, desc, roleGroupIds, ex.getMessage(), ex);
            return MapMessage.errorMessage("操作失败! ex:{}", ex.getMessage());
        }
    }

    @RequestMapping(value = "delsyspath.vpage", method = RequestMethod.POST)
    @ResponseBody
    MapMessage delSysPath() {
        String sysPathId = getRequestString("id");
        try {
            return mizarSystemConfigServiceClient.deleteSysPath(sysPathId);
        } catch (Exception ex) {
            logger.error("删除权限失败,id:{}", sysPathId, ex);
            return MapMessage.errorMessage("操作失败! ex:{}", ex.getMessage());
        }
    }

    @RequestMapping(value = "loadroletree.vpage", method = RequestMethod.GET)
    @ResponseBody
    public Object loadRoleTree() {
        // 是否检查用户已经存在的角色，并check
        String pathId = getRequestString("pathId");
        MizarSysPath sysPath = mizarSystemConfigLoaderClient.loadSysPath(pathId);

        List<String> ownRoleGroups = new ArrayList<>();
        if (sysPath != null) {
            ownRoleGroups.addAll(
                    sysPath.getAuthRoleList()
                            .stream()
                            .map(MizarSysPathRole::getRoleGroupId)
                            .collect(Collectors.toList()
                            ));
        }

        return mizarUserLoaderClient.buildRoleTree(ownRoleGroups, getCurrentUser().getRoleList());
    }

}
