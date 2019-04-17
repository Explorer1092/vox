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
 * 老师数据表 已将字段迁入DataReportField
 * Created by yaguang.wang on 2016/10/12.
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherDataReportData extends ReportData {

    private static final long serialVersionUID = 6535070682295640655L;
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
        result.add(dataMap.get("registerTimeStr"));
        result.add(dataMap.get("authTimeStr"));
        result.add(SafeConverter.toInt(dataMap.get("isAuth")) == 1 ? "是" : "否");
        String subjectStr = StringUtils.isBlank(SafeConverter.toString(dataMap.get("subject"))) ? "" : Subject.valueOf(SafeConverter.toString(dataMap.get("subject"))).getValue();
        result.add(subjectStr);
        result.add(dataMap.get("clazzCount"));
        result.add(dataMap.get("stuRegNum"));
        result.add(dataMap.get("stuAuthNum"));
        result.add(dataMap.get("latestAssignHomeworkTime"));
        result.add(dataMap.get("totalHomeworkCountSc"));
        result.add(dataMap.get("finCsHwGte3AuStuCount"));
        result.add(SafeConverter.toInt(dataMap.get("finCsHwEq1StuCount")));
        result.add(SafeConverter.toInt(dataMap.get("finCsHwEq2StuCount")));
        result.add(dataMap.get("finHwEq1UaStuCount"));
        result.add(dataMap.get("finHwEq2UaStuCount"));
        result.add(dataMap.get("finHwGte3UaStuCount"));
        //result.add(dataMap.get("finSglSubjHwGte3AuStuCount"));
        result.add(dataMap.get("tSemUnauthStuCount"));
        result.add(SafeConverter.toInt(dataMap.get("vacationHwGroupCount")));
        return result;
    }
}
