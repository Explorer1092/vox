package com.voxlearning.utopia.agent.bean.es;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import lombok.Getter;
import lombok.Setter;

import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2018/5/24
 */
public class EsQueryConditions {
    public static final String TERM = "term";
    public static final String TERMS = "terms";
    public static final String WILDCARD = "wildcard";
    public static final String RANGE = "range";

    @Getter
    @Setter
    protected List<Map<String, Object>> mustItems = new ArrayList<>();

    @Getter
    @Setter
    private List<Map<String, Object>> shouldItems = new ArrayList<>();

    public EsQueryConditions addShouldQueryConditions(EsQueryConditions conditions){
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("bool", conditions);
        this.shouldItems.add(conditionMap);
        return this;
    }

    public EsQueryConditions addMustQueryConditions(EsQueryConditions conditions){
        Map<String, Object> conditionMap = new HashMap<>();
        conditionMap.put("bool", conditions);
        this.mustItems.add(conditionMap);
        return this;
    }

    public EsQueryConditions addShouldCondition(Map<String, Object> conditionMap){
        if(MapUtils.isNotEmpty(conditionMap)){
            this.shouldItems.add(conditionMap);
        }
        return this;
    }

    public EsQueryConditions addMustCondition(Map<String, Object> conditionMap){
        if(MapUtils.isNotEmpty(conditionMap)){
            this.mustItems.add(conditionMap);
        }
        return this;
    }

    public static Map<String, Object> createTermsCondition(String keyName, Collection keyValue) {
        if(CollectionUtils.isEmpty(keyValue)){
            return Collections.emptyMap();
        }
        Map<String, Object> conditionDetail = new HashMap<>();
        conditionDetail.put(keyName, keyValue);

        Map<String, Object> conditionItem = new HashMap<>();
        conditionItem.put(TERMS, conditionDetail);
        return conditionItem;
    }

    public static Map<String, Object> createTermCondition(String keyName, Object keyValue) {
        if(keyValue == null){
            return Collections.emptyMap();
        }
        Map<String, Object> conditionDetail = new HashMap<>();
        conditionDetail.put(keyName, keyValue);

        Map<String, Object> conditionItem = new HashMap<>();
        conditionItem.put(TERM, conditionDetail);
        return conditionItem;
    }

    public static Map<String, Object> createWildcardQueryCondition(String keyName, String keyValue) {
        if(StringUtils.isBlank(keyValue)){
            return Collections.emptyMap();
        }
        Map<String, Object> conditionDetail = new HashMap<>();
        conditionDetail.put(keyName, "*" + keyValue + "*");

        Map<String, Object> conditionItem = new HashMap<>();
        conditionItem.put(WILDCARD, conditionDetail);
        return conditionItem;
    }

    public static Map<String, Object> createRangeCondition(String keyName, String keyValue1, String keyValue2) {
        Map<String, Object> conditionDetail = new HashMap<>();
        if (StringUtils.isNotBlank(keyValue1)) {
            conditionDetail.put("gte", keyValue1);
        }
        if (StringUtils.isNotBlank(keyValue2)) {
            conditionDetail.put("lte", keyValue2);
        }
        Map<String, Object> conditionItem = new HashMap<>();
        conditionItem.put(keyName, conditionDetail);

        Map<String, Object> rangeItem = new HashMap<>();
        rangeItem.put(RANGE, conditionItem);
        return rangeItem;
    }

    public Map<String, Object> buildBooleanConditions(){
        Map<String, Object> conditionMap = new HashMap<>();

        if(CollectionUtils.isNotEmpty(this.mustItems)){
            List<Map<String, Object>> mustList = mustItems.stream().map(p -> {
                if(p.containsKey("bool")){
                    return ((EsQueryConditions)p.get("bool")).buildBooleanConditions();
                }else {
                    return p;
                }
            }).collect(Collectors.toList());

            conditionMap.put("must", mustList);
        }

        if(CollectionUtils.isNotEmpty(this.shouldItems)){
            List<Map<String, Object>> shouldList = this.shouldItems.stream().map(p -> {
                if(p.containsKey("bool")){
                    return ((EsQueryConditions)p.get("bool")).buildBooleanConditions();
                }else {
                    return p;
                }
            }).collect(Collectors.toList());

            conditionMap.put("should", shouldList);
        }

        Map<String, Object> result = new HashMap<>();
        if(MapUtils.isNotEmpty(conditionMap)){
            result.put("bool", conditionMap);
        }
        return result;
    }
}
