package com.voxlearning.utopia.mizar.auth;


import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Objects;

import static com.voxlearning.utopia.service.mizar.api.constants.MizarUserRoleType.*;

/**
 * Mizar Authed User Info
 * Created by alex on 2016/8/18.
 */
@Getter
@Setter
@NoArgsConstructor
public class MizarAuthUser implements Serializable {

    private String userId;
    private String accountName;
    private String realName;
    private String mobile;
    private List<Integer> roleList;                // 角色列表
    private List<String> shopList;                 // 机构列表
    private List<String> officialAccountKeyList;   // 公众号标识列表
    private List<Long> schoolList;                 // 学校列表
    private List<String> authPathList;             // 权限列表

    // 检查用户的权限
    public boolean checkSysAuth(String appName, String subSysPath) {
        if (StringUtils.isEmpty(appName) || StringUtils.isEmpty(subSysPath)) {
            return false;
        }

        String pathUrl = "/" + appName + "/";
        if (!Objects.equals("*", subSysPath)) {
            pathUrl = pathUrl + subSysPath;
        }

        for (String authPath : authPathList) {
            if (authPath.startsWith(pathUrl)) {
                return true;
            }
        }
        return false;
    }

    public boolean isAdmin() {
        return checkUserRole(MizarAdmin);
    }

    public boolean isOperator() {
        return checkUserRole(Operator);
    }

    public boolean isShopOwner() {
        return checkUserRole(ShopOwner);
    }

    public boolean isBD() {
        return checkUserRole(BusinessDevelopment);
    }

    public boolean isMicroTeacher() {
        return checkUserRole(MicroTeacher);
    }

    public boolean isInfantOp() {
        return checkUserRole(INFANT_OP);
    }

    public boolean isTangramJury() {
        return checkUserRole(TangramJury);
    }

    public boolean isJxTeacherResearch() {
        return checkUserRole(JX_TEACHER_RESEARCH);
    }

    public boolean isBDManager(){
        return checkUserRole(BusinessDevelopmentManager);
    }

    private boolean checkUserRole(MizarUserRoleType role) {
        return roleList != null && role != null && roleList.contains(role.getId());
    }

    public static String ck_user(String id) {
        return CacheKeyGenerator.generateCacheKey(MizarAuthUser.class, id);
    }

}
