package com.voxlearning.utopia.agent.bean.indicator.school;

import com.voxlearning.utopia.agent.bean.indicator.BaseOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * SchoolOfflineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
@Getter
@Setter
public class SchoolOfflineIndicator extends BaseOfflineIndicator implements Serializable {
    private static final long serialVersionUID = -5248469292357456232L;

    private Long schoolId;
    private Integer day;

    private Map<Integer, OfflineIndicator> indicatorMap = new HashMap<>();
}
