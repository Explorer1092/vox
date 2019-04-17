package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.agent.bean.export.ExportAble;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

/**
 *  快乐学老师每日扫描数据
 * Created by yaguang.wang
 * on 2017/4/7.
 */
@Getter
@Setter
@NoArgsConstructor
public class TeacherKlxEverydayScanData implements ExportAble {

    private String regionName;
    private String cityName;
    private String countyName;
    private String businessDeveloper;
    private Long schoolId;
    private String schoolName;
    private String schoolPhase;
    private String schoolLevel;
    private Long teacherId;
    private String teacherName;
    private String subject;                         // 科目
    private Boolean isQbManager;    // 是否为校本题库管理员
    private Boolean isSubjectLeader;    //是否为学科组长
    private String time;
    private List<Integer> scanData;          // 每日扫描数据

    @Override
    public List<Object> getExportAbleData() {
        List<Object> result = new ArrayList<>();
        result.add(this.getRegionName());
        result.add(this.getCityName());
        result.add(this.getCountyName());
        result.add(this.getBusinessDeveloper());
        result.add(this.getSchoolId());
        result.add(this.getSchoolName());
        result.add(this.getSchoolPhase());
        result.add(this.getSchoolLevel());
        result.add(this.getTeacherId());
        result.add(this.getTeacherName());
        result.add(this.getSubject());
        result.add(SafeConverter.toBoolean(this.getIsQbManager()) ? "校本题库管理员" : "");
        result.add(SafeConverter.toBoolean(this.getIsSubjectLeader()) ? "学科组长" : "");
        result.add(this.getTime());
        if (CollectionUtils.isNotEmpty(scanData)) {
            result.addAll(scanData);
        }
        return result;
    }
}
