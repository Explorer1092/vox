package com.voxlearning.utopia.service.mizar.api.utils;

/**
 * Created by xiang.lv on 2016/10/17.
 *
 * @author xiang.lv
 * @date 2016/10/17   17:22
 */
public class MizarUserUtil {

    /*public  static boolean isBD(final MizarUser mizarUser){
        return checkRoleType(mizarUser,MizarUserRoleType.BusinessDevelopment);
    }
    *//**
     * 判断当前用户的角色
     * @param mizarUser
     * @param mizarUserRoleType
     * @return
     *//*
    private static  boolean checkRoleType(final MizarUser mizarUser,final MizarUserRoleType mizarUserRoleType){
            List<Integer> roleIdList = mizarUser.getUserRoles();
            if(CollectionUtils.isEmpty(roleIdList)){
                return false;
            }

           return roleIdList.contains(mizarUserRoleType.getId());
        }*/
}
