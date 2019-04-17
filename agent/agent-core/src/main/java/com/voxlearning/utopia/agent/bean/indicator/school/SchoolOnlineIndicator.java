package com.voxlearning.utopia.agent.bean.indicator.school;

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
public class SchoolOnlineIndicator extends BaseOnlineIndicator implements Serializable {

    private static final long serialVersionUID = -4294222814097880848L;

    private Long schoolId;
    private Integer day;
//    private Integer dimension;    // 数据维度： 1:昨日 2:当月 3:学期 4:累计

    private Map<Integer, OnlineIndicator> indicatorMap = new HashMap<>();


}
