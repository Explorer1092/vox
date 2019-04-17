package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/13.
 * 具体题型下的数据信息
 */
@Data
public class ResearchStaffPatternDetailMapper implements Serializable {

    private static final long serialVersionUID = -4711332551186770047L;

    private String patternType;
    private int finishCount;

    // 这里是实际正确率*100，用于前端百分比显示
    private double correctRate;
}
