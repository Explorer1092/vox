package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * @author chunlin.yu
 * @create 2018-03-05 19:20
 **/
@Getter
@Setter
public class GroupOnlineReportData extends GroupBaseReportData implements ExportAble{
    private int regStuCount;                        // 注册学生数
    private int auStuCount;                         // 认证学生数
    private Date latestHwTime;                      // 上次布置作业日期
    private int tmHwSc;                             // 本月布置所有作业套数
    private int finCsHwEq1StuCount;                 // 本月完成1套当前科目作业学生数
    private int finCsHwEq2StuCount;                 // 本月完成2套当前科目作业学生数
    private int finCsHwGte3AuStuCount;              // 本月完成3套及以上当前科目作业认证学生数
    private int lmFinCsHwGte3AuStuCount;
    private int increaseSpaceOfCsSettlementStuCount;//新增空间
    private boolean vacnHwFlag;                     // 是否已布置寒假作业
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
        ClazzLevel parse = null;
        if (null != getClazzLevel()){
            parse = ClazzLevel.parse(getClazzLevel());
        }
        result.add(null != parse?parse.getDescription():"");
        result.add(getClazzName());
        Subject subjectEnum = Subject.safeParse(this.getSubject());
        result.add(null != subjectEnum? subjectEnum.getValue():"");
        result.add(getTeacherId());
        result.add(getTeacherName());
        result.add(getAuState() == 1?"是":"否");
        result.add(regStuCount);
        result.add(auStuCount);
        result.add(latestHwTime !=null ? DateUtils.dateToString(latestHwTime,"yyyy-MM-dd"):"");
        result.add(tmHwSc);
        result.add(finCsHwEq1StuCount);
        result.add(finCsHwEq2StuCount);
        result.add(finCsHwGte3AuStuCount);
        result.add(lmFinCsHwGte3AuStuCount);
        result.add(increaseSpaceOfCsSettlementStuCount);
        result.add(vacnHwFlag?"是":"否");
        return result;
    }
}
