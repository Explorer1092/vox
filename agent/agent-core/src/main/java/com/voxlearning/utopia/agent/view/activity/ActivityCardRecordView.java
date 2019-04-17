package com.voxlearning.utopia.agent.view.activity;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Data;

import java.util.Date;

@Data
public class ActivityCardRecordView {

    private Long teacherId;
    private String teacherName;
    private Long schoolId;
    private String schoolName;
    private String subject;

    private String cardNo;

    private Boolean authFlag;
    private Boolean isParent;
    private Boolean isUsed;

    private Date businessTime;              // 业务时间

}
