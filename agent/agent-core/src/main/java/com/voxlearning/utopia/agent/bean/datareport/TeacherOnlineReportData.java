package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * online模式老师数据报告
 *
 * @author chunlin.yu
 * @create 2018-03-05 14:04
 **/

@Getter
@Setter
public class TeacherOnlineReportData extends TeacherBaseReportData implements Serializable,ExportAble {
    private int clazzCount;                 //带班数量
    private int regStuCount;                //注册学生数
    private int auStuCount;                 //认证学生数
    private Date latestHwTime;              //上次布置作业日期
    private int tmHwSc;                     //本月布置所有作业套数
    private int csSettlementStuCount;       //累计新增
    private int tmFinCsHwEq1IncStuCount;    //新增1套
    private int tmFinCsHwEq2IncStuCount;    //新增2套
    private int tmFinCsHwGte3IncAuStuCount; //新增3套
    private int bfEq1StuCount;              //回流1套
    private int bfEq2StuCount;              //回流2套
    private int bfGte3StuCount;             //回流3套
    private int finCsHwGte3AuStuCount;      //本月月活
    private int lmFinCsHwGte3AuStuCount;    //上月月活

    private int vacnHwGroupCount;           //布置假期作业的班组数
    private int termReviewGroupCount;       //布置期末作业的班组数
    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(getDay());
        result.add(getChargePerson());
        result.add(getCityName());
        result.add(getCountyName());
        result.add(getSchoolId());
        result.add(getSchoolName());
        result.add(null != getSchoolLevel() ? getSchoolLevel().getDescription():"");
        result.add(getTeacherId());
        result.add(getTeacherName());
        result.add(getRegTime() !=null ? DateUtils.dateToString(getRegTime(),"yyyy-MM-dd"):"");
        result.add(getAuTime() !=null ? DateUtils.dateToString(getAuTime(),"yyyy-MM-dd"):"");
        result.add(getAuState() == 1?"是":"否");
        Subject subjectEnum = Subject.safeParse(getSubject());
        result.add(null != subjectEnum? subjectEnum.getValue():"");
        result.add(getClazzCount());
        result.add(getRegStuCount());
        result.add(getAuStuCount());
        result.add(getLatestHwTime() !=null ? DateUtils.dateToString(getLatestHwTime(),"yyyy-MM-dd"):"");
        result.add(getTmHwSc());
        result.add(csSettlementStuCount);
        result.add(tmFinCsHwEq1IncStuCount);
        result.add(tmFinCsHwEq2IncStuCount);
        result.add(tmFinCsHwGte3IncAuStuCount);
        result.add(bfEq1StuCount);
        result.add(bfEq2StuCount);
        result.add(bfGte3StuCount);
        result.add(finCsHwGte3AuStuCount);
        result.add(lmFinCsHwGte3AuStuCount);
        result.add(termReviewGroupCount > 0 ? "是" : "否");
        result.add(vacnHwGroupCount > 0 ? "是" : "否");
        return result;
    }
}
