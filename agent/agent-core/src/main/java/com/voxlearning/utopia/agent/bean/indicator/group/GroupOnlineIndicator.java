package com.voxlearning.utopia.agent.bean.indicator.group;

import com.voxlearning.utopia.agent.bean.indicator.BaseOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * GroupOnlineIndicator
 *
 * @author song.wang
 * @date 2018/8/3
 */
@Getter
@Setter
public class GroupOnlineIndicator extends BaseOnlineIndicator implements Serializable {
    private static final long serialVersionUID = -8766200979904545115L;

    private Long groupId;
    private Integer day;

    private Map<Integer, OnlineIndicator> indicatorMap = new HashMap<>();

}
