package com.voxlearning.utopia.agent.bean.indicator.clazz;

import com.voxlearning.utopia.agent.bean.indicator.BaseOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * ClassOfflineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
@Getter
@Setter
public class ClassOfflineIndicator extends BaseOfflineIndicator implements Serializable {
    private static final long serialVersionUID = 6269066075287476923L;

    private Long classId;
    private Integer day;

    private Map<Integer, OfflineIndicator> indicatorMap = new HashMap<>();
}
