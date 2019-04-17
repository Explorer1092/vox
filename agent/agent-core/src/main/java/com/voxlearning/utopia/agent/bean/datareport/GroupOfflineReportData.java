package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author deliang.che
 * @date 2018-03-17
 **/
@Getter
@Setter
public class GroupOfflineReportData extends GroupBaseReportData implements ExportAble{
    private int klxTnCount;             //快乐学考号数
    private int tmScanTpCount;          //本月扫描试卷套数
    private int tmFinCsTpEqStuCount;    //普通扫描≥1次学生数
    private int tmFinCsTpGte3StuCount;  //普通扫描≥3次学生数
    private int lmFinCsTpEqStuCount;    //上月普通扫描≥1次学生数
    private int lmFinCsTpGte3StuCount;  //上月普通扫描≥3次学生数
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
        result.add(null != getSchoolPopularity()?getSchoolPopularity().getLevel():"");
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
        result.add(klxTnCount);
        result.add(tmScanTpCount);
        result.add(tmFinCsTpEqStuCount);
        result.add(tmFinCsTpGte3StuCount);
        result.add(lmFinCsTpEqStuCount);
        result.add(lmFinCsTpGte3StuCount);
        return result;
    }
}
