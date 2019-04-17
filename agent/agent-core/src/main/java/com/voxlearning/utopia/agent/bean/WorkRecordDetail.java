package com.voxlearning.utopia.agent.bean;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * @author Jia HuanYin
 * @since 2015/11/6
 */

@Getter
@Setter
@AllArgsConstructor
public class WorkRecordDetail implements Serializable, Comparable {
    private static final long serialVersionUID = -4044667197212081933L;

    private String code;
    private String name;
    private int count;

    @Override
    public int compareTo(Object other) {
        if (other == null || !(other instanceof WorkRecordDetail)) {
            return -1;
        }
        WorkRecordDetail bean = (WorkRecordDetail) other;
        return bean.count - this.count;
    }
}
