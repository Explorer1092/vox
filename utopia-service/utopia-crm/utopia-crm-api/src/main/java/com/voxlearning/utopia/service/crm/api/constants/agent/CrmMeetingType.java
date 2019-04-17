package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;

/**
 * @author Jia HuanYin
 * @since 2015/8/18
 */
@Getter
public enum CrmMeetingType {
    PROVINCE_LEVEL("省级会议"),
    CITY_LEVEL("市级会议"),
    COUNTY_LEVEL("区级会议"),
    SCHOOL_LEVEL("校级会议");

    public final String value;

    CrmMeetingType(String value) {
        this.value = value;
    }

    public static CrmMeetingType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
