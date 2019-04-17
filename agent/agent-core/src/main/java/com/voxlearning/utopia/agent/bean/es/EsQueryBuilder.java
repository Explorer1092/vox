package com.voxlearning.utopia.agent.bean.es;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import lombok.Getter;

import java.util.HashMap;
import java.util.Map;

/**
 * EsQueryBuilder
 *
 * @author song.wang
 * @date 2018/5/24
 */
public class EsQueryBuilder {
    @Getter
    protected EsQueryConditions query;
    @Getter
    protected EsQueryConditions filter;

    protected int pageFrom = 0;
    protected int pageSize = 0;
    protected Object source = false;  // 默认不获取 _source 数据   false: 不获取_source数据，  [] ：获取所有的_source数据， ["A", "B"]: 获取A, B字段数据

    public EsQueryBuilder(){
        this.query = new EsQueryConditions();
        this.filter = new EsQueryConditions();
    }

    public EsQueryBuilder withPageFrom(Integer from, Integer size) {
        if (from != null && from > 0) {
            this.pageFrom = from;
        }
        if (size != null && size > 0) {
            this.pageSize = size;
        }
        return this;
    }

    public EsQueryBuilder withSource(Object sourceData){
        this.source = sourceData;   //  false: 不获取_source数据，  [] ：获取所有的_source数据， ["A", "B"]: 获取A, B字段数据
        return this;
    }

    private Map<String, Object> buildQueryCondition(){
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> queryConditions = this.query.buildBooleanConditions();
        if(MapUtils.isNotEmpty(queryConditions)){
            resultMap.put("query", queryConditions);
        }

        return resultMap;
    }

    private Map<String, Object> buildFilterCondition(){
        Map<String, Object> resultMap = new HashMap<>();
        Map<String, Object> filterConditions = this.filter.buildBooleanConditions();
        if(MapUtils.isNotEmpty(filterConditions)){
            resultMap.put("filter", filterConditions);
        }
        return resultMap;
    }


    public String buildQueryString(){
        Map<String, Object> filteredMap = new HashMap<>();
        Map<String, Object> queryConditionMap = this.buildQueryCondition();
        if(MapUtils.isNotEmpty(queryConditionMap)){
            filteredMap.putAll(queryConditionMap);
        }
        Map<String, Object> filterConditionMap = this.buildFilterCondition();
        if(MapUtils.isNotEmpty(filterConditionMap)){
            filteredMap.putAll(filterConditionMap);
        }

        Map<String, Object> queryMap = new HashMap<>();
        queryMap.put("filtered", filteredMap);

        Map<String, Object> finalMap = new HashMap<>();
        finalMap.put("query", queryMap);
        if(this.pageFrom > 0){
            finalMap.put("from", this.pageFrom);
        }

        if (this.pageSize > 0) {
            finalMap.put("size", this.pageSize);
        }

        if(this.source != null){
            finalMap.put("_source", source);
        }
        return JsonUtils.toJson(finalMap);
    }
}
