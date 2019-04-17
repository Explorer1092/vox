package com.voxlearning.utopia.agent.bean.indicator.clazz;

import com.voxlearning.utopia.agent.bean.indicator.BaseParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *  ClassParentIndicator
 *
 * @author deliang.che
 * @since  2019/2/22
 */
@Getter
@Setter
public class ClassParentIndicator extends BaseParentIndicator implements Serializable {

    private static final long serialVersionUID = -65273636257846357L;
    private Long classId;
    private Integer day;

    private Map<Integer, ParentIndicator> indicatorMap = new HashMap<>();

}
