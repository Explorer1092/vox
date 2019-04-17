package com.voxlearning.utopia.admin.data;

import lombok.Getter;
import lombok.Setter;

/**
 * @author guangqing
 * @since 2018/7/26
 */
@Getter
@Setter
public class SelectOption {
    private Object value;
    private String desc;
    private boolean selected;

    public SelectOption(Object value, String desc) {
        this.value = value;
        this.desc = desc;
    }

    public SelectOption() {
    }
}
