package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/14.
 * 具体知识技能下的数据信息
 */
@Data
public class ResearchStaffSkillDetailMapper implements Serializable {

    private static final long serialVersionUID = -1654896062575800496L;

    private String patternType;

    private int finishCount;

    private double correctRate;

    private int countPerStudent;

    // 1表示当前技能下做题最多的区/学校，0表示非最多的
    private int rank;
}
