package com.voxlearning.utopia.service.crm.api.constants.agent;

import lombok.Getter;

/**
 * @author Jia HuanYin
 * @since 2015/10/10
 */
@Getter
public enum CrmWorkRecordType {
    SCHOOL("进校"),
    MEETING("组会"),
    JOIN_MEETING("参与组会"),
    VISIT("陪同"),
    TEACHING("拜访教研员");

    public final String value;

    CrmWorkRecordType(String value) {
        this.value = value;
    }

    public static CrmWorkRecordType nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
