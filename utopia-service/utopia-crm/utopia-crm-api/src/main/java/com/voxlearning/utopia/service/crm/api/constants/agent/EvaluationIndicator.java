package com.voxlearning.utopia.service.crm.api.constants.agent;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.AccessLevel;
import lombok.RequiredArgsConstructor;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * 评价指标
 *
 * @author song.wang
 * @date 2018/12/14
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum EvaluationIndicator {


    PREPARATION_SCORE("准备充分度",true),
    PRODUCT_PROFICIENCY_SCORE("产品/话术熟练度",true),
    RESULT_MEET_EXPECTED_RESULT_SCORE("结果符合预期度",true);

    private final String desc;
    private final boolean valid;  // 当前类型是否有效

    private final static Map<String, EvaluationIndicator> NAME_MAP = new LinkedHashMap<>();
    static {
        for(EvaluationIndicator evaluation : EvaluationIndicator.values()){
            NAME_MAP.put(evaluation.name(), evaluation);
        }
    }

    public static EvaluationIndicator nameOf(String name) {
        if(StringUtils.isBlank(name)){
            return null;
        }
        return NAME_MAP.get(StringUtils.trim(name));
    }
}
