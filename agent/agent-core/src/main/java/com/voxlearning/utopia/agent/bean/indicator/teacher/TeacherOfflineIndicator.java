package com.voxlearning.utopia.agent.bean.indicator.teacher;

import com.voxlearning.utopia.agent.bean.indicator.BaseOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * TeacherOfflineIndicator
 *
 * @author song.wang
 * @date 2018/8/21
 */
@Getter
@Setter
public class TeacherOfflineIndicator extends BaseOfflineIndicator implements Serializable {
    private static final long serialVersionUID = 1934218689863117653L;

    private Long teacherId;
    private Integer day;

    private Map<Integer, OfflineIndicator> indicatorMap = new HashMap<>();
}
