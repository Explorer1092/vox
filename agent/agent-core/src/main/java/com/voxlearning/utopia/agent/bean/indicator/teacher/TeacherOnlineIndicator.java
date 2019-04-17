package com.voxlearning.utopia.agent.bean.indicator.teacher;

import com.voxlearning.utopia.agent.bean.indicator.BaseOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * TeacherOnlineIndicator
 *
 * @author song.wang
 * @date 2018/8/3
 */
@Getter
@Setter
public class TeacherOnlineIndicator extends BaseOnlineIndicator implements Serializable {

    private static final long serialVersionUID = -824840268593881332L;

    private Long teacherId;
    private Integer day;

    private Map<Integer, OnlineIndicator> indicatorMap = new HashMap<>();

}
