package com.voxlearning.utopia.service.reward.util;

import com.voxlearning.alps.core.util.StringUtils;

public class TeacherNameWrapper {

    /**
     * 名称后追加“老师”
     */
    public static String respectfulName(String teacherRealName) {
        if (StringUtils.isBlank(teacherRealName)) {
            return "老师";
        }
        if (StringUtils.endsWith(teacherRealName, "老师")) {
            return teacherRealName;
        }
        return teacherRealName + "老师";
    }

    /**
     * 第一个字追加“老师”
     */
    public static String firstName(String teacherRealName) {
        if (StringUtils.isBlank(teacherRealName)) {
            return "老师";
        }
        return teacherRealName.substring(0, 1) + "老师";
    }
}
