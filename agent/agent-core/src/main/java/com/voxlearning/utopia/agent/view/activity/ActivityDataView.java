package com.voxlearning.utopia.agent.view.activity;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.utils.MathUtils;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class ActivityDataView {

    private Long id;
    private Integer idType;
    private String name;
    private List<ActivityIndicatorData> dataList = new ArrayList<>();

    public Map<String, Object> convertToAverageMap(double devided, int newScale){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", this.id);
        dataMap.put("idType", this.idType);
        dataMap.put("name", this.name);
        List<Map<String, Object>> dataMapList = new ArrayList<>();
        for(ActivityIndicatorData data : dataList){
            Map<String, Object> map = new HashMap<>();
            map.put("indicatorName", data.getIndicatorName());
            map.put("indicatorValue", MathUtils.doubleDivide(SafeConverter.toDouble(data.getIndicatorValue()), devided, newScale));
            dataMapList.add(map);
        }
        dataMap.put("dataList", dataMapList);
        return dataMap;
    }

    @Data
    public static class ActivityIndicatorData {
        private String indicatorName;
        private Object indicatorValue;

    }
}


