package com.voxlearning.utopia.agent.salary.type;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 *
 *
 * @author song.wang
 * @date 2016/9/21
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum SalaryKpiType {
    UNKNOWN(0, "未知类型"),
    JUNIOR_SASC(1, "小学单活"),
    JUNIOR_DASC(2, "小学双活"),
    MIDDLE_SASC(3, "中学单活"),
    RANKING_SALARY(4, "绩效工资"),
    JUNIOR_MEETING_CITY(5, "小学市级专场组会"),
    JUNIOR_MEETING_COUNTY(6, "小学区级专场组会"),
    JUNIOR_MEETING_INTERRUPTED(7, "小学插播组会"),
    JUNIOR_CLUE_SUPPORT(8, "小学线索支持"),
    DICT_SCHOOL_SASC(9, "学校业绩-单活"),
    DICT_SCHOOL_DASC(10, "学校业绩-双活"),
    MIDDLE_MEETING_CITY(11, "中学市级专场组会"),
    MIDDLE_MEETING_COUNTY(12, "中学区级专场组会"),
    MIDDLE_MEETING_INTERRUPTED(13, "中学插播组会"),
    MIDDLE_CLUE_SUPPORT(14, "中学线索支持"),
    US_TALK(15, "USTalk"),
    ORG_GUIDE(16, "机构导流"),
    JUNIOR_ENG(17, "小学英活"),
    MIDDLE_ENG(18, "中学英活")



    ;

    @Getter
    private final int type;
    @Getter
    private final String desc;

    private final static Map<String, SalaryKpiType> KPI_TYPE_MAP = new LinkedHashMap<>();
    private final static Map<String, SalaryKpiType> KPI_DESC_MAP = new LinkedHashMap<>();

    static {
        for (SalaryKpiType kpiType : SalaryKpiType.values()) {
            KPI_TYPE_MAP.put(String.valueOf(kpiType.getType()), kpiType);
            KPI_DESC_MAP.put(kpiType.getDesc(), kpiType);
        }
    }

    public static Map<String, SalaryKpiType> getKpiDescMap() {
        return KPI_DESC_MAP;
    }

}
