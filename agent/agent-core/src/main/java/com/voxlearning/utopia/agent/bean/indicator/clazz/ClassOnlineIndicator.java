package com.voxlearning.utopia.agent.bean.indicator.clazz;

import com.voxlearning.utopia.agent.bean.indicator.BaseOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 *
 *
 * @author song.wang
 * @date 2018/8/3
 */
@Getter
@Setter
public class ClassOnlineIndicator extends BaseOnlineIndicator implements Serializable {
    private static final long serialVersionUID = 6058317052584724840L;

    private Long classId;
    private Integer day;

    private Map<Integer, OnlineIndicator> indicatorMap = new HashMap<>();

}
