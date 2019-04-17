package com.voxlearning.utopia.admin.data;

import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.admin.persist.entity.AdminPath;
import com.voxlearning.utopia.admin.persist.entity.AdminRole;
import lombok.Data;
import lombok.Getter;
import lombok.Setter;

import java.lang.reflect.InvocationTargetException;
import java.util.List;
import java.util.Map;

/**
 * AdminPath扩展
 *
 * @author chunlin.yu
 * @create 2018-05-08 18:58
 **/

@Getter
@Setter
public class AdminPathExtend extends AdminPath {
    private static final long serialVersionUID = -1597061349338671737L;


    // roleName,checked
    private Map<String,Boolean> adminRoles;

    public static AdminPathExtend fromAdminPath(AdminPath adminPath){
        if (adminPath == null){
            return null;
        }
        AdminPathExtend  ape = new AdminPathExtend();

        try {
            BeanUtils.copyProperties(ape,adminPath);
            return ape;
        } catch (Exception ignored) {
        }
        return null;
    }
}
