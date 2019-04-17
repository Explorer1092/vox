package com.voxlearning.utopia.service.newhomework.impl.service.queue;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2017/9/27
 */
@Getter
@Setter
public class SaveSelfStudyWordIncreaseHomeworkCommand implements Serializable {

    private static final long serialVersionUID = -9009992732404715611L;

    private Long clazzGroupId;
    private Long studentId;
    private Map<String, Map<String, List<String>>> bookToKpMap;
}
