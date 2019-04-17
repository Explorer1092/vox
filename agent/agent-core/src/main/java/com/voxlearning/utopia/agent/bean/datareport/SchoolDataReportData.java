package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.SchoolDepartmentInfo;
import com.voxlearning.utopia.api.constant.EduSystemType;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPermeabilityType;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * 学校表 已将字段迁入DataReportField
 * Created by yagaung.wang on 2016/10/12.
 */
@Getter
@Setter
@NoArgsConstructor
public class SchoolDataReportData extends ReportData {

    private static final long serialVersionUID = 5185215335793607693L;

    private String time;                        // 时间
    private SchoolDepartmentInfo departmentInfo;// 学校所在部门的信息
    private Map<String, Object> dataMap;        // 接口返回的数据
    private String schoolingLength;            // 学制
    private Integer englishStartGrade;          // 英语起始年级
    private Integer schoolSize;                 // 学校规模
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
        result.add(englishStartGrade);
        result.add(dataMap.get("schoolLevel"));
        String permeability = SafeConverter.toString(dataMap.get("permeability"));
        result.add(StringUtils.isBlank(permeability) ? "" : AgentSchoolPermeabilityType.valueOf(permeability).getDesc());
        result.add(schoolSize);
        result.add(dataMap.get("stuRegNum"));
        result.add(dataMap.get("stuAuthNum"));
        result.add(dataMap.get("tmIncRegStuCount"));
        result.add(dataMap.get("tmIncAuthStuCount"));
        result.add(dataMap.get("engAuthNum"));
        result.add(dataMap.get("mathAuthNum"));
        result.add(dataMap.get("chinaAuthNum"));
        result.add(SafeConverter.toInt(dataMap.get("finSglSubjHwEq1AuStuCount")) + SafeConverter.toInt(dataMap.get("finSglSubjHwEq1UnAuStuCount")));
        result.add(SafeConverter.toInt(dataMap.get("finSglSubjHwEq2AuStuCount")) + SafeConverter.toInt(dataMap.get("finSglSubjHwEq2UnAuStuCount")));
        result.add(dataMap.get("finSglSubjHwGte3AuStuCount"));
        result.add(dataMap.get("finSglSubjHwGte3IncAuStuCount"));
        result.add(dataMap.get("finSglSubjHwGte3ShortBfAuStuCount"));
        result.add(dataMap.get("finSglSubjHwGte3LongBfAuStuCount"));
        result.add(dataMap.get("finEngHwEq1AuStuCount"));
        result.add(dataMap.get("finEngHwEq2AuStuCount"));
        result.add(dataMap.get("finEngHwGte3AuStuCount"));
        result.add(dataMap.get("finMathHwEq1AuStuCount"));
        result.add(dataMap.get("finMathHwEq2AuStuCount"));
        result.add(dataMap.get("finMathHwGte3AuStuCount"));
        result.add(dataMap.get("finChnHwEq1AuStuCount"));
        result.add(dataMap.get("finChnHwEq2AuStuCount"));
        result.add(dataMap.get("finChnHwGte3AuStuCount"));
        result.add(dataMap.get("latestVisitTimeStr"));
        result.add(dataMap.get("tSemUnauthStuCount"));

        result.add(SafeConverter.toInt(dataMap.get("assignVacnHwAuthEngTeaCount")));
        result.add(SafeConverter.toInt(dataMap.get("assignVacnHwAuthMathTeaCount")));
        result.add(SafeConverter.toInt(dataMap.get("assignVacnHwAuthChnTeaCount")));
        return result;
    }
}
