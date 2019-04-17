package com.voxlearning.utopia.agent.bean;

import com.voxlearning.utopia.agent.BeanMapUtils;
import com.voxlearning.utopia.agent.utils.MathUtils;
import lombok.Getter;
import lombok.Setter;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * PerformanceViewData
 *
 * @author song.wang
 * @date 2017/4/11
 */
@Getter
@Setter
public class PerformanceViewData {

    private Long id;
    private String idType;  // ID 类型， 标记ID 是部门ID, 用户ID 还是学校ID
    private String name;    // 名称  部门名称， 用户名， 学校名等

    private Integer viewType; // 业绩类型  1： 小学概览  2：初高中概览  3：小学月活  4 小学新增月活  5 小学长回月活  6：小学短回月活  7：小学注册认证  8：初高中全科
    private String viewName;  // 业绩类型名称  小学， 小数  中英， 中数， 高数

    private int stuScale;          // 学生规模
    private int regStuCount;       // 注册学生数
    private int authStuCount;      // 认证学生数

    private int mauc; // 月活
    private int maucDf; // 日浮
    private int maucBudget; // 预算
    private double maucCompleteRate; // 完成率

    private int incMauc;                      // 新增月活
    private int bfMauc;                       // 回流月活

    private int monthRegStuCount;   // 本月注册
    private int monthAuthStuCount;  // 本月认证
    private int regStuCountDf;      // 昨日注册
    private int authStuCountDf;     // 昨日认证

    private int finHwEq1StuCount;  // 一套
    private int finHwEq2StuCount;  // 两套
    private int finHwGte3StuCount;  // 三套

    private int lmMauc;  // 上月同天月活
    private int lmDiff;  // 与上月同天的差值
    private double lmRate;       // 月环比


    private int stuKlxTnCount;

    private int anshGte2StuCount;
    private int anshGte2IncStuCount;
    private int anshGte2BfStuCount;
    private int anshGte2StuCountDf;

    private int lowAnshEq1StuCount;
    private int lowAnshGte2StuCount;
    private int highAnshEq1StuCount;
    private int highAnshGte2StuCount;



    @Deprecated private int engHwGte3AuStuCount;
    @Deprecated private int engHwGte3AuStuCountDf;

    private int thSemUnauthStuCount;                        // 本学期注册未认证学生数

    // 业绩列表（用于概览页中的业绩列表）
    private List<PerformanceViewDataItem> dataItemList = new ArrayList<>();

    // 月环比列表（用于概览页中的月环比列表）
    private List<PerformanceViewDataLmRateItem> lmRateDataList = new ArrayList<>();


    public void setMaucData(String viewName, int mauc, int maucDf, int maucBudget, double maucCompleteRate){
        this.viewName = viewName;
        this.mauc = mauc;
        this.maucDf = maucDf;
        this.maucBudget = maucBudget;
        this.maucCompleteRate = maucCompleteRate;
    }

    public void setMaucData(String viewName, int mauc, int maucDf){
        this.viewName = viewName;
        this.mauc = mauc;
        this.maucDf = maucDf;
    }

    public void setMaucData(String viewName, int mauc, int incMauc, int bfMauc, int maucDf){
        this.viewName = viewName;
        this.mauc = mauc;
        this.incMauc = incMauc;
        this.bfMauc = bfMauc;
        this.maucDf = maucDf;
    }

    public void setRegAndAuthData(String viewName, int monthRegStuCount, int monthAuthStuCount, int regStuCountDf, int authStuCountDf){
        this.viewName = viewName;
        this.monthRegStuCount = monthRegStuCount;
        this.monthAuthStuCount = monthAuthStuCount;
        this.regStuCountDf = regStuCountDf;
        this.authStuCountDf = authStuCountDf;
    }

    public void setFinHwData(String viewName, int finHwEq1StuCount, int finHwEq2StuCount, int finHwGte3StuCount){
        this.viewName = viewName;
        this.finHwEq1StuCount = finHwEq1StuCount;
        this.finHwEq2StuCount = finHwEq2StuCount;
        this.finHwGte3StuCount = finHwGte3StuCount;
    }

    public void setLmRateData(String viewName, int mauc, int lmMauc){
        this.viewName = viewName;
        this.mauc = mauc;
        this.lmMauc = lmMauc;
        this.lmDiff = mauc - lmMauc;
        this.lmRate = MathUtils.doubleDivide(mauc, lmMauc, 2, BigDecimal.ROUND_FLOOR);
    }

    public void setLowAndHighData(String viewName, int lowAnshEq1StuCount, int lowAnshGte2StuCount, int highAnshEq1StuCount, int highAnshGte2StuCount){
        this.viewName = viewName;
        this.lowAnshEq1StuCount = lowAnshEq1StuCount;
        this.lowAnshGte2StuCount = lowAnshGte2StuCount;
        this.highAnshEq1StuCount = highAnshEq1StuCount;
        this.highAnshGte2StuCount = highAnshGte2StuCount;
    }

    public void setAnshData(String viewName, int anshGte2StuCount, int anshGte2IncStuCount, int anshGte2BfStuCount, int anshGte2StuCountDf){
        this.viewName = viewName;
        this.anshGte2StuCount = anshGte2StuCount;
        this.anshGte2IncStuCount = anshGte2IncStuCount;
        this.anshGte2BfStuCount = anshGte2BfStuCount;
        this.anshGte2StuCountDf = anshGte2StuCountDf;
    }


    public Map<String, Object> generateDateMap(){
        Map<String, Object> retMap = new HashMap<>();
        retMap.put("id", this.id);
        retMap.put("idType", this.idType);
        retMap.put("name", this.name);
        retMap.put("viewType", this.viewType);
        retMap.put("viewName", this.viewName);
        retMap.put("tSemUnauthStuCount", this.thSemUnauthStuCount);
//        if(this.viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_MAUC || this.viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_INC_MAUC || this.viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_LTBF_MAUC
//                || this.viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_STBF_MAUC ){
//            retMap.put("mauc", this.mauc);
//            retMap.put("maucDf", this.maucDf);
//            retMap.put("maucBudget", this.maucBudget);
//            retMap.put("maucCompleteRate", this.maucCompleteRate);
//        }else if(this.viewType == 1){ //todo 小学注册认证
//            retMap.put("monthRegStuCount", this.monthRegStuCount);
//            retMap.put("monthAuthStuCount", this.monthAuthStuCount);
//            retMap.put("regStuCountDf", this.regStuCountDf);
//            retMap.put("authStuCountDf", this.authStuCountDf);
//        }else if(this.viewType == 1){  //todo 初高中全科
//            retMap.put("anshGte2StuCount", this.anshGte2StuCount);
//            retMap.put("anshGte2StuCountDf", this.anshGte2StuCountDf);
//            retMap.put("engHwGte3AuStuCount", this.engHwGte3AuStuCount);
//            retMap.put("engHwGte3AuStuCountDf", this.engHwGte3AuStuCountDf);
//        }

        if(this.viewType == PerformanceData.VIEW_TYPE_OVERVIEW_JUNIOR){  // 小学概览
            // 区域概况
            retMap.put("stuScale", this.stuScale);
            retMap.put("regStuCount", this.regStuCount);
            retMap.put("authStuCount", this.authStuCount);

            // 注册认证
            retMap.put("monthRegStuCount", this.monthRegStuCount);
            retMap.put("monthAuthStuCount", this.monthAuthStuCount);
            retMap.put("regStuCountDf", this.regStuCountDf);
            retMap.put("authStuCountDf", this.authStuCountDf);

            // 1套到3套
            retMap.put("finHwEq1StuCount", this.finHwEq1StuCount);
            retMap.put("finHwEq2StuCount", this.finHwEq2StuCount);
            retMap.put("finHwGte3StuCount", this.finHwGte3StuCount);

            // 月环比
            retMap.put("lmRateDataList", this.getLmRateDataList().stream().map(BeanMapUtils::tansBean2Map).collect(Collectors.toList()));
            // 目标达成情况
            retMap.put("completeDataList", this.dataItemList.stream().map(p -> p.generateData(1)).collect(Collectors.toList()));
        }else if(this.viewType == PerformanceData.VIEW_TYPE_OVERVIEW_MIDDLE){  // 初高中线上概览
            // 区域概况
            retMap.put("stuScale", this.stuScale);
            retMap.put("regStuCount", this.regStuCount);
            retMap.put("authStuCount", this.authStuCount);

            // 注册认证
            retMap.put("monthRegStuCount", this.monthRegStuCount);
            retMap.put("monthAuthStuCount", this.monthAuthStuCount);
            retMap.put("regStuCountDf", this.regStuCountDf);
            retMap.put("authStuCountDf", this.authStuCountDf);

            // 1套到3套
            retMap.put("finHwEq1StuCount", this.finHwEq1StuCount);
            retMap.put("finHwEq2StuCount", this.finHwEq2StuCount);
            retMap.put("finHwGte3StuCount", this.finHwGte3StuCount);

            retMap.put("completeDataList", this.dataItemList.stream().map(p -> p.generateData(2)).collect(Collectors.toList()));

        }else if(this.viewType == PerformanceData.VIEW_TYPE_OVERVIEW_MIDDLE_KLX){  // 初高中扫描概览
            retMap.put("stuScale", this.stuScale);
            retMap.put("stuKlxTnCount", this.stuKlxTnCount);

            // 低高标数据
            retMap.put("lowAnshEq1StuCount", this.lowAnshEq1StuCount);
            retMap.put("lowAnshGte2StuCount", this.lowAnshGte2StuCount);
            retMap.put("highAnshEq1StuCount", this.highAnshEq1StuCount);
            retMap.put("highAnshGte2StuCount", this.highAnshGte2StuCount);

            // 扫描数据
            retMap.put("completeDataList", this.dataItemList.stream().map(p -> p.generateData(3)).collect(Collectors.toList()));

        }

        //  小学完成各种渗透情形下的月活，新增，长回，短回数据
        else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_LP_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_MP_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_HP_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SP_SGLSUBJ_STBF_MAUC
                ){

            retMap.put("mauc", this.mauc);
            retMap.put("maucDf", this.maucDf);
            retMap.put("maucBudget", this.maucBudget);
            retMap.put("maucCompleteRate", this.maucCompleteRate);
        }

        //  小学完成各种渗透情形下的注册，认证数据
        else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_REG_AUTH ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_LP_REG_AUTH ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_MP_REG_AUTH ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_HP_REG_AUTH ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SP_REG_AUTH
                ){
            retMap.put("monthRegStuCount", this.monthRegStuCount);
            retMap.put("monthAuthStuCount", this.monthAuthStuCount);
            retMap.put("regStuCountDf", this.regStuCountDf);
            retMap.put("authStuCountDf", this.authStuCountDf);
        }

        //  小学完成各种渗透情形下的 1到3套 数据
        else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_PROCESS ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_LP_PROCESS ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_MP_PROCESS ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_HP_PROCESS ||
                viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_JUNIOR_SP_PROCESS
                ){
            retMap.put("finHwEq1StuCount", this.finHwEq1StuCount);
            retMap.put("finHwEq2StuCount", this.finHwEq2StuCount);
            retMap.put("finHwGte3StuCount", this.finHwGte3StuCount);
        }


        // 小学月环比各种渗透情形下的 月活，新增，长回，短回数据
        else if(viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_LP_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_MP_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_HP_SGLSUBJ_STBF_MAUC ||

                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_INC_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_LTBF_MAUC ||
                viewType == PerformanceData.VIEW_TYPE_LM_JUNIOR_SP_SGLSUBJ_STBF_MAUC

                ){

            retMap.put("mauc", this.mauc);
            retMap.put("lmMauc", this.lmMauc);
            retMap.put("lmDiff", this.lmDiff);
            retMap.put("lmRate", this.lmRate);
        }

        // 初高中线上英语月活， 注册认证， 1套到3套数据
        else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_MIDDLE_ENG_MAUC){
            retMap.put("mauc", this.mauc);
            retMap.put("incMauc", this.incMauc);
            retMap.put("bfMauc", this.bfMauc);
            retMap.put("maucDf", this.maucDf);
        }else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_MIDDLE_REG_AUTH){
            retMap.put("monthRegStuCount", this.monthRegStuCount);
            retMap.put("monthAuthStuCount", this.monthAuthStuCount);
            retMap.put("regStuCountDf", this.regStuCountDf);
            retMap.put("authStuCountDf", this.authStuCountDf);
        }else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_MIDDLE_PROCESS){
            retMap.put("finHwEq1StuCount", this.finHwEq1StuCount);
            retMap.put("finHwEq2StuCount", this.finHwEq2StuCount);
            retMap.put("finHwGte3StuCount", this.finHwGte3StuCount);
        }

        // 初高中扫描低高标，扫描数据
        else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_MIDDLE_KLX_PROCESS){
            retMap.put("lowAnshEq1StuCount", this.lowAnshEq1StuCount);
            retMap.put("lowAnshGte2StuCount", this.lowAnshGte2StuCount);
            retMap.put("highAnshEq1StuCount", this.highAnshEq1StuCount);
            retMap.put("highAnshGte2StuCount", this.highAnshGte2StuCount);
        }else if(viewType == PerformanceData.VIEW_TYPE_PERFORMANCE_MIDDLE_KLX_ALL_SUBJ){
            retMap.put("anshGte2StuCount", this.anshGte2StuCount);
            retMap.put("anshGte2IncStuCount", this.anshGte2IncStuCount);
            retMap.put("anshGte2BfStuCount", this.anshGte2BfStuCount);
            retMap.put("anshGte2StuCountDf", this.anshGte2StuCountDf);
        }














        else if(this.viewType == PerformanceData.VIET_TYPE_SCHOOL_LIST_JUNIOR){
            retMap.put("stuScale", this.stuScale);
            retMap.put("regStuCount", this.regStuCount);
            retMap.put("authStuCount", this.authStuCount);
            // 单科月活
            retMap.put("mauc", this.mauc);
            retMap.put("maucDf", this.maucDf);

            // 昨日注册
            retMap.put("regStuCountDf", this.regStuCountDf);
        }else if(this.viewType == PerformanceData.VIET_TYPE_SCHOOL_LIST_MIDDLE){
            retMap.put("stuScale", this.stuScale);
            retMap.put("stuKlxTnCount", this.stuKlxTnCount);
            // 全科扫描
            retMap.put("anshGte2StuCount", this.anshGte2StuCount);
            retMap.put("anshGte2StuCountDf", this.anshGte2StuCountDf);
            // 英语月活
            retMap.put("engHwGte3AuStuCount", this.engHwGte3AuStuCount);
            retMap.put("engHwGte3AuStuCountDf", this.engHwGte3AuStuCountDf);
        }
        return retMap;
    }

}
