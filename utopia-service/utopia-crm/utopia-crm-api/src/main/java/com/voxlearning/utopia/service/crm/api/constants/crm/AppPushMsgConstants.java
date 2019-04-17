package com.voxlearning.utopia.service.crm.api.constants.crm;

import java.util.Arrays;
import java.util.List;

public class AppPushMsgConstants {

    public static final String dateFormat = "yyyy-MM-dd HH:mm";

    // 我也不知道为啥是 1,2,3,5....
    public static final int Fixed = 1;         // 全部用户
    public static final int TargetUser = 2;    // 按指定用户投放
    public static final int TargetRegion = 3;  // 按指定地区投放
    public static final int TargetSchool = 4;  // 按指定学校投放
    public static final int TargetTagGroup = 5;   // 按指定标签组投放
    public static final List<Integer> validPushType = Arrays.asList(
            Fixed, TargetUser, TargetRegion, TargetSchool, TargetTagGroup
    );
}
