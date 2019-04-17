package com.voxlearning.utopia.agent.bean;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

/**
 * Created by yaguang.wang on 2016/7/4.
 */
@Getter
@Setter
public class workRecordBufferData {
    private Long schoolId;
    private List<Long> teacherId;
}
