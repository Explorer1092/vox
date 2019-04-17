package com.voxlearning.utopia.agent.controller.permission;

import com.voxlearning.alps.annotation.common.Mode;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.runtime.RuntimeMode;
import com.voxlearning.utopia.agent.annotation.OperationCode;
import com.voxlearning.utopia.agent.controller.AbstractAgentController;
import com.voxlearning.utopia.agent.service.permission.SystemRolePermissionService;
import com.voxlearning.utopia.agent.view.permission.SystemOperationView;
import com.voxlearning.utopia.agent.view.permission.SystemPageElementView;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentRoleType;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * SystemRolePermissionController
 *
 * @author song.wang
 * @date 2018/5/17
 */
@Controller
@RequestMapping("/system/permission")
public class SystemRolePermissionController extends AbstractAgentController {

    @Inject private SystemRolePermissionService systemRolePermissionService;

    @RequestMapping(value = "role_list.vpage", name = "角色列表")
    @OperationCode("024930ae72c448b2")
    public String roleList(Model model){
        List<AgentRoleType> roleTypeList = AgentRoleType.getValidRoleList();
        model.addAttribute("roleList", roleTypeList);
        return "/system/permission/role_list";
    }

    @RequestMapping(value = "delete_page_element.vpage", name = "删除页面元素")
    @ResponseBody
    public MapMessage deletePageElement(){

        Long elementId = getRequestLong("elementId");
        if(elementId < 1){
            return MapMessage.errorMessage("请输入有效的元素ID");
        }
        return systemRolePermissionService.deletePageElement(elementId);
    }

    @RequestMapping(value = "role_elements_config.vpage", name = "给指定角色配置页面元素权限页面")
    public String rolePageElementConfig(Model model){
        Integer roleId = requestInteger("roleId");
        AgentRoleType roleType = AgentRoleType.of(roleId);
        if(roleType != null){
            List<SystemPageElementView> viewList = systemRolePermissionService.getPageElementsForRole(roleId);
            model.addAttribute("pageElementList", viewList);
            model.addAttribute("role", roleType);
        }

        return "/system/permission/role_elements_config";
    }


    @RequestMapping(value = "save_role_elements.vpage", name = "保存指定角色的页面元素权限")
    @ResponseBody
    public MapMessage saveRolePageElements(){
        Integer roleId = requestInteger("roleId");
        AgentRoleType roleType = AgentRoleType.of(roleId);
        if(roleType == null){
            return MapMessage.errorMessage("角色有误！");
        }

        Set<Long> pageElementIds = requestLongSet("pageElementIds");
        return systemRolePermissionService.setPageElementsForRole(roleId, pageElementIds);
    }

    //页面元素配置
    @RequestMapping(value = "elements_roles_list.vpage", name = "页面元素列表")
    public String pageElementsRolesList(Model model){
        List<SystemPageElementView> viewList = systemRolePermissionService.getPageElementsWithRoles();
        model.addAttribute("pageElementList", viewList);
        return "/system/permission/elements_roles_list";
    }
//    编辑
    @RequestMapping(value = "element_roles_config.vpage", name = "将指定元素配置给多个角色页面")
    public String pageElementRolesConfig(Model model){
        Long elementId = getRequestLong("elementId");
        if(elementId > 0){
            SystemPageElementView view = systemRolePermissionService.getPageElementByElementId(elementId);
            model.addAttribute("pageElement", view);
        }
        List<AgentRoleType> roleTypeList = AgentRoleType.getValidRoleList();
        model.addAttribute("roleList", roleTypeList);
        // 新建页面元素时，测试环境可以手动编辑元素code，可和线上保持一致， 线上环境禁止手动编辑code
        model.addAttribute("codeEditable", RuntimeMode.current().lt(Mode.STAGING));
        return "/system/permission/element_roles_config";
    }

    @RequestMapping(value = "save_element_roles.vpage", name = "保存页面元素给多个角色")
    @ResponseBody
    public MapMessage savePageElementToRoles(){
        Long elementId = getRequestLong("elementId");

        String module = requestString("module");
        String subModule = requestString("subModule");
        String elementCode = requestString("elementCode");
        String pageName = requestString("pageName");
        if(StringUtils.isBlank(module)){
            return MapMessage.errorMessage("模块不能为空！");
        }
        if(StringUtils.isBlank(subModule)){
            return MapMessage.errorMessage("子模块名称不能为空！");
        }
        if(StringUtils.isBlank(pageName)){
            return MapMessage.errorMessage("页面名称不能为空！");
        }
        String elementName = requestString("elementName");
        if(StringUtils.isBlank(elementName)){
            return MapMessage.errorMessage("元素名称不能为空！");
        }
        String comment = getRequestString("comment");
        MapMessage message = systemRolePermissionService.editPageElement(elementId, module, subModule, pageName, elementCode, elementName, comment);
        if(!message.isSuccess()){
            return message;
        }
        elementId = SafeConverter.toLong(message.get("elementId"));

        Set<Integer> roleIds = requestIntegerSet("roleIds");
        List<AgentRoleType> roleTypeList = new ArrayList<>();
        roleIds.forEach(p -> roleTypeList.add(AgentRoleType.of(p)));
        return systemRolePermissionService.setPageElementForRoles(elementId, roleTypeList);
    }

    @RequestMapping(value = "generate_uuid.vpage", name = "生成页面元素编码")
    @ResponseBody
    public MapMessage generateElementCode(){
        String uuid = UUID.randomUUID().toString().replaceAll("-", "");
        return MapMessage.successMessage().add("code", uuid.substring(0, 16));
    }

    ////////////////////////////////////////   操作权限配置

    // 给指定角色配置操作权限页面
    @RequestMapping(value = "role_operations_config.vpage")
    public String roleOperationsConfig(Model model){
        Integer roleId = requestInteger("roleId");
        AgentRoleType roleType = AgentRoleType.of(roleId);
        if(roleType != null){
            List<SystemOperationView> viewList = systemRolePermissionService.getOperationsForRole(roleId);
            model.addAttribute("dataList", viewList);
            model.addAttribute("role", roleType);
        }
        return "/system/permission/role_operations_config";
    }

    // 保存角色的操作权限
    @RequestMapping(value = "save_role_operations.vpage")
    @ResponseBody
    public MapMessage saveRoleOperations(){
        Integer roleId = requestInteger("roleId");
        AgentRoleType roleType = AgentRoleType.of(roleId);
        if(roleType == null){
            return MapMessage.errorMessage("角色有误！");
        }

        Set<Long> operationIds = requestLongSet("operationIds");
        return systemRolePermissionService.setOperationsForRole(roleId, operationIds);
    }


    // 操作权限列表
    @RequestMapping(value = "operations_roles_list.vpage")
    public String operationsRolesList(Model model){
        List<SystemOperationView> viewList = systemRolePermissionService.getOperationsWithRoles();
        model.addAttribute("dataList", viewList);
        return "/system/permission/operations_roles_list";
    }

    @RequestMapping(value = "operation_roles_config.vpage")
    public String operationRolesConfig(Model model){
        Long operationId = getRequestLong("operationId");
        if(operationId > 0){
            SystemOperationView view = systemRolePermissionService.getOperationById(operationId);
            model.addAttribute("operation", view);
        }
        List<AgentRoleType> roleTypeList = AgentRoleType.getValidRoleList();
        model.addAttribute("roleList", roleTypeList);
        // 新建页面元素时，测试环境可以手动编辑元素code，可和线上保持一致， 线上环境禁止手动编辑code
        model.addAttribute("codeEditable", RuntimeMode.current().lt(Mode.STAGING));
        return "/system/permission/operation_roles_config";
    }


    @RequestMapping(value = "save_operation_roles.vpage")
    @ResponseBody
    public MapMessage saveOperationToRoles(){
        Long operationId = getRequestLong("operationId");

        String operationCode = requestString("operationCode");
        String module = requestString("module");
        String subModule = requestString("subModule");
        String operationName = requestString("operationName");
        if(StringUtils.isBlank(module)){
            return MapMessage.errorMessage("模块不能为空！");
        }
        if(StringUtils.isBlank(subModule)){
            return MapMessage.errorMessage("子模块名称不能为空！");
        }
        if(StringUtils.isBlank(operationName)){
            return MapMessage.errorMessage("操作名称不能为空！");
        }
        String comment = getRequestString("comment");
        MapMessage message = systemRolePermissionService.editOperation(operationId, module, subModule, operationCode, operationName, comment);
        if(!message.isSuccess()){
            return message;
        }
        operationId = SafeConverter.toLong(message.get("operationId"));

        Set<Integer> roleIds = requestIntegerSet("roleIds");
        List<AgentRoleType> roleTypeList = new ArrayList<>();
        roleIds.forEach(p -> roleTypeList.add(AgentRoleType.of(p)));
        return systemRolePermissionService.setOperationForRoles(operationId, roleTypeList);
    }



}
