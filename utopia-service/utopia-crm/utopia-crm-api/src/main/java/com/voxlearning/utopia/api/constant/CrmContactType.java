package com.voxlearning.utopia.api.constant;

/**
 * @author Jia HuanYin
 * @since 2015/10/24
 */
public enum CrmContactType {
    电话呼入,
    电话呼出,
    在线咨询,
    微信咨询,
    其他;

    public static CrmContactType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
