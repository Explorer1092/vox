package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * offline模式学校数据报告
 *
 * @author deliang.che
 * @date 2018-03-17
 **/
@Getter
@Setter
public class SchoolOfflineReportData extends SchoolBaseReportData implements Serializable,ExportAble {
    private int stuScale;
    private int klxTnCount;              //快乐学考号数
    private int tmScanTpTeaCount;        //本月扫描试卷老师数
    private int tmFinTpEqStuCount;       //普通扫描≥1次学生数
    private int tmFinTpGte3StuCount;     //普通扫描≥3次学生数
    private int lmFinTpEqStuCount;       //上月普通扫描≥1次学生数
    private int lmFinTpGte3StuCount;     //上月普通扫描≥3次学生数
    private int tmFinMathBgExamStuCount; //本月数学大考扫描学生数
    private int tmFinEngBgExamStuCount;  //本月英语大考扫描学生数
    private int tmFinChnBgExamStuCount;  //本月语文大考扫描学生数
    private int tmFinPhyBgExamStuCount;  //本月物理大考扫描学生数
    private int tmFinCheBgExamStuCount;  //本月化学大考扫描学生数
    private int tmFinBiolBgExamStuCount; //本月生物大考扫描学生数
    private int tmFinPolBgExamStuCount;  //本月政治大考扫描学生数
    private int tmFinHistBgExamStuCount; //本月历史大考扫描学生数
    private int tmFinGeogBgExamStuCount; //本月地理大考扫描学生数
    private Date latestVisitTime;        //最近拜访日期
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
        result.add(null != getSchoolPopularity()?getSchoolPopularity().getLevel():"");
        result.add(getStuScale());
        result.add(getKlxTnCount());
        result.add(getTmScanTpTeaCount());
        result.add(getTmFinTpEqStuCount());
        result.add(getTmFinTpGte3StuCount());
        result.add(getLmFinTpEqStuCount());
        result.add(getLmFinTpGte3StuCount());
        result.add(getTmFinMathBgExamStuCount());
        result.add(getTmFinEngBgExamStuCount());
        result.add(getTmFinChnBgExamStuCount());
        result.add(getTmFinPhyBgExamStuCount());
        result.add(getTmFinCheBgExamStuCount());
        result.add(getTmFinBiolBgExamStuCount());
        result.add(getTmFinPolBgExamStuCount());
        result.add(getTmFinHistBgExamStuCount());
        result.add(getTmFinGeogBgExamStuCount());
        result.add(null != latestVisitTime ? DateUtils.dateToString(latestVisitTime,"yyyy-MM-dd"):"");
        return result;
    }
}
