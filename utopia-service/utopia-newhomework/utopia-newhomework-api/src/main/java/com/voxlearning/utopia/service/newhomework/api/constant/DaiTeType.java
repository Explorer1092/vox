package com.voxlearning.utopia.service.newhomework.api.constant;


import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.MapUtils;
import lombok.Getter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 戴特内容类型
 */
public enum DaiTeType {

    LEVEL_READINGS("绘本"),//推介绘本
    ALL_LEVEL_READINGS("全部绘本"),
    CLASS_COURSE_WARE("课件"),
    WORD_RECOGNITION_AND_READING("生字认读"),
    INTELLIGENT_TEACHING("讲练测"),
    BASIC_APP("基础练习"),
    KEY_POINTS("重难点视频专练"),
    CUO_TI_BAO("易错题微课"), //原名：错题宝
    ZI_KA("字卡"),
    WORD_TEACH_AND_PRACTICE("字词讲练"),
    NATURAL_SPELLING("自然拼读"),
    ALL_NATURAL_SPELLING("全部自然拼读");

    public static List<Map<String, Object>> getTypesBySubject(Subject subject) {
        List<Map<String, Object>> typeMapperList = new ArrayList<>();
        if (subject.equals(Subject.CHINESE)) {
            typeMapperList.add(MapUtils.m("type", DaiTeType.WORD_RECOGNITION_AND_READING, "typeName", DaiTeType.WORD_RECOGNITION_AND_READING.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.WORD_TEACH_AND_PRACTICE, "typeName", DaiTeType.WORD_TEACH_AND_PRACTICE.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.LEVEL_READINGS, "typeName", DaiTeType.LEVEL_READINGS.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.CUO_TI_BAO, "typeName", DaiTeType.CUO_TI_BAO.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.CLASS_COURSE_WARE, "typeName", DaiTeType.CLASS_COURSE_WARE.getTypeName()));
        }
        if (subject.equals(Subject.MATH)) {
            typeMapperList.add(MapUtils.m("type", DaiTeType.INTELLIGENT_TEACHING, "typeName", DaiTeType.INTELLIGENT_TEACHING.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.KEY_POINTS, "typeName", DaiTeType.KEY_POINTS.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.CUO_TI_BAO, "typeName", DaiTeType.CUO_TI_BAO.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.CLASS_COURSE_WARE, "typeName", DaiTeType.CLASS_COURSE_WARE.getTypeName()));
        }
        if (subject.equals(Subject.ENGLISH)) {
            typeMapperList.add(MapUtils.m("type", DaiTeType.BASIC_APP, "typeName", DaiTeType.BASIC_APP.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.LEVEL_READINGS, "typeName", DaiTeType.LEVEL_READINGS.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.NATURAL_SPELLING, "typeName", DaiTeType.NATURAL_SPELLING.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.CUO_TI_BAO, "typeName", DaiTeType.CUO_TI_BAO.getTypeName()));
            typeMapperList.add(MapUtils.m("type", DaiTeType.CLASS_COURSE_WARE, "typeName", DaiTeType.CLASS_COURSE_WARE.getTypeName()));
        }
        return typeMapperList;
    }

    @Getter
    private final String typeName;

    DaiTeType(String typeName) {
        this.typeName = typeName;
    }

    public static DaiTeType of(String name) {
        try {
            return valueOf(name);
        } catch (Exception ex) {
            return null;
        }
    }

}
