package com.voxlearning.utopia.agent.bean.workrecord;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chunlin.yu
 * @create 2018-01-23 17:39
 **/
@Setter
@Getter
public class IntoSchoolStatisticsItem extends WorkRecordStatisticsItem {

    public IntoSchoolStatisticsItem(Long id,Integer idType,String name,Double perCapitaIntoSchool,
                                    Double visitSchoolAvgTeaCount,Double visitEngTeaPercent,Double visitMathTeaPercent){
        super(id,idType,name);
        setPerCapitaIntoSchool(perCapitaIntoSchool);
        setVisitSchoolAvgTeaCount(visitSchoolAvgTeaCount);
        setVisitEngTeaPercent(visitEngTeaPercent);
        setVisitMathTeaPercent(visitMathTeaPercent);
    }

    public IntoSchoolStatisticsItem(Long id,Integer idType,String name,Double perCapitaIntoSchool,
                                    Double visitSchoolAvgTeaCount,Double visitEngTeaPercent,Double visitMathTeaPercent,Double userVisitAndAssignHwTeaPct,Double workload){
        super(id,idType,name);
        setPerCapitaIntoSchool(perCapitaIntoSchool);
        setVisitSchoolAvgTeaCount(visitSchoolAvgTeaCount);
        setVisitEngTeaPercent(visitEngTeaPercent);
        setVisitMathTeaPercent(visitMathTeaPercent);
        setUserVisitAndAssignHwTeaPct(userVisitAndAssignHwTeaPct);
        setWorkload(workload);
    }

    /**
     * 人均日均进校次数
     */
    private Double perCapitaIntoSchool;

    /**
     * 校均拜访老师数
     */
    private Double visitSchoolAvgTeaCount;

    /**
     * 拜访英语老师占比
     */
    private Double visitEngTeaPercent;

    /**
     * 拜访的数学老师占比
     */
    private Double visitMathTeaPercent;

    /**
     * 作业布置率
     */
    private Double userVisitAndAssignHwTeaPct;

    /**
     * 工作量
     */
    private Double workload;

}
