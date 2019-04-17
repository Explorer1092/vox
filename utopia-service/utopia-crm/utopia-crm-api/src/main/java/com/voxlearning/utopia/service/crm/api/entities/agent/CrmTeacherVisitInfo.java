package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 *
 * Created by yaguang.wang on 2016/7/8.
 */
@Getter
@Setter
@NoArgsConstructor
public class CrmTeacherVisitInfo implements Serializable{
    private static final long serialVersionUID = -4975671831850493532L;
    private Long teacherId;  // 老师ID   如果拜访对象为校长时：teacherId=100, 为其他时，teacherId=190 教导主任290
    private String teacherName; // 老师姓名
    private String visitInfo; // 拜访效果及详情

    private Subject subject;

    private Date visitTime;//拜访时间

    public boolean isRealTeacher() {
        return teacherId != null && teacherId > 1000L;
    }
}
