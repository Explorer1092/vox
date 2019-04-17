package com.voxlearning.utopia.admin.auth;

import com.voxlearning.utopia.admin.constant.AdminPageRole;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;


public class AuthCurrentAdminUser implements Serializable {
    @Getter @Setter String adminUserName;
    @Getter @Setter String departmentName;
    @Getter @Setter String realName;
    @Getter @Setter String password;
    @Getter @Setter String passwordSalt;
    @Getter @Setter String ccAgentId;
    @Getter @Setter String redmineApikey;
    @Getter @Setter Map<String, Object> systemAndPageRightMap;
    @Getter @Setter boolean csosUser = false;

    // 新管理员系统与老的不一样，不一定有 user id，遗留系统有时候还需要提供这个id，所以就先伪造一个
    public Long getFakeUserId() {
        return Long.parseLong("9" + Integer.toString(Math.abs(adminUserName.hashCode())));
    }

    // 判断用户是否有权限访问systemName 下的 url
    public boolean checkAuth(String systemName, String url, AdminPageRole[] authRoles) {

        // 不做检查，所有异常全部返回false
        List<String> authRoleNameList = new ArrayList<>();
        for (AdminPageRole authRole : authRoles) {
            authRoleNameList.add(authRole.getRoleName());
        }
        return checkAuthByRoleNames(systemName, url, authRoleNameList);
    }

    public boolean checkAuthByRoleNames(String systemName, String url, List<String> authRoleNameList) {

        // 不做检查，所有异常全部返回false
        try {
            //格式为{"userName":"admin","isAdmin":true,"pathRight":[roleName1, roleName2,...]}
            Map<String, Object> pageRightMap = (Map<String, Object>)systemAndPageRightMap.get(systemName);
            if ((Boolean)pageRightMap.get("isAdmin"))
                return true;

            Map<String, List> urlAndRolesMap = (Map<String, List>)pageRightMap.get("pathRight");
            List<String> roles = (List<String>)urlAndRolesMap.get(url);
            if (!Collections.disjoint(roles, authRoleNameList))
                return true;
        } catch (Exception ignored) {
            // do nothing, just return false
        }

        return false;
    }

}
