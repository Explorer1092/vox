package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.SchoolDepartmentInfo;
import com.voxlearning.utopia.api.constant.EduSystemType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Created by yaguang.wang
 * on 2017/3/14.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolO2oReportData extends ReportData {
    private String time;                    // 时间
    private SchoolDepartmentInfo departmentInfo;// 学校所在部门的信息
    private String schoolingLength;        // 学制
    private Map<String, Object> dataMap;        // 接口返回的数据
    private Integer schoolSize;             // 学生基数

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
        SchoolLevel schoolLevel = SchoolLevel.safeParse(SafeConverter.toInt(dataMap.get("schoolPhase")));
        result.add(schoolLevel.getDescription());
        if(StringUtils.isBlank(schoolingLength)){
            if(schoolLevel == SchoolLevel.JUNIOR){
                schoolingLength = EduSystemType.P6.getDescription();
            }else if(schoolLevel == SchoolLevel.MIDDLE){
                schoolingLength = EduSystemType.J4.getDescription();
            }else if(schoolLevel == SchoolLevel.HIGH){
                schoolingLength = EduSystemType.S3.getDescription();
            }else if(schoolLevel == SchoolLevel.INFANT){
                schoolingLength = EduSystemType.I4.getDescription();
            }
        }
        result.add(schoolingLength);
        result.add(dataMap.get("schoolLevel"));
        result.add(schoolSize);
        result.add(dataMap.get("stuRegNum"));
        result.add(dataMap.get("stuAuthNum"));
        result.add(dataMap.get("tmIncRegStuCount"));
        result.add(dataMap.get("tmIncAuthStuCount"));
        result.add(dataMap.get("stuTnNum"));
        result.add(dataMap.get("finMathAnshEq1StuCount"));
        result.add(dataMap.get("finMathAnshGte2StuCount"));
        result.add(dataMap.get("finMathAnshGte2IncStuCount"));
        result.add(dataMap.get("finMathAnshGte2BfStuCount"));
        result.add(dataMap.get("finEngAnshEq1StuCount"));
        result.add(dataMap.get("finEngAnshGte2StuCount"));
        result.add(dataMap.get("finEngAnshGte2IncStuCount"));
        result.add(dataMap.get("finEngAnshGte2BfStuCount"));
        result.add(dataMap.get("finPhyAnshEq1StuCount"));
        result.add(dataMap.get("finPhyAnshGte2StuCount"));
        result.add(dataMap.get("finPhyAnshGte2IncStuCount"));
        result.add(dataMap.get("finPhyAnshGte2BfStuCount"));
        result.add(dataMap.get("finCheAnshEq1StuCount"));
        result.add(dataMap.get("finCheAnshGte2StuCount"));
        result.add(dataMap.get("finCheAnshGte2IncStuCount"));
        result.add(dataMap.get("finCheAnshGte2BfStuCount"));
        result.add(dataMap.get("finBiolAnshEq1StuCount"));
        result.add(dataMap.get("finBiolAnshGte2StuCount"));
        result.add(dataMap.get("finBiolAnshGte2IncStuCount"));
        result.add(dataMap.get("finBiolAnshGte2BfStuCount"));
        result.add(dataMap.get("finChnAnshEq1StuCount"));
        result.add(dataMap.get("finChnAnshGte2StuCount"));
        result.add(dataMap.get("finChnAnshGte2IncStuCount"));
        result.add(dataMap.get("finChnAnshGte2BfStuCount"));
        result.add(dataMap.get("finHistAnshEq1StuCount"));
        result.add(dataMap.get("finHistAnshGte2StuCount"));
        result.add(dataMap.get("finHistAnshGte2IncStuCount"));
        result.add(dataMap.get("finHistAnshGte2BfStuCount"));
        result.add(dataMap.get("finGeogAnshEq1StuCount"));
        result.add(dataMap.get("finGeogAnshGte2StuCount"));
        result.add(dataMap.get("finGeogAnshGte2IncStuCount"));
        result.add(dataMap.get("finGeogAnshGte2BfStuCount"));
        result.add(dataMap.get("finPolAnshEq1StuCount"));
        result.add(dataMap.get("finPolAnshGte2StuCount"));
        result.add(dataMap.get("finPolAnshGte2IncStuCount"));
        result.add(dataMap.get("finPolAnshGte2BfStuCount"));
        result.add(dataMap.get("finEngHwEq1AuStuCount"));
        result.add(dataMap.get("finEngHwEq2AuStuCount"));
        result.add(dataMap.get("finEngHwGte3AuStuCount"));
        result.add(dataMap.get("latestVisitTimeStr"));
        result.add(SafeConverter.toInt(dataMap.get("engAuthNum")));
        result.add(SafeConverter.toInt(dataMap.get("assignVacnHwAuthEngTeaCount")));
        return result;
    }

    public static void main(String[] args) {
        String table ="";
        String[] tableHead = table.split("\n");
        System.out.println(Arrays.toString(tableHead));
        StringBuffer result = new StringBuffer("");
        for (String head : tableHead) {
            result.append(StringUtils.formatMessage("result.add(dataMap.get(\"{}\"));\n", head));
        }
        System.out.println(result.toString());
    }
}
