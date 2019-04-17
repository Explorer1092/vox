package com.voxlearning.utopia.service.psr.entity;

import lombok.Data;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

@Data
public class EkGradeRegionContent implements Serializable {

    private static final long serialVersionUID = 1518151130205839530L;

    // 知识点
    private String ek;

    private Map<Integer/*grade*/, EkRegionContent> ekGradesInfo = new HashMap<>();

    // 随机记录一个默认的信息(随机年级,随机地区),如果从ekGradesInfo中获取不到数据,则使用该默认数据
    private EkRegionItem defaultRegionInfo;

    public EkRegionItem getEkRegionItemByGradeAndRegion(Integer grade, Integer region) {
        if (grade == null || grade < 3|| grade > 6 || region == null)
            return null;

        EkRegionContent ekRegionContent = getEkRegionContentByGrade(grade);
        if (ekRegionContent == null)
            return null;

        EkRegionItem ekRegionItem = ekRegionContent.getEkRegionItemByRegion(region);
        if (ekRegionItem == null)
            ekRegionItem = ekRegionContent.getEkRegionItemRandom();  // 指定地区下没有题,则随机一个吧

        return ekRegionItem;
    }

    public EkRegionContent getEkRegionContentByGrade(Integer grade) {
        if (grade == null || grade < 3|| grade > 6)
            return null;

        if (ekGradesInfo.containsKey(grade))
            return ekGradesInfo.get(grade);

        return null;
    }

    public void setEkRegionContent(Integer grade, EkRegionContent ekRegionContent) {
        if (grade == null || grade < 3|| grade > 6 || ekRegionContent == null)
            return;

        ekGradesInfo.put(grade, ekRegionContent);
    }

}


