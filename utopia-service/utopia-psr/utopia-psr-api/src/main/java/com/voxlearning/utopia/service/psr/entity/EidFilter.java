package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.util.List;

@Data
public class EidFilter {

    // 记录某book 对应的Eid-list,用于判断 题目是否超纲
    private List<String> bookEids;
    private AboveLevelBookUnitEidsNew bookUnitEids;
    private AboveLevelBookUnitEidsNew bookGroupIdEids;
    private AboveLevelBookUnitEidsNewMath bookUnitEidsMath;
    private AboveLevelBookUnitEidsNewMath bookGroupIdEidsMath;
}
