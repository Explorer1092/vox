package com.voxlearning.utopia.admin.controller.management;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.page.PageableUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageRequest;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.admin.data.AdminPathExtend;
import com.voxlearning.utopia.admin.persist.entity.*;
import com.voxlearning.utopia.service.crm.client.AdminAppSystemServiceClient;
import com.voxlearning.utopia.service.crm.client.AdminRoleServiceClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import org.springframework.beans.factory.config.BeanDefinition;
import org.springframework.context.annotation.ClassPathScanningCandidateComponentProvider;
import org.springframework.core.type.filter.AnnotationTypeFilter;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.lang.reflect.Method;
import java.sql.Timestamp;
import java.util.*;
import java.util.stream.Collectors;

/**
 * Created with IntelliJ IDEA.
 * User: QJ
 * Date: 13-7-5
 * Time: 下午5:14
 * To change this template use File | Settings | File Templates.
 */
@Controller
@RequestMapping("/management/path")
public class ManagementPathController extends ManagementAbstractController {

    @Inject
    private AdminAppSystemServiceClient adminAppSystemServiceClient;
    @Inject
    private AdminRoleServiceClient adminRoleServiceClient;

    @RequestMapping(value = "path_list.vpage", method = RequestMethod.GET)
    public String path_list(Model model) {
        String appName = getRequestParameter("appName", "").replaceAll("\\s", "");
        String pathName = getRequestParameter("pathName", "").replaceAll("\\s", "");
        String pathDescription = getRequestParameter("pathDescription", "").replaceAll("\\s", "");
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> appListForRead = managementService.getAppList(adminName, "read");
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        List<String> appListForDelete = managementService.getAppList(adminName, "delete");
        if (appListForRead.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        List<AdminAppSystem> selectAppList = adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAllAdminAppSystems()
                .getUninterruptibly()
                .stream()
                .filter(e -> appListForRead.contains(e.getAppName()))
                .collect(Collectors.toList());
//        def groupList;
        List<AdminPath> pathList;
        String pathWhere = "WHERE 1=1";
        Map<String, Object> pathParams = new HashMap<>();
        if(!appName.equals("")){
            pathWhere += " AND APP_NAME IN (:a)";
            pathParams.put("a", appName);
        }
        if(StringUtils.isNotBlank(pathName)){
            pathWhere += " AND PATH_NAME=:b";
            pathParams.put("b", pathName);
        }
        if(StringUtils.isNotBlank(pathDescription)){
            pathWhere += " AND PATH_DESCRIPTION LIKE :c";
            pathParams.put("c", "%" + pathDescription + "%");
        }
        pathList = adminPathPersistence.withSelectFromTable(pathWhere).useParams(pathParams).queryAll();

//        if (!pathName.equals("")) {
//            pathWhere = "WHERE APP_NAME IN (:a) AND PATH_NAME=:b";
//            pathParams.put("a", appListForRead);
//            pathParams.put("b", pathName);
//            if(!pathDescription.equals("")){
//                pathWhere = "WHERE APP_NAME IN (:a) AND PATH_NAME=:b AND PATH_DESCRIPTION LIKE '%:c%'";
//                pathParams.put("c", pathDescription);
//            }
//        } else {
//            pathWhere = "WHERE APP_NAME IN (:a)";
//            pathParams.put("a", appListForRead);
//            if(!pathDescription.equals("")){
//                pathWhere = "WHERE APP_NAME IN (:a) AND PATH_DESCRIPTION LIKE '%:c%'";
//                pathParams.put("c", pathDescription);
//            }
//        }
//        if (appName.equals("")) {
//            pathList = adminPathPersistence.withSelectFromTable(pathWhere).useParams(pathParams).queryAll();
//        } else {
//            if (appListForRead.contains(appName)) {
//                if (pathName.equals("")) {
//                    pathList = adminPathPersistence.withSelectFromTable("WHERE APP_NAME=?").useParamsArgs(appName).queryAll();
//                } else {
//                    pathList = adminPathPersistence.withSelectFromTable("WHERE APP_NAME=? AND PATH_NAME=?").useParamsArgs(appName, pathName).queryAll();
//                }
//            } else {
//                getAlertMessageManager().addMessageError("您缺少权限查看此权限组");
//                pathList = adminPathPersistence.withSelectFromTable(pathWhere).useParams(pathParams).queryAll();
//            }
//        }
        // 分页处理下
        int page = getRequestInt("page", 1);
        if (page <= 0) page = 1;
        Pageable pageable = new PageRequest(page - 1, 50);
        Page<AdminPath> productPage = PageableUtils.listToPage(pathList, pageable);
        model.addAttribute("pathPage", productPage);
        model.addAttribute("currentPage", productPage.getTotalPages() < page ? 1 : page);
        model.addAttribute("totalPage", productPage.getTotalPages());
        model.addAttribute("hasPrev", productPage.hasPrevious());
        model.addAttribute("hasNext", productPage.hasNext());


        model.addAttribute("pathName", pathName);
        model.addAttribute("appName", appName);
        model.addAttribute("pathDescription", pathDescription);
        model.addAttribute("selectAppList", selectAppList);
        model.addAttribute("pathRoleMap", managementService.getPathRoles());
        model.addAttribute("roleList", managementService.getRoleNames());
        model.addAttribute("appNames", managementService.getAppNames());
        model.addAttribute("appListForWrite", appListForWrite.toString());
        model.addAttribute("appListForDelete", appListForDelete.toString());
        model.addAttribute("pageMessage", "页面路径");
        return "management/path/path_list";
    }

    @RequestMapping(value = "path_new_unsigned.vpage", method = RequestMethod.GET)
    public String path_new_unsigned(Model model) {

        String reqAppName = getRequestParameter("appName", "").replaceAll("\\s", "");
        String reqPathName = getRequestParameter("pathName", "").replaceAll("\\s", "");
        List<AdminPathExtend> allPathFromCodeList = scanningAllAdminPath();
        List<AdminPath> allOldPathList = adminPathPersistence.withAllFromTable().queryAll();
        allPathFromCodeList = allPathFromCodeList.stream().filter(p -> {
            for (AdminPath adminPath : allOldPathList) {
                if (Objects.equals(adminPath.getAppName(), p.getAppName()) && Objects.equals(adminPath.getPathName(), p.getPathName())) {
                    return false;
                }
            }
            if (!reqAppName.equals("") || !reqPathName.equals("")) {
                if (!reqAppName.equals("") && !reqPathName.equals("")) {
                    if (!Objects.equals(reqAppName, p.getAppName()) || !p.getPathName().contains(reqPathName)) {
                        return false;
                    }
                } else if (!reqAppName.equals("")) {
                    if (!Objects.equals(reqAppName, p.getAppName())) {
                        return false;
                    }
                } else if (!reqPathName.equals("")) {
                    if (!p.getPathName().contains(reqPathName)) {
                        return false;
                    }
                }
            }

            return true;
        }).collect(Collectors.toList());

        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> appListForRead = managementService.getAppList(adminName, "read");
        List<AdminAppSystem> selectAppList = adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAllAdminAppSystems()
                .getUninterruptibly()
                .stream()
                .filter(e -> appListForRead.contains(e.getAppName()))
                .collect(Collectors.toList());
        model.addAttribute("pathList", allPathFromCodeList);
        model.addAttribute("selectAppList", selectAppList);
        model.addAttribute("pathName", reqPathName);
        model.addAttribute("appName", reqAppName);
        model.addAttribute("appNames", managementService.getAppNames());
        return "management/path/path_new_unsigned";
    }

    @RequestMapping(value = "path_new.vpage", method = RequestMethod.GET)
    public String path_new(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        if (appListForWrite.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("appList", adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAllAdminAppSystems().getUninterruptibly());
        model.addAttribute("pageMessage", "添加路径");
        model.addAttribute("appName", "");
        model.addAttribute("includeUrl", "path/path_form.ftl");
        return "management/form";
    }

    @RequestMapping(value = "path_new.vpage", method = RequestMethod.POST)
    public String path_new_post(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        if (appListForWrite.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        String pathName = getRequestParameter("pathName", "").replaceAll("\\s", "");
        String pathDescription = getRequestParameter("pathDescription", "").replaceAll("\\s", "");
        String appName = getRequestParameter("appName", "").replaceAll("\\s", "");
        if (pathName.equals("")) {
            getAlertMessageManager().addMessageError("路径标识不能为空");
        }
        if (!pathName.matches("^[a-zA-Z0-9-_./]+")) {
            getAlertMessageManager().addMessageError("唯一标识ID只能由字母 数字 / - _ .组成");
        }
        if (appName.length() > 80) {
            getAlertMessageManager().addMessageError("路径标识过长");
        }
        if (pathDescription.length() > 255) {
            getAlertMessageManager().addMessageError("路径中文名称过长");
        }
        if (appName.equals("")) {
            getAlertMessageManager().addMessageError("业务系统不能为空");
        }
        if (adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAdminAppSystem(appName)
                .getUninterruptibly() == null) {
            getAlertMessageManager().addMessageError("所选业务系统不存在");
        }
        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "path/path_form.ftl");
            model.addAttribute("pageMessage", "添加路径");
            model.addAttribute("pathName", pathName);
            model.addAttribute("pathDescription", pathDescription);
            model.addAttribute("appName", appName);
            model.addAttribute("appList", adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAllAdminAppSystems().getUninterruptibly());
            return "management/form";
        }
        AdminPath pathInfo = adminPathPersistence.withSelectFromTable("WHERE PATH_NAME=? AND APP_NAME=? ").useParamsArgs(pathName, appName).queryObject();
        if (pathInfo != null) {
            getAlertMessageManager().addMessageError("已存在相同路径标识");
            model.addAttribute("includeUrl", "path/path_form.ftl");
            model.addAttribute("pathName", pathName);
            model.addAttribute("pathDescription", pathDescription);
            model.addAttribute("pageMessage", "添加路径");
            model.addAttribute("appName", appName);
            model.addAttribute("appList", adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAllAdminAppSystems().getUninterruptibly());
            return "management/form";
        } else {
            //新建路径
            AdminPath newPath = new AdminPath();
            newPath.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            newPath.setPathName(pathName);
            newPath.setPathDescription(pathDescription);
            newPath.setAppName(appName);
            adminPathPersistence.persist(newPath);
            addAdminLog("addNewPath", pathName, appName, newPath);
            getAlertMessageManager().addMessageSuccess("添加新路径" + pathDescription + "成功");
            return redirect("/management/path/path_list.vpage");
        }
    }

    @RequestMapping(value = "path_role_add.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage path_role_add(){
        String pathName = getRequestParameter("pathName", "").replaceAll("\\s", "");
        String pathDescription = getRequestParameter("pathDescription", "").replaceAll("\\s", "");
        String appName = getRequestParameter("appName", "").replaceAll("\\s", "");
        if (pathName.equals("")) {
            return MapMessage.errorMessage("路径标识不能为空");
        }
        if (!pathName.matches("^[a-zA-Z0-9-_./]+")) {
            return MapMessage.errorMessage("唯一标识ID只能由字母 数字 / - _ .组成");
        }
        if (appName.length() > 80) {
            return MapMessage.errorMessage("路径标识过长");
        }
        if (pathDescription.length() > 255) {
            return MapMessage.errorMessage("路径中文名称过长");
        }
        if (appName.equals("")) {
            return MapMessage.errorMessage("业务系统不能为空");
        }
        if (adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAdminAppSystem(appName)
                .getUninterruptibly() == null) {
            return MapMessage.errorMessage("所选业务系统不存在");
        }
        //新建路径
        AdminPath newPath = new AdminPath();
        newPath.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
        newPath.setPathName(pathName);
        newPath.setPathDescription(pathDescription);
        newPath.setAppName(appName);
        Long id = adminPathPersistence.persist(newPath);
        addAdminLog("addNewPath", pathName, appName, newPath);
        return MapMessage.successMessage().add("id",id);
    }

    @RequestMapping(value = "path_new_batch.vpage", method = RequestMethod.GET)
    public String path_new_batch(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        if (appListForWrite.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("appList", adminAppSystemServiceClient.getAdminAppSystemService()
                .loadAllAdminAppSystems().getUninterruptibly());
        model.addAttribute("pageMessage", "批量添加路径");
        model.addAttribute("appName", "");
        model.addAttribute("includeUrl", "path/path_form_batch.ftl");
        return "management/form";
    }

    @RequestMapping(value = "path_new_batch.vpage", method = RequestMethod.POST)
    public String path_new_batch_post(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        if (appListForWrite.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }

        String content = getRequestString("content");
        if (StringUtils.isBlank(content)) {
            getAlertMessageManager().addMessageError("请输入内容");
        }

        List<AdminPath> pathList = new ArrayList<>();

        String[] records = content.split("\\n");
        for (String record : records) {
            String[] cols = record.split("\\t");
            if (cols.length != 3) {
                getAlertMessageManager().addMessageError("输入内容不合法");
                break;
            }

            String appName = StringUtils.deleteWhitespace(cols[0]);
            String pathName = StringUtils.deleteWhitespace(cols[1]);
            String pathDesc = StringUtils.deleteWhitespace(cols[2]);

            if (StringUtils.isBlank(appName) || StringUtils.isBlank(pathName) || StringUtils.isBlank(pathDesc)) {
                getAlertMessageManager().addMessageError("输入内容[{}]不合法", record);
                break;
            }

            if (!pathName.matches("^[a-zA-Z0-9-_./]+")) {
                getAlertMessageManager().addMessageError("唯一标识ID只能由字母 数字 / - _ .组成");
                break;
            }
            if (appName.length() > 80) {
                getAlertMessageManager().addMessageError("路径标识过长");
                break;
            }
            if (pathDesc.length() > 255) {
                getAlertMessageManager().addMessageError("路径中文名称过长");
                break;
            }

            if (adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAdminAppSystem(appName)
                    .getUninterruptibly() == null) {
                getAlertMessageManager().addMessageError("所选业务系统不存在");
                break;
            }

            AdminPath pathInfo = adminPathPersistence.withSelectFromTable("WHERE PATH_NAME=? AND APP_NAME=? ").useParamsArgs(pathName, appName).queryObject();
            if (pathInfo != null) {
                getAlertMessageManager().addMessageError("输入的权限路径{}已经存在了", record);
                break;
            }

            AdminPath newPath = new AdminPath();
            newPath.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            newPath.setPathName(pathName);
            newPath.setPathDescription(pathDesc);
            newPath.setAppName(appName);

            pathList.add(newPath);
        }


        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "path/path_form_batch.ftl");
            model.addAttribute("pageMessage", "批量添加路径");
            model.addAttribute("content", content);
            model.addAttribute("appList", adminAppSystemServiceClient.getAdminAppSystemService()
                    .loadAllAdminAppSystems().getUninterruptibly());
            return "management/form";
        }

        for (AdminPath newPath : pathList) {
            adminPathPersistence.persist(newPath);
            addAdminLog("addNewPath", newPath.getPathName(), newPath.getAppName(), newPath);
        }

        getAlertMessageManager().addMessageSuccess("批量添加新路径成功");
        return redirect("/management/path/path_list.vpage");
    }

    @RequestMapping(value = "role_new.vpage", method = RequestMethod.GET)
    public String role_new(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        if (appListForWrite.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        model.addAttribute("adminName", "adminName");
        model.addAttribute("pageMessage", "新建角色");
        model.addAttribute("includeUrl", "path/role_form.ftl");
        return "management/form";
    }

    @RequestMapping(value = "role_new.vpage", method = RequestMethod.POST)
    public String role_new_post(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        if (appListForWrite.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        String roleName = getRequestParameter("roleName", "").replaceAll("\\s", "");
        String roleDescription = getRequestParameter("roleDescription", "").replaceAll("\\s", "");
        if (roleName.equals("")) {
            getAlertMessageManager().addMessageError("角色标识不能为空");
        }
        if (!roleName.matches("^[a-zA-Z]+")) {
            getAlertMessageManager().addMessageError("角色只能由英文组成");
        }
        if (roleName.length() > 80) {
            getAlertMessageManager().addMessageError("角色标识符过长");
        }
        if (roleDescription.length() > 255) {
            getAlertMessageManager().addMessageError("角色描述中文名称过长");
        }
        if (getAlertMessageManager().hasMessageError()) {
            model.addAttribute("includeUrl", "group/group_form.ftl");
            model.addAttribute("pageMessage", "添加角色");
            model.addAttribute("roleName", roleName);
            model.addAttribute("roleDescription", roleDescription);
            return "management/form";
        }
        boolean existRole = adminRoleServiceClient.getAdminRoleService().loadAdminRole(roleName).getUninterruptibly() != null;
        if (existRole) {
            getAlertMessageManager().addMessageError("已存在相同角色标识");
            model.addAttribute("includeUrl", "role/role_form.ftl");
            model.addAttribute("roleName", roleName);
            model.addAttribute("roleDescription", roleDescription);
            model.addAttribute("pageMessage", "添加角色");
            return "management/form";
        } else {
            //新建角色
            AdminRole newRole = new AdminRole();
            newRole.setCreateDatetime(new Timestamp(System.currentTimeMillis()));
            newRole.setName(roleName);
            newRole.setDescription(roleDescription);
            newRole = adminRoleServiceClient.getAdminRoleService().insertAdminRole(newRole).getUninterruptibly();
            addAdminLog("addNewRole", roleName, "", newRole);
            getAlertMessageManager().addMessageSuccess("添加新权限组" + roleDescription + "成功");
            return redirect("/management/path/path_list.vpage");
        }
    }

    @RequestMapping(value = "path_role.vpage", method = RequestMethod.GET)
    public String path_role(Model model) {
        long pathId = Long.parseLong(getRequestParameter("pathId", "0"));
        String adminName = getCurrentAdminUser().getAdminUserName();
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        AdminPath pathInfo = adminPathPersistence.loadFromDatabase(pathId);
        if (!appListForWrite.contains(pathInfo.getAppName())) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("roleList", adminRoleServiceClient.getAdminRoleService().loadAllAdminRoles().getUninterruptibly());
        model.addAttribute("pathRoles", adminPathRolePersistence.withSelectFromTable("ROLE_NAME", "WHERE PATH_ID=? AND DISABLED=0")
                .useParamsArgs(pathId).queryColumnValues().toString());
        model.addAttribute("pathInfo", pathInfo);
        model.addAttribute("pageMessage", "路径关联角色");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/path/path_role";
    }

    @RequestMapping(value = "path_role_edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Object path_role_edit() {
        String doType = getRequestParameter("do", "").replaceAll("\\s", "");
        long pathId = Long.parseLong(getRequestParameter("pathId", "0"));
        String roleName = getRequestParameter("roleName", "").replaceAll("\\s", "");

        if (pathId != 0 && !roleName.equals("")) {
            AdminPathRole pathRoleInfo = adminPathRolePersistence.withSelectFromTable("WHERE PATH_ID=? AND ROLE_NAME=?").useParamsArgs(pathId, roleName).queryObject();
            AdminPathRole newPathRoleInfo = new AdminPathRole();
            if (pathRoleInfo != null) {
                if (doType.equals("add")) {
                    newPathRoleInfo.setDisabled(false);
                    adminPathRolePersistence.update(pathRoleInfo.getId(), newPathRoleInfo);
                    addAdminLog("addRoleToPath", roleName, pathId + "", newPathRoleInfo);
                } else if (doType.equals("del")) {
                    newPathRoleInfo.setDisabled(true);
                    adminPathRolePersistence.update(pathRoleInfo.getId(), newPathRoleInfo);
                    addAdminLog("delRoleToPath", roleName, pathId + "", newPathRoleInfo);
                }
            } else {
                if (doType.equals("add")) {
                    newPathRoleInfo.setPathId(pathId);
                    newPathRoleInfo.setRoleName(roleName);
                    adminPathRolePersistence.persist(newPathRoleInfo);
                    addAdminLog("addRoleToPath", roleName, pathId + "", newPathRoleInfo);
                }
            }
        }

        return null;
    }

    @RequestMapping(value = "path_role_group.vpage", method = RequestMethod.GET)
    public String path_role_group(Model model) {
        String adminName = getCurrentAdminUser().getAdminUserName();
        long pathRoleId = Long.parseLong(getRequestParameter("pathRoleId", "0"));
        List<String> appListForWrite = managementService.getAppList(adminName, "write");
        List<String> groupListForWrite = managementService.getGroupList(adminName, "write");
        Map<String, Object> pathRoleInfo = adminPathRolePersistence.getPathRoleByPathRoleId(pathRoleId, appListForWrite);
        if (pathRoleInfo.size() == 0 || groupListForWrite.size() == 0) {
            getAlertMessageManager().addMessageError("您缺少权限查看此页面");
            return redirect("/management/index.vpage");
        }
        model.addAttribute("pathRoleInfo", pathRoleInfo);
        model.addAttribute("appNames", managementService.getAppNames());
        model.addAttribute("groupList", adminGroupPersistence.withSelectFromTable("WHERE NAME IN (:depNames)").useParams(MapUtils.map("depNames", groupListForWrite)).queryAll());
        model.addAttribute("pathRolegroups", adminPathRoleGroupPersistence.withSelectFromTable("GROUP_NAME", "WHERE PATH_ROLE_ID=? AND DISABLED=0")
                .useParamsArgs(pathRoleId).queryColumnValues().toString());
        model.addAttribute("pageMessage", "角色关联权限组");
        model.addAttribute("showAdmin", managementService.superAdmin(adminName));
        return "management/path/path_role_group";
    }


    @RequestMapping(value = "path_role_group_edit.vpage", method = RequestMethod.POST)
    @ResponseBody
    public Object path_role_group_edit() {
        String doType = getRequestParameter("do", "").replaceAll("\\s", "");
        long pathRoleId = Long.parseLong(getRequestParameter("pathRoleId", "0"));
        String groupName = getRequestParameter("groupName", "").replaceAll("\\s", "");
//        String adminName = getCurrentAdminUser().getAdminUserName();

        if (pathRoleId != 0 && !groupName.equals("")) {
            AdminPathRoleGroup pathRoleGroupInfo = adminPathRoleGroupPersistence.withSelectFromTable("WHERE PATH_ROLE_ID=? AND GROUP_NAME=?")
                    .useParamsArgs(pathRoleId, groupName).queryObject();
            AdminPathRoleGroup newPathRoleGroupInfo = new AdminPathRoleGroup();
            if (pathRoleGroupInfo != null) {
                if (doType.equals("add")) {
                    newPathRoleGroupInfo.setDisabled(false);
                    adminPathRoleGroupPersistence.update(pathRoleGroupInfo.getId(), newPathRoleGroupInfo);
                    addAdminLog("addGroupToRole", groupName, pathRoleId + "", newPathRoleGroupInfo);
                } else if (doType.equals("del")) {
                    newPathRoleGroupInfo.setDisabled(true);
                    adminPathRoleGroupPersistence.update(pathRoleGroupInfo.getId(), newPathRoleGroupInfo);
                    addAdminLog("delGroupToRole", groupName, pathRoleId + "", newPathRoleGroupInfo);
                }
            } else {
                if (doType.equals("add")) {
                    newPathRoleGroupInfo.setPathRoleId(pathRoleId);
                    newPathRoleGroupInfo.setGroupName(groupName);
                    adminPathRoleGroupPersistence.persist(newPathRoleGroupInfo);
                    addAdminLog("addGroupToRole", groupName, pathRoleId + "", newPathRoleGroupInfo);
                }
            }
        }
        return null;
    }


    /**
     * 扫描系统中所有的path
     *
     * @return
     */
    private List<AdminPathExtend> scanningAllAdminPath() {
        List<AdminPathExtend> allPathFromCodeList = new ArrayList<>();
        try {
            ClassPathScanningCandidateComponentProvider scanner = new ClassPathScanningCandidateComponentProvider(false);
            scanner.addIncludeFilter(new AnnotationTypeFilter(Controller.class));
            Set<BeanDefinition> beanSet = scanner.findCandidateComponents("com.voxlearning.utopia.admin.controller");
            for (BeanDefinition def : beanSet) {
                String beanClassName = def.getBeanClassName();
                Class<?> clazz = Class.forName(beanClassName);
                RequestMapping clazzReqMapping = clazz.getAnnotation(RequestMapping.class);
                if (clazzReqMapping == null || clazzReqMapping.value().length == 0 || clazzReqMapping.value().length > 1) {
                    continue;
                }
                String[] claReqMaps = clazzReqMapping.value();
                if (claReqMaps != null && claReqMaps.length > 0) {
                    Method[] methods = clazz.getDeclaredMethods();
                    for (String claReqMap : claReqMaps) {
                        claReqMap = StringUtils.removeStart(claReqMap, "/");
                        claReqMap = StringUtils.removeEnd(claReqMap, "/");
                        if (StringUtils.isNotEmpty(claReqMap)) {
                            String[] claReqMapPaths = claReqMap.split("/");
                            String appName = claReqMapPaths[0];
                            for (Method method : methods) {
                                RequestMapping requestMapping = method.getAnnotation(RequestMapping.class);
                                if (requestMapping == null || requestMapping.value().length == 0 || requestMapping.value().length > 1) {
                                    continue;
                                }
                                for (String reqMapping : requestMapping.value()) {
                                    reqMapping = StringUtils.removeStart(reqMapping, "/");
                                    reqMapping = StringUtils.removeEnd(reqMapping, ".vpage");
                                    //System.out.println(beanClassName + "#" + method.getName() + "->" + claReqMap + "/" + reqMapping + "," + JsonUtils.toJson(requestMapping.method()));
                                    List<String> pathNames = new ArrayList<>();
                                    if (claReqMapPaths.length > 1) {
                                        for (int i = 1; i < claReqMapPaths.length; i++) {
                                            pathNames.add(claReqMapPaths[i]);
                                        }
                                    }
                                    AdminPathExtend path = new AdminPathExtend();
                                    path.setAppName(appName);
                                    String pathName = StringUtils.join(pathNames, "/") + "/" + reqMapping;
                                    pathName = StringUtils.removeStart(pathName, "/");
                                    path.setPathName(pathName);
                                    RequestMethod[] requestMethods = requestMapping.method();
                                    Map<String,Boolean> adminRoles = new HashMap<>();
                                    if (null != requestMethods && requestMethods.length > 0) {
                                        for (RequestMethod requestMethod:requestMethods){
                                            if (requestMethod == RequestMethod.GET){
                                                adminRoles.put("get",true);
                                            }else {
                                                adminRoles.put("get",false);
                                            }
                                            if (requestMethod == RequestMethod.POST){
                                                adminRoles.put("post",true);
                                            }else {
                                                adminRoles.put("post",false);
                                            }
                                        }
                                    }else {
                                        adminRoles.put("get",true);
                                        adminRoles.put("post",true);
                                    }
                                    path.setAdminRoles(adminRoles);
                                    allPathFromCodeList.add(path);
                                }
                            }
                        }
                    }
                }
            }
        } catch (Exception ignored) {

        }
        return allPathFromCodeList;
    }
}
