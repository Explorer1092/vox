package com.voxlearning.utopia.agent.support;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.indicator.OfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.OnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOfflineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolOnlineIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOfflineIndicatorWithBudget;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumOnlineIndicatorWithBudget;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.constants.AgentKpiType;
import com.voxlearning.utopia.agent.utils.MathUtils;
import com.voxlearning.utopia.agent.view.indicator.SumOfflineIndicatorView;
import com.voxlearning.utopia.agent.view.performance.Performance17ViewData;
import com.voxlearning.utopia.agent.view.performance.Performance17ViewDataItem;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * AgentIndicatorSupport
 *
 * @author song.wang
 * @date 2018/8/23
 */
public class AgentIndicatorSupport {



    /// code 定义规则：
    // 前两位表示中小学：  01：小学  02：初中  04：高中  24： 初高中
    // 第三四位表示指标：  01：概况  02：月活  03：新增  04：回流  05：留存  06：口测     11: 周测1套   12：周测2套
    // 第五六位表示学科：  00：全部  01：单科  02：英语  03：数学  04：语文                 （05：物理  06：化学  07：生物  08：历史  09：地理  10：政治）
    // 第七八位表示周期：  01：本月  02：昨日

    // 概览
    public static final int VIEW_TYPE_OVERVIEW_JUNIOR = 1000000;                           // 小学概览
    public static final int VIEW_TYPE_OVERVIEW_MIDDLE = 24000000;                           // 初高中概览

    // 小学注册认证
    public static final int VIEW_TYPE_JUNIOR_REG_AUTH_ALL_TM = 1010001;                       // 小学注册认证（本月）
    public static final int VIEW_TYPE_JUNIOR_REG_AUTH_ALL_PD = 1010002;                       // 小学注册认证（昨日）

    // 小学月活
    public static final int VIEW_TYPE_JUNIOR_MAUC_SGLSUBJ_TM = 1020101;                       // 小学月活-单科-本月
    public static final int VIEW_TYPE_JUNIOR_MAUC_ENG_TM = 1020201;                       // 小学月活-英语-本月
    public static final int VIEW_TYPE_JUNIOR_MAUC_MATH_TM = 1020301;                       // 小学月活-数学-本月
    public static final int VIEW_TYPE_JUNIOR_MAUC_CHN_TM = 1020401;                       // 小学月活-语文-本月

    // 小学新增
    public static final int VIEW_TYPE_JUNIOR_INC_ENG_TM = 1030201;                       // 小学新增-英语-本月
    public static final int VIEW_TYPE_JUNIOR_INC_ENG_PD = 1030202;                       // 小学新增-英语-昨日
    public static final int VIEW_TYPE_JUNIOR_INC_MATH_TM = 1030301;                       // 小学新增-数学-本月
    public static final int VIEW_TYPE_JUNIOR_INC_MATH_PD = 1030302;                       // 小学新增-数学-昨日
    public static final int VIEW_TYPE_JUNIOR_INC_CHN_TM = 1030401;                       // 小学新增-语文-本月
    public static final int VIEW_TYPE_JUNIOR_INC_CHN_PD = 1030402;                       // 小学新增-语文-昨日
    public static final int VIEW_TYPE_JUNIOR_INC_SGLSUBJ_TM = 1030101;                       // 小学新增-单科-本月
    public static final int VIEW_TYPE_JUNIOR_INC_SGLSUBJ_PD = 1030102;                       // 小学新增-单科-昨日


    // 小学留存
    public static final int VIEW_TYPE_JUNIOR_RT_ENG_TM = 1050201;                        // 小学留存-英语-本月
    public static final int VIEW_TYPE_JUNIOR_RT_MATH_TM = 1050301;                       // 小学留存-数学-本月
    public static final int VIEW_TYPE_JUNIOR_RT_CHN_TM = 1050401;                       // 小学留存-语文-本月
    public static final int VIEW_TYPE_JUNIOR_RT_SGLSUBJ_TM = 1050101;                       // 小学留存-单科-本月


    // 初高中概况
    public static final int VIEW_TYPE_MIDDLE_REG_AUTH_ALL_TM = 24010001;                       // 初高中概况（本月）
    public static final int VIEW_TYPE_MIDDLE_REG_AUTH_ALL_PD = 24010002;                       // 初高中概况（昨日）

    // 初高中月活
    public static final int VIEW_TYPE_MIDDLE_MAUC_SGLSUBJ_TM = 24020101;                       // 初高中月活-单科-本月
    public static final int VIEW_TYPE_MIDDLE_MAUC_ENG_TM = 24020201;                       // 初高中月活-英语-本月
    public static final int VIEW_TYPE_MIDDLE_MAUC_MATH_TM = 24020301;                       // 初高中月活-数学-本月

    // 初高中新增
    public static final int VIEW_TYPE_MIDDLE_INC_SGLSUBJ_TM = 24030101;                       // 初高中新增-单科-本月
    public static final int VIEW_TYPE_MIDDLE_INC_SGLSUBJ_PD = 24030102;                       // 初高中新增-单科-昨日
    public static final int VIEW_TYPE_MIDDLE_INC_ENG_TM = 24030201;                       // 初高中新增-英语-本月
    public static final int VIEW_TYPE_MIDDLE_INC_ENG_PD = 24030202;                       // 初高中新增-英语-昨日
    public static final int VIEW_TYPE_MIDDLE_INC_MATH_TM = 24030301;                       // 初高中新增-数学-本月
    public static final int VIEW_TYPE_MIDDLE_INC_MATH_PD = 24030302;                       // 初高中新增-数学-昨日

    // 初高中留存
    public static final int VIEW_TYPE_MIDDLE_RT_ENG_TM = 24050201;                        // 初高中留存-英语-本月
    public static final int VIEW_TYPE_MIDDLE_RT_MATH_TM = 24050301;                       // 初高中留存-数学-本月

    public static final int VIEW_TYPE_MIDDLE_SCAN_GTE1_TM = 24110001;                        // 初高中-周测1套-本月
    public static final int VIEW_TYPE_MIDDLE_SCAN_GTE1_PD = 24110002;                        // 初高中-周测1套-昨日

    public static final int VIEW_TYPE_MIDDLE_SCAN_GTE2_TM = 24120001;                        // 初高中-周测2套-本月
    public static final int VIEW_TYPE_MIDDLE_SCAN_GTE2_PD = 24120002;                        // 初高中-周测2套-昨日



    public static Performance17ViewData generateOnlineViewData(SumOnlineIndicatorWithBudget indicatorWithBudget, Integer viewType){
        if(indicatorWithBudget == null){
            return null;
        }
        Performance17ViewData data = new Performance17ViewData();
        data.setId(indicatorWithBudget.getId());
        data.setIdType(String.valueOf(indicatorWithBudget.getDataType()));
        data.setSchoolLevel(indicatorWithBudget.getSchoolLevel());
        data.setName(indicatorWithBudget.getName());

        data.setViewType(viewType);

        int headCount = indicatorWithBudget.getHeadCount();

        if(viewType == VIEW_TYPE_OVERVIEW_JUNIOR || viewType == VIEW_TYPE_OVERVIEW_MIDDLE){
            if(viewType == VIEW_TYPE_OVERVIEW_JUNIOR){
                data.setViewName("小学作业");
            }else {
                data.setViewName("中学作业");
            }


            // 注册认证
            data.setStuScale(SafeConverter.toInt(indicatorWithBudget.fetchSumData().getStuScale()));
            data.setRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchSumData().getRegStuCount()));
            data.setAuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchSumData().getAuStuCount()));

            data.setTmRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getRegStuCount()));
            data.setTmAuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getAuStuCount()));

            data.setPdRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getRegStuCount()));
            data.setPdAuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getAuStuCount()));

            if (viewType == VIEW_TYPE_OVERVIEW_MIDDLE){
                data.setTmPromoteRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getPromoteRegStuCount()));//本月学生注册数（升学）
                data.setPdPromoteRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getPromoteRegStuCount()));  //昨日学生注册数（升学）
            }

            // 月活部分
            data.setTmLoginStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getLoginStuCount()));
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1StuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte3AuStuCount()));

//            //  默认情况下基数取学期维度“分科目认证3套月活”
//            //  8/9月份取5月分科目认证3套月活
//            if(viewType == VIEW_TYPE_OVERVIEW_JUNIOR){
//                data.setEngMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte3SettleStuCount()));
//                data.setEngMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte3SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinEngHwGte3AuStuCount())));
//                data.setMathMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte3SettleStuCount()));
//                data.setMathMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte3SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinMathHwGte3AuStuCount())));
//                data.setChnMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinChnHwGte3SettleStuCount()));
//                data.setChnMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinChnHwGte3SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinChnHwGte3AuStuCount())));
//            }else {
//                //  中学留存默认情况下基数取学期维度“学生完成1套及以上作业数”
//                //  留存数取月维度的“已新增结算学生完成1套及以上作业数”
//                //  8/9月份取5月分科目认证3套月活
//                data.setEngMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1SettleStuCount()));
//                data.setEngMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinEngHwGte1AuStuCount())));
//                data.setMathMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1SettleStuCount()));
//                data.setMathMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinMathHwGte1AuStuCount())));
//            }

            //留存率
            if (viewType == VIEW_TYPE_OVERVIEW_MIDDLE){
                Performance17ViewDataItem engBfItem = new Performance17ViewDataItem();
                engBfItem.setName("英语");
                engBfItem.setMrtRate1(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinEngHwGte1AuStuCount())));
                engBfItem.setMrtRate2(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte3SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinEngHwGte3AuStuCount())));
                data.getBfItemList().add(engBfItem);

                Performance17ViewDataItem mathBfItem = new Performance17ViewDataItem();
                mathBfItem.setName("数学");
                mathBfItem.setMrtRate1(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinMathHwGte1AuStuCount())));
                mathBfItem.setMrtRate2(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte3SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinMathHwGte3AuStuCount())));
                data.getBfItemList().add(mathBfItem);
            }
            if(viewType == VIEW_TYPE_OVERVIEW_JUNIOR) {
                Performance17ViewDataItem chnBfItem = new Performance17ViewDataItem();
                chnBfItem.setName("单科");
                chnBfItem.setMrtRate1(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1AuStuCount())));
                chnBfItem.setMrtRate2(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumSglSubj()), SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinSglSubjHwGte3AuStuCount())));
                data.getBfItemList().add(chnBfItem);
            }


            // 新增月活
            if (viewType == VIEW_TYPE_OVERVIEW_MIDDLE){
                Performance17ViewDataItem engItem = new Performance17ViewDataItem();
                engItem.setName("英语");
                engItem.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1UnSettleStuCount()));
                engItem.setPdFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinEngHwGte1UnSettleStuCount()));
                engItem.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementEngStuCount()));
                engItem.setPdFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getIncSettlementEngStuCount()));
                AgentKpiType engKpiType = AgentKpiType.MIDDLE_ENG_ADD;
                Integer engBudget = indicatorWithBudget.getKpiBudgetMap().get(engKpiType) == null ? 0 : indicatorWithBudget.getKpiBudgetMap().get(engKpiType);
                engItem.setBudget(engBudget);
                engItem.setCompleteRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementEngStuCount()), engBudget));
                data.getIncItemList().add(engItem);

                Performance17ViewDataItem mathItem = new Performance17ViewDataItem();
                mathItem.setName("数学");
                mathItem.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1UnSettleStuCount()));
                mathItem.setPdFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinMathHwGte1UnSettleStuCount()));
                mathItem.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementMathStuCount()));
                mathItem.setPdFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getIncSettlementMathStuCount()));
                AgentKpiType mathKpiType = AgentKpiType.MIDDLE_MATH_ADD;
                Integer mathBudget = indicatorWithBudget.getKpiBudgetMap().get(mathKpiType) == null ? 0 : indicatorWithBudget.getKpiBudgetMap().get(mathKpiType);
                mathItem.setBudget(mathBudget);
                mathItem.setCompleteRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementMathStuCount()), mathBudget));
                data.getIncItemList().add(mathItem);
            }
            if(viewType == VIEW_TYPE_OVERVIEW_JUNIOR){
                Performance17ViewDataItem sglSubjItem = new Performance17ViewDataItem();
                sglSubjItem.setName("单科");
                sglSubjItem.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1UnSettleStuCount()));
                sglSubjItem.setPdFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinSglSubjHwGte1UnSettleStuCount()));
                sglSubjItem.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementSglSubjStuCount()));
                sglSubjItem.setPdFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getIncSettlementSglSubjStuCount()));
                AgentKpiType kpiType = AgentKpiType.JUNIOR_SGL_SUBJ_ADD;
                Integer sglSubjBudget = indicatorWithBudget.getKpiBudgetMap().get(kpiType) == null ? 0 : indicatorWithBudget.getKpiBudgetMap().get(kpiType);
                sglSubjItem.setBudget(sglSubjBudget);
                sglSubjItem.setCompleteRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementSglSubjStuCount()), sglSubjBudget));
                data.getIncItemList().add(sglSubjItem);
            }

        }else if(viewType == VIEW_TYPE_JUNIOR_REG_AUTH_ALL_TM || viewType == VIEW_TYPE_MIDDLE_REG_AUTH_ALL_TM) {     // 小学及初高中本月注册认证
            data.setViewName("本月注册认证");
            data.setTmRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getRegStuCount()));
            data.setTmAuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getAuStuCount()));
            if (viewType == VIEW_TYPE_JUNIOR_REG_AUTH_ALL_TM){
                // 人均注册
                if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                    data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getRegStuCount()), headCount, 0));
                }
            }else if (viewType == VIEW_TYPE_MIDDLE_REG_AUTH_ALL_TM){
                data.setTmPromoteRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getPromoteRegStuCount()));//本月学生注册数（升学）
                // 人均注册
                if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                    data.setPerCapita(MathUtils.doubleDivide(data.getTmRegStuCount() + data.getTmPromoteRegStuCount(), headCount, 0));
                }
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_REG_AUTH_ALL_PD || viewType == VIEW_TYPE_MIDDLE_REG_AUTH_ALL_PD){     // 小学及初高中昨日注册认证
            data.setViewName("昨日注册认证");
            data.setPdRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getRegStuCount()));
            data.setPdAuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getAuStuCount()));
            if (viewType == VIEW_TYPE_JUNIOR_REG_AUTH_ALL_PD){
                // 人均注册
                if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                    data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getRegStuCount()), headCount, 0));
                }
            }else if (viewType == VIEW_TYPE_MIDDLE_REG_AUTH_ALL_PD){
                data.setPdPromoteRegStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getPromoteRegStuCount()));  //昨日学生注册数（升学）
                // 人均注册
                if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                    data.setPerCapita(MathUtils.doubleDivide(data.getPdRegStuCount() + data.getPdPromoteRegStuCount(), headCount, 0));
                }
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_MAUC_SGLSUBJ_TM || viewType == VIEW_TYPE_MIDDLE_MAUC_SGLSUBJ_TM){
            data.setViewName("月活-单科-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1StuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte3StuCount()));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1StuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_MAUC_ENG_TM || viewType == VIEW_TYPE_MIDDLE_MAUC_ENG_TM){     // 小学及初高中
            data.setViewName("月活-英语-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1StuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte3StuCount()));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1StuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_MAUC_MATH_TM || viewType == VIEW_TYPE_MIDDLE_MAUC_MATH_TM) {  // 小学及初高中
            data.setViewName("月活-数学-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1StuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte3StuCount()));
            // 人均1套
            if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1StuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_MAUC_CHN_TM){
            data.setViewName("月活-语文-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinChnHwGte1StuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinChnHwGte3StuCount()));
            // 人均1套
            if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinChnHwGte1StuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_ENG_TM || viewType == VIEW_TYPE_MIDDLE_INC_ENG_TM){     // 小学及初高中
            data.setViewName("新增-英语-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1UnSettleStuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementEngStuCount()));
            AgentKpiType kpiType = viewType == VIEW_TYPE_JUNIOR_INC_ENG_TM ? AgentKpiType.JUNIOR_ENG_ADD : AgentKpiType.MIDDLE_ENG_ADD;
            if (viewType == VIEW_TYPE_MIDDLE_INC_ENG_TM){
                Integer budget = indicatorWithBudget.getKpiBudgetMap().get(kpiType) == null ? 0 : indicatorWithBudget.getKpiBudgetMap().get(kpiType);
                data.setBudget(budget);
                data.setCompleteRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementEngStuCount()), budget));
            }
            // 人均1套
            if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_ENG_PD || viewType == VIEW_TYPE_MIDDLE_INC_ENG_PD){     // 小学及初高中
            data.setViewName("新增-英语-昨日");
            data.setPdFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinEngHwGte1UnSettleStuCount()));
            data.setPdFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getIncSettlementEngStuCount()));
            // 人均1套
            if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinEngHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_MATH_TM || viewType == VIEW_TYPE_MIDDLE_INC_MATH_TM){     // 小学及初高中
            data.setViewName("新增-数学-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1UnSettleStuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementMathStuCount()));
            AgentKpiType kpiType = viewType == VIEW_TYPE_JUNIOR_INC_MATH_TM ? AgentKpiType.JUNIOR_MATH_ADD : AgentKpiType.MIDDLE_MATH_ADD;
            if (viewType == VIEW_TYPE_MIDDLE_INC_MATH_TM){
                Integer budget = indicatorWithBudget.getKpiBudgetMap().get(kpiType) == null ? 0 : indicatorWithBudget.getKpiBudgetMap().get(kpiType);
                data.setBudget(budget);
                data.setCompleteRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementMathStuCount()), budget));
            }
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_MATH_PD || viewType == VIEW_TYPE_MIDDLE_INC_MATH_PD){     // 小学及初高中
            data.setViewName("新增-数学-昨日");
            data.setPdFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinMathHwGte1UnSettleStuCount()));
            data.setPdFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getIncSettlementMathStuCount()));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinMathHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_CHN_TM){   // 小学语文
            data.setViewName("新增-语文-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinChnHwGte1UnSettleStuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementChnStuCount()));
            AgentKpiType kpiType = AgentKpiType.JUNIOR_CHN_ADD;
//            Integer budget = indicatorWithBudget.getKpiBudgetMap().get(kpiType) == null ? 0 : indicatorWithBudget.getKpiBudgetMap().get(kpiType);
//            data.setBudget(budget);
//            data.setCompleteRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementChnStuCount()), budget));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinChnHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_CHN_PD) {
            data.setViewName("新增-语文-昨日");
            data.setPdFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinChnHwGte1UnSettleStuCount()));
            data.setPdFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getIncSettlementChnStuCount()));
            // 人均1套
            if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinChnHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_SGLSUBJ_TM || viewType == VIEW_TYPE_MIDDLE_INC_SGLSUBJ_TM){  // 小学、初高中单科-本月
            data.setViewName("新增-单科-本月");
            data.setTmFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1UnSettleStuCount()));
            data.setTmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementSglSubjStuCount()));
            AgentKpiType kpiType = viewType == VIEW_TYPE_JUNIOR_INC_SGLSUBJ_TM ? AgentKpiType.JUNIOR_SGL_SUBJ_ADD : AgentKpiType.MIDDLE_SGL_SUBJ_ADD;
            Integer budget = indicatorWithBudget.getKpiBudgetMap().get(kpiType) == null ? 0 : indicatorWithBudget.getKpiBudgetMap().get(kpiType);
            data.setBudget(budget);
            data.setCompleteRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getIncSettlementSglSubjStuCount()), budget));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinSglSubjHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_INC_SGLSUBJ_PD || viewType == VIEW_TYPE_MIDDLE_INC_SGLSUBJ_PD) { //小学、初高中单科-昨日
            data.setViewName("新增-单科-昨日");
            data.setPdFinHwGte1StuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinSglSubjHwGte1UnSettleStuCount()));
            data.setPdFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getIncSettlementSglSubjStuCount()));
            // 人均1套
            if (Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getFinSglSubjHwGte1UnSettleStuCount()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_JUNIOR_RT_ENG_TM){
            data.setViewName("留存-英语");
            //  默认情况下基数取学期维度“分科目认证3套月活”
            //  8/9月份取5月分科目认证3套月活

            //3套留存率:分子(本月分科目的3套回流:returnSettleNum_)/分母(近7个月的最大科目月活:baseFin_HwGte3AuStuCount)
            data.setLmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinEngHwGte3AuStuCount()));
            data.setMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumEng()));
            data.setMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumEng()), SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinEngHwGte3AuStuCount())));
        }else if(viewType == VIEW_TYPE_MIDDLE_RT_ENG_TM){
            data.setViewName("留存-英语");
            //  中学留存默认情况下基数取学期维度“学生完成1套及以上作业数”
            //  留存数取月维度的“已新增结算学生完成1套及以上作业数”
            //  8/9月份取5月分科目认证3套月活
            data.setLmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinEngHwGte1AuStuCount()));
            data.setMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1SettleStuCount()));
            data.setMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinEngHwGte1SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinEngHwGte1AuStuCount())));
        }else if(viewType == VIEW_TYPE_JUNIOR_RT_MATH_TM){
            data.setViewName("留存-数学");
            //  默认情况下基数取学期维度“分科目认证3套月活”
            //  8/9月份取5月分科目认证3套月活

            //3套留存率:分子(本月分科目的3套回流:returnSettleNum_)/分母(近7个月的最大科目月活:baseFin_HwGte3AuStuCount)
            data.setLmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinMathHwGte3AuStuCount()));
            data.setMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumMath()));
            data.setMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumMath()), SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinMathHwGte3AuStuCount())));
        }else if(viewType == VIEW_TYPE_MIDDLE_RT_MATH_TM){
            data.setViewName("留存-数学");
            //  中学留存默认情况下基数取学期维度“学生完成1套及以上作业数”
            //  留存数取月维度的“已新增结算学生完成1套及以上作业数”
            //  8/9月份取5月分科目认证3套月活
            data.setLmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinMathHwGte1AuStuCount()));
            data.setMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1SettleStuCount()));
            data.setMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getFinMathHwGte1SettleStuCount()), SafeConverter.toInt(indicatorWithBudget.fetchTermData().getFinMathHwGte1AuStuCount())));
        }else if(viewType == VIEW_TYPE_JUNIOR_RT_CHN_TM) {
            data.setViewName("留存-语文");
            //  默认情况下基数取学期维度“分科目认证3套月活”
            //  8/9月份取5月分科目认证3套月活

            //3套留存率:分子(本月分科目的3套回流:returnSettleNum_)/分母(近7个月的最大科目月活:baseFin_HwGte3AuStuCount)
            data.setLmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinChnHwGte3AuStuCount()));
            data.setMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumChn()));
            data.setMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumChn()), SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinChnHwGte3AuStuCount())));
        }else if(viewType == VIEW_TYPE_JUNIOR_RT_SGLSUBJ_TM) {
            data.setViewName("留存-单科");
            //  默认情况下基数取学期维度“分科目认证3套月活”
            //  8/9月份取5月分科目认证3套月活

            //3套留存率:分子(本月分科目的3套回流:returnSettleNum_)/分母(近7个月的最大科目月活:baseFin_HwGte3AuStuCount)
            data.setLmFinHwGte3AuStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinSglSubjHwGte3AuStuCount()));
            data.setMrtStuCount(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumSglSubj()));
            data.setMrtRate(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getReturnSettleNumSglSubj()), SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getBaseFinSglSubjHwGte3AuStuCount())));
        }
        return data;
    }

    /**
     *
     * @param schoolOnlineIndicator 学校指标数据
     * @param indicator  01: 新增注册， 02：新增1套， 03：新增3套，04：回流1套 05：回流3套
     * @param subject  00：全部  01：单科  02：英语  03：数学  04：语文
     * @param monthOrDay  01：本月  02：昨日
     * @param schoolLevelFlag 学校阶段   1:小学   24：初高中
     * @return
     */
    public static Map<String, Object> generateSchoolOnlineViewData(SchoolOnlineIndicator schoolOnlineIndicator, int indicator, int subject, int monthOrDay, int schoolLevelFlag){
        if(schoolOnlineIndicator == null){
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", schoolOnlineIndicator.getSchoolId());
        int indicatorValue = 0;
        OnlineIndicator onlineIndicator = new OnlineIndicator();
        if(monthOrDay == 1){
            onlineIndicator = schoolOnlineIndicator.fetchMonthData();
        }else if(monthOrDay == 2){
            onlineIndicator = schoolOnlineIndicator.fetchDayData();
        }

        if(indicator == 1){
            if (schoolLevelFlag == 1){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getRegStuCount());
            }else if (schoolLevelFlag == 24){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getRegStuCount()) + SafeConverter.toInt(onlineIndicator.getPromoteRegStuCount());
            }
        }else if(indicator == 2){
            if (subject == 1){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinSglSubjHwGte1UnSettleStuCount());
            }else if(subject == 2){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinEngHwGte1UnSettleStuCount());
            }else if(subject == 3){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinMathHwGte1UnSettleStuCount());
            }else if(subject == 4){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinChnHwGte1UnSettleStuCount());
            }
        }else if(indicator == 3){
            if (subject == 1){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getIncSettlementSglSubjStuCount());
            }else if(subject == 2){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getIncSettlementEngStuCount());
            }else if(subject == 3){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getIncSettlementMathStuCount());
            }else if(subject == 4){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getIncSettlementChnStuCount());
            }
        }else if(indicator == 4){
            if (subject == 1){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinSglSubjHwGte1SettleStuCount());
            }else if(subject == 2){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinEngHwGte1SettleStuCount());
            }else if(subject == 3){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinMathHwGte1SettleStuCount());
            }else if(subject == 4){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinChnHwGte1SettleStuCount());
            }
        }else if(indicator == 5){
            if (subject == 1){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinSglSubjHwGte3SettleStuCount());
            }else if(subject == 2){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinEngHwGte3SettleStuCount());
            }else if(subject == 3){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinMathHwGte3SettleStuCount());
            }else if(subject == 4){
                indicatorValue = SafeConverter.toInt(onlineIndicator.getFinChnHwGte3SettleStuCount());
            }
        }
        resultMap.put("indicatorValue", indicatorValue);
        return resultMap;
    }

    public static SumOfflineIndicatorView generateOfflineViewData(SumOfflineIndicatorWithBudget indicatorWithBudget, Integer viewType){
        if(indicatorWithBudget == null){
            return null;
        }
        SumOfflineIndicatorView data = new SumOfflineIndicatorView();
        data.setId(indicatorWithBudget.getId());
        data.setIdType(indicatorWithBudget.getDataType());
        data.setSchoolLevel(indicatorWithBudget.getSchoolLevel());
        data.setName(indicatorWithBudget.getName());

        data.setViewType(viewType);
        int headCount = indicatorWithBudget.getHeadCount();

        if(viewType == VIEW_TYPE_OVERVIEW_MIDDLE){
            data.setViewName("中学扫描");
            data.setKlxTotalNum(indicatorWithBudget.fetchSumData().getKlxTotalNum());
            data.setScanStuNum(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getScanTermStuNumSglSubj()));

            data.setTmGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementNumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementNumSglSubj()));
            data.setTmSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementNumSglSubj()));
            data.setTmUnSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementNumSglSubj()));

            data.setTmGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementGte2NumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementGte2NumSglSubj()));
            data.setTmSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementGte2NumSglSubj()));
            data.setTmUnSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementGte2NumSglSubj()));

            data.setTmScanStuNum(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getScanStuNumSglSubj()));


            data.setPdGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementNumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementNumSglSubj()));
            data.setPdSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementNumSglSubj()));
            data.setPdUnSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementNumSglSubj()));

            data.setPdGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementGte2NumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementGte2NumSglSubj()));
            data.setPdSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementGte2NumSglSubj()));
            data.setPdUnSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementGte2NumSglSubj()));

            data.setPdScanStuNum(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getScanStuNumSglSubj()));

        }else if(viewType == VIEW_TYPE_MIDDLE_SCAN_GTE1_TM){
            data.setViewName("中学-周测1套-本月");

            data.setTmGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementNumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementNumSglSubj()));
            data.setTmSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementNumSglSubj()));
            data.setTmUnSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementNumSglSubj()));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementNumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementNumSglSubj()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_MIDDLE_SCAN_GTE1_PD){
            data.setViewName("中学-周测1套-昨日");

            data.setPdGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementNumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementNumSglSubj()));
            data.setPdSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementNumSglSubj()));
            data.setPdUnSettlementGte1Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementNumSglSubj()));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementNumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementNumSglSubj()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_MIDDLE_SCAN_GTE2_TM){
            data.setViewName("中学-周测2套-本月");

            data.setTmGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementGte2NumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementGte2NumSglSubj()));
            data.setTmSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementGte2NumSglSubj()));
            data.setTmUnSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementGte2NumSglSubj()));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getSettlementGte2NumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchMonthData().getUnsettlementGte2NumSglSubj()), headCount, 0));
            }
        }else if(viewType == VIEW_TYPE_MIDDLE_SCAN_GTE2_PD){
            data.setViewName("中学-周测2套-昨日");

            data.setPdGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementGte2NumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementGte2NumSglSubj()));
            data.setPdSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementGte2NumSglSubj()));
            data.setPdUnSettlementGte2Num(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementGte2NumSglSubj()));
            // 人均1套
            if(Objects.equals(indicatorWithBudget.getDataType(), AgentConstants.INDICATOR_TYPE_GROUP)){
                data.setPerCapita(MathUtils.doubleDivide(SafeConverter.toInt(indicatorWithBudget.fetchDayData().getSettlementGte2NumSglSubj()) +  SafeConverter.toInt(indicatorWithBudget.fetchDayData().getUnsettlementGte2NumSglSubj()), headCount, 0));
            }
        }

        return data;
    }

    /**
     * 获取学校扫描指标
     * @param schoolIndicator 学校指标数据 (offline)
     * @param indicator  11: 周测1套， 12:周测2套
     * @param monthOrDay  01：本月  02：昨日
     * @return 学校的扫描指标
     */
    public static Map<String, Object> generateSchoolOfflineViewData(SchoolOfflineIndicator schoolIndicator, int indicator, int monthOrDay){
        if(schoolIndicator == null){
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", schoolIndicator.getSchoolId());
        int indicatorValue = 0;
        OfflineIndicator offlineIndicator = new OfflineIndicator();
        if(monthOrDay == 1){
            offlineIndicator = schoolIndicator.fetchMonthData();
        }else if(monthOrDay == 2){
            offlineIndicator = schoolIndicator.fetchDayData();
        }

        if(indicator == 11){
            indicatorValue = SafeConverter.toInt(offlineIndicator.getSettlementNumSglSubj()) + SafeConverter.toInt(offlineIndicator.getUnsettlementNumSglSubj());
        }else if(indicator == 12){
            indicatorValue = SafeConverter.toInt(offlineIndicator.getSettlementGte2NumSglSubj()) + SafeConverter.toInt(offlineIndicator.getUnsettlementGte2NumSglSubj());
        }
        resultMap.put("indicatorValue", indicatorValue);
        return resultMap;
    }

}
