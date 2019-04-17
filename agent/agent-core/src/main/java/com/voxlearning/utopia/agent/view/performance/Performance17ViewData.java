package com.voxlearning.utopia.agent.view.performance;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.support.AgentIndicatorSupport;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 *
 *
 * @author song.wang
 * @date 2018/3/2
 */
@Getter
@Setter
public class Performance17ViewData implements Serializable {

    private Long id;
    private String idType;  // ID 类型， 标记ID 是部门ID, 用户ID 还是学校ID
    private Integer schoolLevel;                                                         // 1 ：小学；2 ：初中；4 ：高中；24： 初高中
    private String name;    // 名称  部门名称， 用户名， 学校名等

    private Integer viewType; // 业绩类型  1： 小学概览  2：初高中概览  3：小学月活  4 小学新增月活  5 小学长回月活  6：小学短回月活  7：小学注册认证  8：初高中全科
    private String viewName;  // 业绩类型名称  小学， 小数  中英， 中数， 高数

    // 仅天玑首页展示
    private int tmFinEngHwGte3IncAuStuCount;                    // 本月英语新增月活
    private int pdFinEngHwGte3IncAuStuCount;                    // 昨日英语新增月活

    private int tmFinMathHwGte3IncAuStuCount;                    // 本月数学新增月活
    private int pdFinMathHwGte3IncAuStuCount;                    // 昨日数学新增月活

    private int tmFinChnHwGte3IncAuStuCount;                    // 本月语文新增月活
    private int pdFinChnHwGte3IncAuStuCount;                    // 昨日语文新增月活

    // 注册认证
    private int stuScale;          // 学生规模
    private int regStuCount;       // 注册学生数
    private int auStuCount;      // 认证学生数

    private int tmRegStuCount;                                     //本月注册学生数
    private int tmAuStuCount;                                     //本月认证学生数
    private int pdRegStuCount;                                     //昨日注册学生数
    private int pdAuStuCount;                                     //昨日认证学生数

    private int tmPromoteRegStuCount;               //本月学生注册数（升学）
    private int pdPromoteRegStuCount;               //昨日学生注册数（升学）

    private Double perCapita;                                     // 人均


    // 月活部分
    private int tmLoginStuCount;                                  // MAU
    private int tmFinHwGte1StuCount;                              // 本月全部1套（新增，回流）
    private int tmFinHwGte3AuStuCount;                            // 本月认证3套（新增，回流）
    private int pdFinHwGte1StuCount;                              // 昨日全部1套（新增，回流）
    private int pdFinHwGte3AuStuCount;                            // 昨日认证3套（新增，回流）
    private int budget;                                           // 预算
    private Double completeRate;                                  // 完成率

    // 口测
    private int tmFinEngOralTestGte1StuCount;                     // 本月完成1次及以上英语口语测评学生数
    private int pdFinEngOralTestGte1StuCount;                     // 昨日完成1次及以上英语口语测评学生数



    // 新增情况
    private List<Performance17ViewDataItem> incItemList = new ArrayList<>();
    // 回流情况
    private List<Performance17ViewDataItem> bfItemList = new ArrayList<>();

    // 留存情况
    private int engMrtStuCount;                                      // 英语留存数
    private Double engMrtRate;
    private int mathMrtStuCount;                                      // 数学留存数
    private Double mathMrtRate;
    private int chnMrtStuCount;                                      // 语文留存数
    private Double chnMrtRate;

    private int lmFinHwGte3AuStuCount;                            // 留存基数
    private int mrtStuCount;                                      // 留存数
    private Double mrtRate;                                       // 留存率


    public Map<String, Object> generateIndicatorDataMap(){
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("id", this.id);
        retMap.put("dataType", this.idType);
        retMap.put("schoolLevel", this.schoolLevel);
        retMap.put("name", this.name);
        retMap.put("viewType", this.viewType);
        retMap.put("viewName", this.viewName);

        Integer dataType = SafeConverter.toInt(this.idType);

        if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_OVERVIEW_JUNIOR){
            // 注册认证
            retMap.put("stuScale", this.stuScale);
            retMap.put("regStuCount", this.regStuCount);
            retMap.put("auStuCount", this.auStuCount);

            retMap.put("tmRegStuCount", this.tmRegStuCount);
            retMap.put("tmAuStuCount", this.tmAuStuCount);
            retMap.put("pdRegStuCount", this.pdRegStuCount);
            retMap.put("pdAuStuCount", this.pdAuStuCount);

            // 月活
            retMap.put("tmLoginStuCount", this.tmLoginStuCount);
            retMap.put("tmFinHwGte1StuCount", this.tmFinHwGte1StuCount);
            retMap.put("tmFinHwGte3AuStuCount", this.tmFinHwGte3AuStuCount);

//            // 留存
//            retMap.put("engMrtRate", this.engMrtRate);
//            retMap.put("mathMrtRate", this.mathMrtRate);
//            retMap.put("chnMrtRate", this.chnMrtRate);
//
//            retMap.put("engMrtStuCount", this.engMrtStuCount);
//            retMap.put("mathMrtStuCount", this.mathMrtStuCount);
//            retMap.put("chnMrtStuCount", this.chnMrtStuCount);

            // 新增
            retMap.put("incItemList", incItemList.stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList()));
            // 留存
            retMap.put("bfItemList", bfItemList.stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList()));
        }else if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_OVERVIEW_MIDDLE){
            // 注册认证
            retMap.put("stuScale", this.stuScale);
            retMap.put("regStuCount", this.regStuCount);
            retMap.put("auStuCount", this.auStuCount);

            retMap.put("tmRegStuCount", this.tmRegStuCount + this.tmPromoteRegStuCount);
            retMap.put("tmAuStuCount", this.tmAuStuCount);
            retMap.put("pdRegStuCount", this.pdRegStuCount + this.pdPromoteRegStuCount);
            retMap.put("pdAuStuCount", this.pdAuStuCount);

            // 月活
            retMap.put("tmLoginStuCount", this.tmLoginStuCount);
            retMap.put("tmFinHwGte1StuCount", this.tmFinHwGte1StuCount);
            retMap.put("tmFinHwGte3AuStuCount", this.tmFinHwGte3AuStuCount);

//            retMap.put("engMrtRate", this.engMrtRate);
//            retMap.put("mathMrtRate", this.mathMrtRate);
//
//            retMap.put("engMrtStuCount", this.engMrtStuCount);
//            retMap.put("mathMrtStuCount", this.mathMrtStuCount);

            // 新增
            retMap.put("incItemList", incItemList.stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList()));
            // 留存
            retMap.put("bfItemList", bfItemList.stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList()));
        }else if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_REG_AUTH_ALL_TM || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_REG_AUTH_ALL_TM){     // 小学及初高中本月注册认证
            if (this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_REG_AUTH_ALL_TM){
                retMap.put("tmRegStuCount", this.tmRegStuCount);
            }else if (this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_REG_AUTH_ALL_TM){
                retMap.put("tmRegStuCount", this.tmRegStuCount + this.tmPromoteRegStuCount);
            }
            retMap.put("tmAuStuCount", this.tmAuStuCount);
            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
                retMap.put("perCapita", this.perCapita);
            }
        }else if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_REG_AUTH_ALL_PD || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_REG_AUTH_ALL_PD){     // 小学及初高中昨日注册认证
            if (this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_REG_AUTH_ALL_PD){
                retMap.put("pdRegStuCount", this.pdRegStuCount);
            }else if (this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_REG_AUTH_ALL_PD){
                retMap.put("pdRegStuCount", this.pdRegStuCount + this.pdPromoteRegStuCount);
            }
            retMap.put("pdAuStuCount", this.pdAuStuCount);
            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
                retMap.put("perCapita", this.perCapita);
            }
        }else if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_MAUC_SGLSUBJ_TM                  // 小学月活-单科-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_MAUC_ENG_TM                  // 小学月活-英语-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_MAUC_MATH_TM                  // 小学月活-数学-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_MAUC_CHN_TM                  // 小学月活-语文-本月

                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_MAUC_SGLSUBJ_TM                  // 初高中月活-单科-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_MAUC_ENG_TM                  // 初高中月活-英语-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_MAUC_MATH_TM                  // 初高中月活-数学-本月
                ){
            retMap.put("tmFinHwGte1StuCount", this.tmFinHwGte1StuCount);
            retMap.put("tmFinHwGte3AuStuCount", this.tmFinHwGte3AuStuCount);
            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
                retMap.put("perCapita", this.perCapita);
            }
        }else if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_ENG_TM                  //小学新增-英语-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_MATH_TM                  //小学新增-数学-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_CHN_TM                  //小学新增-语文-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_SGLSUBJ_TM  //小学新增-单科-本月

                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_INC_SGLSUBJ_TM                  // 初高中新增-单科-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_INC_ENG_TM                  // 初高中新增-英语-本月
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_INC_MATH_TM                  // 初高中新增-数学-本月
                ){
            retMap.put("tmFinHwGte1StuCount", this.tmFinHwGte1StuCount);
            retMap.put("tmFinHwGte3AuStuCount", this.tmFinHwGte3AuStuCount);
            if (this.viewType != AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_ENG_TM                  //小学新增-英语-本月
                    && this.viewType != AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_MATH_TM          //小学新增-数学-本月
                    && this.viewType != AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_CHN_TM           //小学新增-语文-本月
                    ){
                retMap.put("budget", this.budget);
                retMap.put("completeRate", this.completeRate);
            }

            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
                retMap.put("perCapita", this.perCapita);
            }
        }else if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_ENG_PD                 // 小学新增-英语-昨日
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_MATH_PD                 //小学新增-数学-昨日
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_CHN_PD                 //小学新增-语文-昨日
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_INC_SGLSUBJ_PD                 //小学新增-单科-昨日

                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_INC_SGLSUBJ_PD                 // 初高中新增-单科-昨日
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_INC_ENG_PD                 // 初高中新增-英语-昨日
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_INC_MATH_PD                 // 初高中新增-数学-昨日
                ){
            retMap.put("pdFinHwGte1StuCount", this.pdFinHwGte1StuCount);
            retMap.put("pdFinHwGte3AuStuCount", this.pdFinHwGte3AuStuCount);
            if(Objects.equals(dataType, AgentConstants.INDICATOR_TYPE_GROUP)){
                retMap.put("perCapita", this.perCapita);
            }
        }else if(this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_ENG_TM                 // 小学留存-英语
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_MATH_TM                 // 小学留存-数学
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_CHN_TM                 // 小学留存-语文
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_JUNIOR_RT_SGLSUBJ_TM                 // 小学留存-单科

                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_RT_ENG_TM                 // 初高中留存-英语
                || this.viewType == AgentIndicatorSupport.VIEW_TYPE_MIDDLE_RT_MATH_TM                 // 初高中留存-数学
                ){
            retMap.put("lmFinHwGte3AuStuCount", this.lmFinHwGte3AuStuCount);
            retMap.put("mrtStuCount", this.mrtStuCount);
            retMap.put("mrtRate", this.mrtRate);
        }

        return retMap;
    }


}
