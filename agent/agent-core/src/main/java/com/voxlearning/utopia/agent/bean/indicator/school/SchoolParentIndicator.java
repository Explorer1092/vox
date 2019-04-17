package com.voxlearning.utopia.agent.bean.indicator.school;

import com.voxlearning.utopia.agent.bean.indicator.BaseParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SchoolParentIndicator
 * @author deliang.che
 * @since  2019/2/21
 */
@Getter
@Setter
public class SchoolParentIndicator extends BaseParentIndicator implements Serializable {

    private static final long serialVersionUID = 1245786614145412860L;
    private Long schoolId;
    private Integer day;

    private Map<Integer, ParentIndicator> indicatorMap = new HashMap<>();

}
