package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * online模式学校数据报告
 *
 * @author chunlin.yu
 * @create 2018-03-03 13:07
 **/
@Getter
@Setter
public class SchoolOnlineReportData extends SchoolBaseReportData implements Serializable,ExportAble {

    private String maxPenetrateRateSglSubj;
    private String maxPenetrateRateEng;
    private String maxPenetrateRateMath;
    private String maxPenetrateRateChn;

    private int stuScale;                   //学生规模

    private int regTeaCount;                // 累计注册老师
    private int tmRegTeaCount;              // 本月注册老师
    private int tmAssignHwTeaCount;         // 本月新增使用老师

    private int regStuCount;                //累计注册学生数
    private int auStuCount;                 //累计认证学生数
    private int tmRegStuCount;              //本月注册学生数
    private int tmAuStuCount;               //本月认证学生数

    private int engSettlementStuCount;      //英语累计新增
    private int finEngHwEq1UnSettleStuCount;   //英语新增1套
    private int finEngHwEq2UnSettleStuCount;   //英语新增2套
    private int incSettlementEngStuCount;   //英语新增3套
    private int newEngTeaHwGte3UnSettleStuCount;   // 英语新老师新增3套
    private int oldEngTeaHwGte3UnSettleStuCount;   // 英语老老师新增3套
    private int finEngHwEq1SettleStuCount;           //英语回流1套
    private int finEngHwEq2SettleStuCount;           //英语回流2套
    private int finEngHwGte3SettleStuCount;          //英语回流3套
    private int finEngHwGte3AuStuCount;     //英语本月月活
    private int lmFinEngHwGte3AuStuCount;   //英语上月月活


    private int mathSettlementStuCount;      //数学累计新增
    private int finMathHwEq1UnSettleStuCount;   //数学新增1套
    private int finMathHwEq2UnSettleStuCount;   //数学新增2套
    private int incSettlementMathStuCount;   //数学新增3套
    private int newMathTeaHwGte3UnSettleStuCount;   // 数学新老师新增3套
    private int oldMathTeaHwGte3UnSettleStuCount;   // 数学老老师新增3套
    private int finMathHwEq1SettleStuCount;           //数学回流1套
    private int finMathHwEq2SettleStuCount;           //数学回流2套
    private int finMathHwGte3SettleStuCount;          //数学回流3套
    private int finMathHwGte3AuStuCount;     //数学本月月活
    private int lmFinMathHwGte3AuStuCount;   //数学上月月活


    private int chnSettlementStuCount;      //语文累计新增
    private int finChnHwEq1UnSettleStuCount;   //语文新增1套
    private int finChnHwEq2UnSettleStuCount;   //语文新增2套
    private int incSettlementChnStuCount;   //语文新增3套
    private int newChnTeaHwGte3UnSettleStuCount;   // 语文新老师新增3套
    private int oldChnTeaHwGte3UnSettleStuCount;   // 语文老老师新增3套
    private int finChnHwEq1SettleStuCount;           //语文回流1套
    private int finChnHwEq2SettleStuCount;           //语文回流2套
    private int finChnHwGte3SettleStuCount;          //语文回流3套
    private int finChnHwGte3AuStuCount;     //语文本月月活
    private int lmFinChnHwGte3AuStuCount;   //语文上月月活

    private Date latestVisitTime;           //上次拜访日期

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(getDay());
        result.add(getGroupName());
        result.add(getChargePerson());
        result.add(getCityName());
        result.add(getCountyName());
        result.add(getSchoolId());
        result.add(getSchoolName());
        result.add(null != getSchoolLevel() ? getSchoolLevel().getDescription():"");
        result.add(null != getEduSystemType()? getEduSystemType().getDescription():"");
        if (Objects.equals(getSchoolLevel(),SchoolLevel.JUNIOR) || Objects.equals(getSchoolLevel(),SchoolLevel.INFANT)){
            result.add(getEnglishStartGrade());
        }else {
            result.add("");
        }
        result.add(null != getSchoolPopularity()?getSchoolPopularity().getLevel():"");

        result.add(maxPenetrateRateSglSubj);
        result.add(maxPenetrateRateSglSubj);
        result.add(maxPenetrateRateSglSubj);
        result.add(maxPenetrateRateSglSubj);


        result.add(stuScale);

        result.add(regTeaCount);
        result.add(tmRegTeaCount);
        result.add(tmAssignHwTeaCount);

        result.add(regStuCount);
        result.add(auStuCount);
        result.add(tmRegStuCount);
        result.add(tmAuStuCount);

        result.add(engSettlementStuCount);
        result.add(finEngHwEq1UnSettleStuCount);
        result.add(finEngHwEq2UnSettleStuCount);
        result.add(incSettlementEngStuCount);
        result.add(newEngTeaHwGte3UnSettleStuCount);
        result.add(oldEngTeaHwGte3UnSettleStuCount);
        result.add(finEngHwEq1SettleStuCount);
        result.add(finEngHwEq2SettleStuCount);
        result.add(finEngHwGte3SettleStuCount);
        result.add(finEngHwGte3AuStuCount);
        result.add(lmFinEngHwGte3AuStuCount);

        result.add(mathSettlementStuCount);
        result.add(finMathHwEq1UnSettleStuCount);
        result.add(finMathHwEq2UnSettleStuCount);
        result.add(incSettlementMathStuCount);
        result.add(newMathTeaHwGte3UnSettleStuCount);
        result.add(oldMathTeaHwGte3UnSettleStuCount);
        result.add(finMathHwEq1SettleStuCount);
        result.add(finMathHwEq2SettleStuCount);
        result.add(finMathHwGte3SettleStuCount);
        result.add(finMathHwGte3AuStuCount);
        result.add(lmFinMathHwGte3AuStuCount);

        result.add(chnSettlementStuCount);
        result.add(finChnHwEq1UnSettleStuCount);
        result.add(finChnHwEq2UnSettleStuCount);
        result.add(incSettlementChnStuCount);
        result.add(newChnTeaHwGte3UnSettleStuCount);
        result.add(oldChnTeaHwGte3UnSettleStuCount);
        result.add(finChnHwEq1SettleStuCount);
        result.add(finChnHwEq2SettleStuCount);
        result.add(finChnHwGte3SettleStuCount);
        result.add(finChnHwGte3AuStuCount);
        result.add(lmFinChnHwGte3AuStuCount);

        result.add(null != latestVisitTime ? DateUtils.dateToString(latestVisitTime,"yyyy-MM-dd"):"");
        return result;
    }
}
