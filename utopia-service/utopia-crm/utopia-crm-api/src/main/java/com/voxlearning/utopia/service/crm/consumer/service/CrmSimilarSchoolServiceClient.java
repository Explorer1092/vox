package com.voxlearning.utopia.service.crm.consumer.service;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.utopia.service.crm.tools.SchoolNameSimilarityCalculator;

import java.math.BigDecimal;
import java.util.*;

/**
 * @author fugui.chang
 * @since 2017/3/23
 */
public class CrmSimilarSchoolServiceClient {
    public static final Double SIMILAR_VALUE = 0.75;
    //返回相似度  返回大于等于similarValue并且前limit个相似的学校
    public Map<String, Double> getSchoolNameSimilarity(String schoolNameUgc, Collection<String> schoolNameSysCollection, String city, SchoolLevel schoolLevel, Integer limit, Double similarValue) {
        if (CollectionUtils.isEmpty(schoolNameSysCollection) || schoolLevel == null) {
            return Collections.emptyMap();
        }

        String type = "小学";
        if (Objects.equals(schoolLevel, SchoolLevel.MIDDLE)) {
            type = "初中";
        } else if (Objects.equals(schoolLevel, SchoolLevel.HIGH)) {
            type = "高中";
        } else if (Objects.equals(schoolLevel, SchoolLevel.INFANT)) {
            type = "学前";
        }

        Map<String, BigDecimal> similarMap = new LinkedHashMap<>();
        for (String schoolNameSys : schoolNameSysCollection) {
            Double doubleResult = SchoolNameSimilarityCalculator.calculateSimilarityValue(schoolNameUgc, schoolNameSys, city, type);
            if (doubleResult != null) {
                BigDecimal bigDecimal = BigDecimal.valueOf(doubleResult);
                if (bigDecimal.compareTo(BigDecimal.valueOf(similarValue)) > 0) {
                    similarMap.put(schoolNameSys, bigDecimal);
                }
            }
        }

        //排序后返回前limit个
        Map<String, Double> result = new LinkedHashMap<>();
        similarMap.entrySet().stream().sorted((l1, l2) -> l2.getValue().compareTo(l1.getValue())).limit(limit).forEach(stringBigDecimalEntry -> {
            result.put(stringBigDecimalEntry.getKey(), stringBigDecimalEntry.getValue().doubleValue());
        });

        return result;
    }

}
