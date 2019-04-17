package com.voxlearning.utopia.agent.view.honeycomb;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.activity.ActivityDataView;
import lombok.Data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
public class HoneycombDataView {
    private Long id;
    private Integer idType;
    private String name;

    private int orderNum;               //订单数
    private int fansNum;                //粉丝数
    private int horizontalContractNum;  //异业签约数
    private int horizontalOrderNum;     //异业订单数

    public Map<String, Object> convertToAverageMap(double devided, int newScale){
        Map<String, Object> dataMap = new HashMap<>();
        dataMap.put("id", this.id);
        dataMap.put("idType", this.idType);
        dataMap.put("name", this.name);

        dataMap.put("orderNum", MathUtils.doubleDivide(SafeConverter.toDouble(this.getOrderNum()), devided, newScale));
        dataMap.put("fansNum", MathUtils.doubleDivide(SafeConverter.toDouble(this.getFansNum()), devided, newScale));
        dataMap.put("horizontalContractNum", MathUtils.doubleDivide(SafeConverter.toDouble(this.getHorizontalContractNum()), devided, newScale));
        dataMap.put("horizontalOrderNum", MathUtils.doubleDivide(SafeConverter.toDouble(this.getHorizontalOrderNum()), devided, newScale));
        return dataMap;
    }
}
