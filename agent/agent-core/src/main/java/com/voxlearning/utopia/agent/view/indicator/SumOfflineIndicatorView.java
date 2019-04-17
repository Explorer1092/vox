package com.voxlearning.utopia.agent.view.indicator;

import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.support.AgentIndicatorSupport;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * SumOfflineIndicatorView
 *
 * @author song.wang
 * @date 2018/9/25
 */
@Getter
@Setter
public class SumOfflineIndicatorView implements Serializable {

    private Long id;
    private Integer idType;  // ID 类型， 标记ID 是部门ID, 用户ID 还是学校ID
    private Integer schoolLevel;                                                         // 1 ：小学；2 ：初中；4 ：高中；24： 初高中
    private String name;    // 名称  部门名称， 用户名， 学校名等

    private Integer viewType; // 业绩类型
    private String viewName;  // 业绩类型名称


    private Integer stuCount;               // 学生规模
    private Integer klxTotalNum;            // 快乐学考号数
    private Integer scanStuNum;             // 本学期扫描学生总数

    private Integer tmGte1Num;
    private Integer tmSettlementGte1Num;
    private Integer tmUnSettlementGte1Num;

    private Integer tmGte2Num;
    private Integer tmSettlementGte2Num;
    private Integer tmUnSettlementGte2Num;

    private Integer tmScanStuNum;               // 本月扫描学生数

    private Integer pdGte1Num;
    private Integer pdSettlementGte1Num;
    private Integer pdUnSettlementGte1Num;

    private Integer pdGte2Num;
    private Integer pdSettlementGte2Num;
    private Integer pdUnSettlementGte2Num;

    private Integer pdScanStuNum;               // 昨日扫描学生数

    private Double perCapita;                   // 人均


    public Map<String, Object> generateDataMap() {
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("id", this.id);
        retMap.put("dataType", this.idType);
        retMap.put("schoolLevel", this.schoolLevel);
        retMap.put("name", this.name);
        retMap.put("viewType", this.viewType);
        retMap.put("viewName", this.viewName);

        if(viewType == AgentIndicatorSupport.VIEW_TYPE_OVERVIEW_MIDDLE){
            retMap.put("stuCount", this.stuCount);
            retMap.put("klxTotalNum", this.klxTotalNum);
            retMap.put("scanStuNum", this.scanStuNum);

            retMap.put("tmGte1Num", this.tmGte1Num);
            retMap.put("tmSettlementGte1Num", this.tmSettlementGte1Num);
            retMap.put("tmUnSettlementGte1Num", this.tmUnSettlementGte1Num);
            retMap.put("tmGte2Num", this.tmGte2Num);
            retMap.put("tmSettlementGte2Num", this.tmSettlementGte2Num);
            retMap.put("tmUnSettlementGte2Num", this.tmUnSettlementGte2Num);
            retMap.put("tmScanStuNum", this.tmScanStuNum);

            retMap.put("pdGte1Num", this.pdGte1Num);
            retMap.put("pdSettlementGte1Num", this.pdSettlementGte1Num);
            retMap.put("pdUnSettlementGte1Num", this.pdUnSettlementGte1Num);
            retMap.put("pdGte2Num", this.pdGte2Num);
            retMap.put("pdSettlementGte2Num", this.pdSettlementGte2Num);
            retMap.put("pdUnSettlementGte2Num", this.pdUnSettlementGte2Num);
            retMap.put("pdScanStuNum", this.pdScanStuNum);
        }else if(viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_SCAN_GTE1_TM){
            retMap.put("stuNum", this.tmGte1Num);
            retMap.put("settlementNum", this.tmSettlementGte1Num);
            retMap.put("unSettlementNum", this.tmUnSettlementGte1Num);
            if(Objects.equals(this.idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
                retMap.put("perCapita", this.perCapita);
            }
        }else if(viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_SCAN_GTE1_PD){
            retMap.put("stuNum", this.pdGte1Num);
            retMap.put("settlementNum", this.pdSettlementGte1Num);
            retMap.put("unSettlementNum", this.pdUnSettlementGte1Num);
            if(Objects.equals(this.idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
                retMap.put("perCapita", this.perCapita);
            }
        }else if(viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_SCAN_GTE2_TM){
            retMap.put("stuNum", this.tmGte2Num);
            retMap.put("settlementNum", this.tmSettlementGte2Num);
            retMap.put("unSettlementNum", this.tmUnSettlementGte2Num);
            if(Objects.equals(this.idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
                retMap.put("perCapita", this.perCapita);
            }
        }else if(viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_SCAN_GTE2_PD){
            retMap.put("stuNum", this.pdGte2Num);
            retMap.put("settlementNum", this.pdSettlementGte2Num);
            retMap.put("unSettlementNum", this.pdUnSettlementGte2Num);
            if(Objects.equals(this.idType, AgentConstants.INDICATOR_TYPE_GROUP)) {
                retMap.put("perCapita", this.perCapita);
            }
        }
        return retMap;
    }
}
