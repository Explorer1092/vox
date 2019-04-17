package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import lombok.Getter;
import lombok.Setter;

import java.util.HashMap;
import java.util.Map;

/**
 * PerformanceViewDataItem
 *
 * @author song.wang
 * @date 2017/8/28
 */
@Getter
@Setter
public class PerformanceViewDataItem {

    private String name;
    // 小学  初高中线上
    private int mauc; // 月活
    private int maucDf; // 日浮
    private int maucBudget; // 预算
    private double maucCompleteRate; // 完成率

    private int incMauc;                      // 新增月活
    private int bfMauc;                       // 回流月活

    // 初高中扫描
    private int anshGte2StuCount;      // 扫描数
    private int anshGte2IncStuCount;   // 新增
    private int anshGte2BfStuCount;    // 回流
    private int anshGte2StuCountDf;    // 日浮

    public Map<String, Object> generateData(Integer mode){
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("name", name);
        if(mode == 1){
            retMap.put("mauc", mauc);
            retMap.put("maucDf", maucDf);
            retMap.put("maucBudget", maucBudget);
            retMap.put("maucCompleteRate", maucCompleteRate);
        }else if(mode == 2){
            retMap.put("mauc", mauc);
            retMap.put("incMauc", incMauc);
            retMap.put("bfMauc", bfMauc);
            retMap.put("maucDf", maucDf);
        }else if(mode == 3){
            retMap.put("anshGte2StuCount", anshGte2StuCount);
            retMap.put("anshGte2IncStuCount", anshGte2IncStuCount);
            retMap.put("anshGte2BfStuCount", anshGte2BfStuCount);
            retMap.put("anshGte2StuCountDf", anshGte2StuCountDf);
        }
        return retMap;
    }
}
