package com.voxlearning.utopia.agent.bean.resource;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
@NoArgsConstructor
public class ClazzTeacherResource implements Serializable {
    private Long teacherId;                                             // 2017/5/22
    private String teacherName;                                         // 2017/5/22
    private Subject subject;
    private Boolean hwAssigned = false; // 是否布置过作业
    private Integer tmHwSc = 0;//老师在该班本月布置所有作业套数（同组同老师）， 17模式 2017/5/22
    // 快乐学模式数据
    private Integer tmScanCsTpCount = 0;//老师当月扫描此group试卷数   2017/5/22
    private Boolean isAssignSHW = false;        // 是否布置暑期作业
}
