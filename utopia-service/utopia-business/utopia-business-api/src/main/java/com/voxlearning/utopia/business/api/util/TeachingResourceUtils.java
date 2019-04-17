package com.voxlearning.utopia.business.api.util;

import com.voxlearning.utopia.service.business.api.entity.TeachingResource;
import com.voxlearning.utopia.service.business.api.mapper.TeachingResourceRaw;
import com.voxlearning.utopia.service.campaign.api.entity.YiqiJTCourse;

import java.util.*;
import java.util.function.Predicate;

public class TeachingResourceUtils {

    public static final String YIQI_JIANGTANG = YiqiJTCourse.Category.YIQI_JIANGTANG.name();
    public static final String HAVE_TYPE_ORDINARY = "ORDINARY";
    public static final String HAVE_TYPE_PRIVILEGE = "PRIVILEGE";

    // 只显示新的三个栏目 + 一起新讲堂
    public static Set<String> category = new HashSet<>();

    static {
        category.add(TeachingResource.Category.WEEK_WELFARE.name());
        category.add(TeachingResource.Category.TEACHING_SPECIAL.name());
        category.add(TeachingResource.Category.SYNC_COURSEWARE.name());
        category.add(YIQI_JIANGTANG);

        category.add(TeachingResource.Category.IMPORTANT_CASE.name());
        category.add(TeachingResource.Category.GROW_UP.name());
        category.add(TeachingResource.Category.ACTIVITY_NOTICE.name());
        category.add(TeachingResource.Category.OTHER_STONE.name());
    }

    public static Predicate<TeachingResourceRaw> filterCategory = (TeachingResourceRaw o) -> {
        if (o == null || o.getCategory() == null) {
            return false;
        }
        return category.contains(o.getCategory());
    };

    public static boolean isCourcseId(String resourceId) {
        if (resourceId == null) {
            return false;
        }
        if (resourceId.length() == 24) {
            return false;
        }
        return true;
    }

    public static boolean isTeachingResource(String resourceId) {
        if (resourceId == null) {
            return false;
        }
        return !isCourcseId(resourceId);
    }

    /**
     * 把 17 讲堂存的科目转化为 teachingResource 的格式
     *
     * @param subjects
     * @return
     */
    public static String convertSubject(List<Integer> subjects) {
        if (subjects == null || subjects.isEmpty()) {
            return "";
        }
        StringBuilder stringBuilder = new StringBuilder();

        Map<Integer, String> map = new HashMap<>();
        map.put(101, "CHINESE");
        map.put(102, "MATH");
        map.put(103, "ENGLISH");

        // emmmm 竟然有重复的
        for (Integer subject : new LinkedHashSet<>(subjects)) {
            stringBuilder.append(map.get(subject)).append(",");
        }
        String string = stringBuilder.toString();
        return string.substring(0, string.length() - 1);
    }
}
