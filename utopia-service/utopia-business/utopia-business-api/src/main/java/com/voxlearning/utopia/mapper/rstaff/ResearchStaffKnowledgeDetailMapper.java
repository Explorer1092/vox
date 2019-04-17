package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;

/**
 * Created by Changyuan on 2015/1/15.
 * 具体语言知识下的数据信息
 */
@Data
public class ResearchStaffKnowledgeDetailMapper implements Serializable {

    private static final long serialVersionUID = 526641094215603552L;

    private int finishCount;

    private double correctRate;

    private int countPerStudent;

    // 1表示做题最多的区/学校，0表示非最多的
    private int rank;
}
