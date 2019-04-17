package com.voxlearning.utopia.mapper.rstaff;

import lombok.Data;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Created by Changyuan on 2015/1/13.
 * 教研员题型Mapper
 */
@Data
public class ResearchStaffPatternMapper implements Serializable {
    private static final long serialVersionUID = 2014840377180604034L;

    // 题型=>做题总量，按做题量由高到底排序
    private Map<String, Long> patternRank;

    private long countyCount;   // 区域数，仅省教研员使用

    private long schoolCount;   // 学校数

    private long validSchoolCount;  // 有效学校数

    List<ResearchStaffPatternUnitMapper> patternUnitList = new ArrayList<>();
}
