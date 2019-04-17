package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by Changyuan on 2015/1/15.
 * 教研员单元薄弱Mapper
 */
@Data
public class ResearchStaffUnitWeakPointMapper implements Serializable {

    private static final long serialVersionUID = -2870980956943259027L;

    private List<String> selectedPresses = new ArrayList<>();   // 被选取的样本教材

    //private int maxUnitNum; // 教材最大单元数

    private boolean samePressFlag;

    private Map<String, List<ResearchStaffUnitWeakPointUnitMapper>> regionWeakPointMap;
}
