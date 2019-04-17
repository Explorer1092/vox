package com.voxlearning.utopia.agent.view.indicator;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.indicator.ParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.school.SchoolParentIndicator;
import com.voxlearning.utopia.agent.bean.indicator.sum.SumParentIndicator;
import com.voxlearning.utopia.agent.constants.AgentConstants;
import com.voxlearning.utopia.agent.utils.MathUtils;

import java.util.HashMap;
import java.util.Map;
import java.util.Objects;

/**
 * ParentIndicatorViewSupport
 *
 * @author deliang.che
 * @since  2019/3/8
 */
public class ParentIndicatorViewSupport {

    /*
    code 定义规则：
    1.第一位表示小学：  01：小学 （目前只支持小学）
    2. 第二三位表示指标：  01：绑定  02：活跃
    3. 第四五位表示周期：  01：本月  02：昨日
     */
    public static final int PARENT_BIND_TM = 10101;                       // 家长-绑定-本月
    public static final int PARENT_BIND_PD = 10102;                       // 家长-绑定-昨日

    public static final int PARENT_ACTIVE_TM = 10201;                       // 家长-活跃-本月
    public static final int PARENT_ACTIVE_PD = 10202;                       // 家长-活跃-昨日



    public static ParentIndicatorView generateParentViewData(SumParentIndicator parentIndicator, Integer viewType){
        if(parentIndicator == null){
            return null;
        }
        ParentIndicatorView data = new ParentIndicatorView();
        data.setId(parentIndicator.getId());
        data.setIdType(parentIndicator.getIdType());
        data.setSchoolLevel(parentIndicator.getSchoolLevel());
        data.setName(parentIndicator.getName());

        data.setViewType(viewType);

        int headCount = parentIndicator.getHeadCount();

        if(viewType == PARENT_BIND_TM){
            data.setViewName("家长-绑定-本月");
            data.setTmBindStuParentNum(SafeConverter.toInt(parentIndicator.fetchMonthData().getBindStuParentNum()));
            data.setTmBindParentStuNum(SafeConverter.toInt(parentIndicator.fetchMonthData().getBindParentStuNum()));
            // 人均绑定家长
            if (Objects.equals(parentIndicator.getIdType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapitaNum(MathUtils.doubleDivide(SafeConverter.toInt(parentIndicator.fetchMonthData().getBindStuParentNum()), headCount, 0));
            }
        }else if (viewType == PARENT_BIND_PD){
            data.setViewName("家长-绑定-昨日");
            data.setPdBindStuParentNum(SafeConverter.toInt(parentIndicator.fetchDayData().getBindStuParentNum()));
            data.setPdBindParentStuNum(SafeConverter.toInt(parentIndicator.fetchDayData().getBindParentStuNum()));
            // 人均绑定家长
            if (Objects.equals(parentIndicator.getIdType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapitaNum(MathUtils.doubleDivide(SafeConverter.toInt(parentIndicator.fetchDayData().getBindStuParentNum()), headCount, 0));
            }
        }else if (viewType == PARENT_ACTIVE_TM){
            data.setViewName("家长-活跃-本月");
            data.setTmLoginGte1BindStuParentNum(SafeConverter.toInt(parentIndicator.fetchMonthData().getTmLoginGte1BindStuParentNum()));
            data.setTmLoginGte3BindStuParentNum(SafeConverter.toInt(parentIndicator.fetchMonthData().getTmLoginGte3BindStuParentNum()));
            data.setTmIncParentNum(SafeConverter.toInt(parentIndicator.fetchMonthData().getTmLoginGte3BindStuParentNum()) - SafeConverter.toInt(parentIndicator.fetchMonthData().getBackFlowParentNum()));
            data.setTmParentStuActiveSettlementNum(SafeConverter.toInt(parentIndicator.fetchMonthData().getParentStuActiveSettlementNum()));
            // 人均活跃一次
            if (Objects.equals(parentIndicator.getIdType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapitaNum(MathUtils.doubleDivide(SafeConverter.toInt(parentIndicator.fetchMonthData().getTmLoginGte1BindStuParentNum()), headCount, 0));
            }
        }else if (viewType == PARENT_ACTIVE_PD){
            data.setViewName("家长-活跃-本月");
            data.setPdLoginGte1BindStuParentNum(SafeConverter.toInt(parentIndicator.fetchDayData().getTmLoginGte1BindStuParentNum()));
            data.setPdLoginGte3BindStuParentNum(SafeConverter.toInt(parentIndicator.fetchDayData().getTmLoginGte3BindStuParentNum()));
            data.setPdIncParentNum(SafeConverter.toInt(parentIndicator.fetchDayData().getTmLoginGte3BindStuParentNum()) - SafeConverter.toInt(parentIndicator.fetchDayData().getBackFlowParentNum()));
            data.setPdParentStuActiveSettlementNum(SafeConverter.toInt(parentIndicator.fetchDayData().getParentStuActiveSettlementNum()));
            // 人均活跃一次
            if (Objects.equals(parentIndicator.getIdType(), AgentConstants.INDICATOR_TYPE_GROUP)) {
                data.setPerCapitaNum(MathUtils.doubleDivide(SafeConverter.toInt(parentIndicator.fetchDayData().getTmLoginGte1BindStuParentNum()), headCount, 0));
            }
        }
        return data;
    }

    public static Map<String, Object> generateSchoolParentViewData(SchoolParentIndicator schoolParentIndicator, int indicator, int monthOrDay) {
        if (schoolParentIndicator == null) {
            return null;
        }
        Map<String, Object> resultMap = new HashMap<>();
        resultMap.put("id", schoolParentIndicator.getSchoolId());
        int indicatorValue = 0;
        ParentIndicator parentIndicator = new ParentIndicator();
        if (monthOrDay == 1) {
            parentIndicator = schoolParentIndicator.fetchMonthData();
        } else if (monthOrDay == 2) {
            parentIndicator = schoolParentIndicator.fetchDayData();
        }

        if (indicator == 1) {
            indicatorValue = SafeConverter.toInt(parentIndicator.getBindStuParentNum());
        }else if (indicator == 2){
            indicatorValue = SafeConverter.toInt(parentIndicator.getBindParentStuNum());
        }else if (indicator == 3){
            indicatorValue = SafeConverter.toInt(parentIndicator.getTmLoginGte1BindStuParentNum());
        }else if (indicator == 4){
            indicatorValue = SafeConverter.toInt(parentIndicator.getTmLoginGte3BindStuParentNum());
        }else if (indicator == 5){
            indicatorValue = SafeConverter.toInt(parentIndicator.getParentStuActiveSettlementNum());
        }
        resultMap.put("indicatorValue", indicatorValue);
        return resultMap;
    }
}
