package com.voxlearning.utopia.agent.bean.es;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.http.client.execute.AlpsHttpResponse;
import com.voxlearning.alps.http.client.execute.HttpRequestExecutor;
import com.voxlearning.alps.lang.convert.ConversionUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.runtime.RuntimeMode;

import java.util.*;

/**
 * TeacherEsQueryContent
 *
 * @author song.wang
 * @date 2018/5/25
 */
public class TeacherEsQueryContent {

    private static final String PRODUCT_ES_URL = "http://10.7.7.37:9200/vox_crm_teacher_summary_v11/main/_search?";


    private Map<Long, Map<String, Object>> teacherSourceMap = new LinkedHashMap<>();  // 保证有序， 查询可能指定相关性评分

    public TeacherEsQueryContent(String es) {
        AlpsHttpResponse response;
        if (RuntimeMode.isProduction() || RuntimeMode.isStaging()) {
            response = HttpRequestExecutor.defaultInstance().post(PRODUCT_ES_URL).json(es).execute();
        } else {
            response = HttpRequestExecutor.defaultInstance().post(PRODUCT_ES_URL).json(es).execute();
        }

        Map<String, Object> responseMap = JsonUtils.convertJsonObjectToMap(response.getResponseString());
        Map<String, Object> hitInfoMap = (Map) responseMap.get("hits");
        if (MapUtils.isNotEmpty(hitInfoMap)) {

            Object tmpList = hitInfoMap.get("hits");
            if (tmpList != null) {
                List<Map<String, Object>> hitList = (List) (tmpList);
                for (Map<String, Object> map : hitList) {
                    Long teacherId = ConversionUtils.toLong(map.get("_id"));
                    Map<String, Object> sourceMap = null;
                    if(map.containsKey("_source")){
                        sourceMap = (Map<String, Object>) map.get("_source");
                    }
                    if(teacherId > 0){
                        teacherSourceMap.put(teacherId, sourceMap);
                    }
                }
            }
        }
    }

    public Set<Long> getTeacherIds() {
        Set<Long> result = new LinkedHashSet<>();  // 保证有序
        teacherSourceMap.forEach((k, v) -> result.add(k));
        return result;
    }

    public Map<Long, Map<String, Object>> getTeacherWithSourceData(){
        return this.teacherSourceMap;
    }
}
