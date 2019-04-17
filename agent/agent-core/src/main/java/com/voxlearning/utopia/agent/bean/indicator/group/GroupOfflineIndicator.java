package com.voxlearning.utopia.agent.bean.indicator.group;

import com.voxlearning.utopia.agent.bean.indicator.BaseOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * GroupOfflineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
@Getter
@Setter
public class GroupOfflineIndicator extends BaseOfflineIndicator implements Serializable {
    private static final long serialVersionUID = -3157064231683874186L;

    private Long groupId;
    private Integer day;

    private Map<Integer, OfflineIndicator> indicatorMap = new HashMap<>();

}
