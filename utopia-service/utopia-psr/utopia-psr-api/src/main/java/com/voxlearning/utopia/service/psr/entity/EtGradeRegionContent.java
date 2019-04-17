package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class EtGradeRegionContent implements Serializable {

    private static final long serialVersionUID = 6326282401875873933L;

    // 知识点
    private String et;

    private Map<Integer/*grade*/, EtRegionContent> etGradesInfo = new HashMap<>();

    // 随机记录一个默认的信息(随机年级,随机地区),如果从ekGradesInfo中获取不到数据,则使用该默认数据
    private EtRegionItem defaultRegionInfo;

    public EtRegionItem getEtRegionItemByGradeAndRegion(Integer grade, Integer region) {
        if (grade == null || grade < 1 || grade > 6 || region == null)
            return null;

        EtRegionContent etRegionContent = getEtRegionContentByGrade(grade);
        if (etRegionContent == null)
            return null;

        EtRegionItem etRegionItem = etRegionContent.getEtRegionItemByRegion(region);
        if (etRegionItem == null)
            etRegionItem = etRegionContent.getEtRegionItemRandom();  // 指定地区下没有题,则随机一个吧

        return etRegionItem;
    }

    public EtRegionContent getEtRegionContentByGrade(Integer grade) {
        if (grade == null || grade < 1 || grade > 6)
            return null;

        if (etGradesInfo.containsKey(grade))
            return etGradesInfo.get(grade);

        return null;
    }

    public void setEtRegionContent(Integer grade, EtRegionContent etRegionContent) {
        if (grade == null || grade < 1 || grade > 6 || etRegionContent == null)
            return;

        etGradesInfo.put(grade, etRegionContent);
    }

}


