package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.ClazzLevel;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 * @author deliang.che
 * @create 2018-04-08
 **/
@Getter
@Setter
public class ClassOfflineReportData extends ClassBaseReportData implements ExportAble{
    private String teacher;                     //老师
    private int klxTnCount;                     //快乐学考号数
    private int tmFinTpGte1StuCount;            //普通扫描≥1次学生数
    private int tmFinTpGte3StuCount;            //普通扫描≥3次学生数
    private int tmFinTpGte1StuCountLastMonth;   //上月普通扫描≥1次学生数
    private int tmFinTpGte3StuCountLastMonth;   //上月普通扫描≥3次学生数
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
        result.add(teacher);
        result.add(klxTnCount);
        result.add(tmFinTpGte1StuCount);
        result.add(tmFinTpGte3StuCount);
        result.add(tmFinTpGte1StuCountLastMonth);
        result.add(tmFinTpGte3StuCountLastMonth);
        return result;
    }
}
