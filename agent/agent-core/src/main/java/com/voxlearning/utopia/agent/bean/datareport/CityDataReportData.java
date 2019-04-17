package com.voxlearning.utopia.agent.bean.datareport;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.List;

/**
 * 城市数据报表内容 已将字段迁入DataReportField
 * Created by yaguang.wang on 2016/10/9.
 */
@Getter
@Setter
@NoArgsConstructor
@Deprecated
public class CityDataReportData extends ReportData {

    private static final long serialVersionUID = 5791785215468940190L;
    private String time;                // 时间分布
    private String regionName;          // 大区
    private String cityName;            // 城市
    private String countyName;          // 区
    private String businessDeveloper;   // 专员姓名
    private Integer studentNum;         // 学生基数
    private Integer authMathTeacherNum; // 累计认证数学老师数
    private Integer authEnglishTeacherNum;  //累计认证英语老师数
    private Integer activeEnglishTeacherNum;//认证活跃英语老师数
    private Integer activeMathTeacherNum;   //认证活跃数学老师数
    private Integer activeTeacherNum;   // 认证活跃老师数(本月不知过一次及以上制定作业)
    private Long registerStuNum;        // 学生新增注册数
    private Long authStuNum;            // 学生新增认证数
    private Integer totalRegStuNum;     // 学生累计注册数
    private Integer totalAuthStuNum;    // 学生累计认证数
    private Integer singBudget;         // 单科预算
    private Integer authStuSingActionNum;// 认证学生单科月活数
    private Double singBudgetPer;       // 单科预算完成率
    private Long singBackFlow;          // 本月单科回流
    private Integer doubleBudget;       // 双科预算
    private Integer authStuDoubleActive;// 认证学生双科月活数
    private Double doubleBudgetPer;     // 双科完成率
    private Long doubleBackFlow;        // 本月前双科回流
    private Integer authStuSingDayGrow; // 认证学生单科月活日浮
    private Integer authStuDoubleDayGrow;// 认证学生双科月活日浮
    private Integer authStuEngNum;      // 认证学生英语月活
    private Integer authStuMatNum;      // 认证学生数学月活

    @Override
    public List<Object> getExportAbleData() {
        return null;
    }
}
