package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.SchoolDepartmentInfo;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Created by yaguang.wang
 * on 2017/3/14.
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherO2oReportData extends ReportData{
    private String time;
    private SchoolDepartmentInfo departmentInfo;// 学校所在部门的信息
    private Map<String, Object> dataMap;        // 接口返回的数据
    // ------------------- 17年春季字段 ----------------------------
    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(this.getTime());
        fillDepartmentInfo(result, departmentInfo);
        result.add(dataMap.get("provinceName"));
        result.add(dataMap.get("cityName"));
        result.add(dataMap.get("countyName"));
        result.add(dataMap.get("schoolId"));
        result.add(dataMap.get("schoolName"));
        result.add( SchoolLevel.safeParse(SafeConverter.toInt(dataMap.get("schoolPhase"))).getDescription());
        result.add(dataMap.get("schoolLevel"));
        result.add(dataMap.get("teacherId"));
        result.add(dataMap.get("teacherName"));
        String subjectStr = StringUtils.isBlank(SafeConverter.toString(dataMap.get("subject"))) ? "" : Subject.valueOf(SafeConverter.toString(dataMap.get("subject"))).getValue();
        result.add(subjectStr);
        result.add(dataMap.get("clazzCount"));
        result.add(dataMap.get("stuTnNum"));
        result.add(dataMap.get("latestScanTpTime"));
        result.add(dataMap.get("tmScanTpCount"));
        result.add(dataMap.get("tmCsAnshEq1StuCount"));
        result.add(dataMap.get("tmCsAnshGte2StuCount"));
        result.add(dataMap.get("finCsHwEq1AuStuCount"));
        result.add(dataMap.get("finCsHwEq2AuStuCount"));
        result.add(dataMap.get("finCsHwGte3AuStuCount"));
        result.add(SafeConverter.toInt(dataMap.get("vacationHwGroupCount")));
        return result;
    }
}
