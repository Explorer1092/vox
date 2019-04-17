package com.voxlearning.utopia.agent.bean.es;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;

import java.util.*;

/**
 * SchoolEsQueryContent
 *
 * @author song.wang
 * @date 2018/5/27
 */
public class SchoolEsQueryContent {

    private static final String PRODUCT_ES_URL = "http://10.7.7.37:9200/vox_crm_school_summary_v11/main/_search?";

    private Map<Long, Map<String, Object>> schoolSourceMap = new LinkedHashMap<>();  // 保证有序， 查询可能指定相关性评分

    public SchoolEsQueryContent(String es) {
        AlpsHttpResponse response = HttpRequestExecutor.defaultInstance().post(PRODUCT_ES_URL).json(es).execute();
        Map<String, Object> responseMap = JsonUtils.convertJsonObjectToMap(response.getResponseString());
        Map<String, Object> hitInfoMap = (Map) responseMap.get("hits");
        if (MapUtils.isNotEmpty(hitInfoMap)) {
            Object tmpList = hitInfoMap.get("hits");
            if (tmpList != null) {
                List<Map<String, Object>> hitList = (List) (tmpList);
                for (Map<String, Object> map : hitList) {
                    Long schoolId = ConversionUtils.toLong(map.get("_id"));
                    Map<String, Object> sourceMap = null;
                    if(map.containsKey("_source")){
                        sourceMap = (Map<String, Object>) map.get("_source");
                    }
                    if(schoolId > 0){
                        schoolSourceMap.put(schoolId, sourceMap);
                    }
                }
            }
        }
    }

    public Set<Long> getSchoolIds() {
        Set<Long> result = new LinkedHashSet<>();  // 保证有序
        schoolSourceMap.forEach((k, v) -> result.add(k));
        return result;
    }

    public Map<Long, Map<String, Object>> getSchoolWithSourceData(){
        return this.schoolSourceMap;
    }
}
