package com.voxlearning.utopia.service.mizar.api.constants.cjlschool;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.user.api.entities.ArtScienceType;

import java.util.HashMap;
import java.util.Map;

/**
 * Created by Yuechen.Wang on 2017/7/31.
 */
public class CJLConstants {

    public static final String SYNC_TEACHER_WEB_SOURCE = "cjl_eschool_sync";

    public static final String SYNC_TEACHER_DEFAULT_PASSWORD = "qsjklx";

    public static final String DEFAULT_CJL_HIGH_SCHOOL_ID = "20140630153845521372954439312732";

    // TODO 根据数据再做调整
    private static final Map<String, ClazzLevel> gradeNameMap;

    // TODO 根据数据再做调整
    private static final Map<String, ArtScienceType> artScienceTypeMap;

    static {
        gradeNameMap = new HashMap<>();
        gradeNameMap.put("高中一年级", ClazzLevel.SENIOR_ONE);
        gradeNameMap.put("高中二年级", ClazzLevel.SENIOR_TWO);
        gradeNameMap.put("高中三年级", ClazzLevel.SENIOR_THREE);

        artScienceTypeMap = new HashMap<>();
        artScienceTypeMap.put("普通班", ArtScienceType.ARTSCIENCE);
        artScienceTypeMap.put("文科班", ArtScienceType.ART);
        artScienceTypeMap.put("理科班", ArtScienceType.SCIENCE);
    }

    public static ClazzLevel parseGrade(String gradeName) {
        return gradeNameMap.get(gradeName);
    }

    public static ArtScienceType parseArtScienceType(String type) {
        if (StringUtils.isBlank(type)) {
            return ArtScienceType.UNKNOWN;
        }
        return artScienceTypeMap.getOrDefault(type, ArtScienceType.UNKNOWN);
    }
}
