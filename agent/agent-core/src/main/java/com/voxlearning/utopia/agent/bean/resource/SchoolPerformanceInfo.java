package com.voxlearning.utopia.agent.bean.resource;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

/**
 * 学校业绩数据
 * <p>
 * Created by yaguang.wang on 2017/2/7.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolPerformanceInfo implements Serializable {
    private static final long serialVersionUID = -2240931229289425548L;
    private Long teacherId;
    private String teacherName;
    private Integer performanceNum;
}
