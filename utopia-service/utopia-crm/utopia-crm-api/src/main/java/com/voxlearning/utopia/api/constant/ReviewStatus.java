package com.voxlearning.utopia.api.constant;

import lombok.Getter;

/**
 * @author Jia HuanYin
 * @since 2015/12/2
 */
@Getter
public enum ReviewStatus {

    WAIT("待审核"),
    PASS("已通过"),
    REJECT("已驳回");

    public final String value;

    ReviewStatus(String value) {
        this.value = value;
    }

    public static ReviewStatus nameOf(String name) {
        try {
            return valueOf(name);
        } catch (Exception e) {
            return null;
        }
    }
}
