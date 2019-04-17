package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;

/**
 * Created by Alex on 2015/9/10.
 */
@Data
public class JoinClazzStudentInfoMapper  implements Serializable {

    private Long schoolId;
    private String schoolName;
    private String teacherMobileOrid;
    private String teacherName;
    private Integer clazzLevel;
    private String clazzName;
    private String studentName;
    private String studentMobile;

    public boolean isAllEmpty() {
        return schoolId == null
                && StringUtils.isBlank(schoolName)
                && StringUtils.isBlank(teacherMobileOrid)
                && StringUtils.isBlank(teacherName)
                && clazzLevel == null
                && StringUtils.isBlank(clazzName)
                && StringUtils.isBlank(studentName)
                && StringUtils.isBlank(studentMobile);
    }
}