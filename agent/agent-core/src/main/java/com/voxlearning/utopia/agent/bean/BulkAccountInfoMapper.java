package com.voxlearning.utopia.agent.bean;

import com.voxlearning.alps.core.util.StringUtils;
import lombok.Data;

import java.io.Serializable;

/**
 *
 * Created by Alex on 2015/9/11.
 */
@Data
public class BulkAccountInfoMapper implements Serializable{

    private Long schoolId;
    private String schoolName;
    private String teacherName;
    private String subject;
    private String teacherMobile;
    private Integer clazzLevel;
    private String clazzName;
    private String studentName;

    public boolean isAllEmpty() {
        return schoolId == null
                && StringUtils.isBlank(schoolName)
                && StringUtils.isBlank(teacherName)
                && StringUtils.isBlank(subject)
                && StringUtils.isBlank(teacherMobile)
                && clazzLevel == null
                && StringUtils.isBlank(clazzName)
                && StringUtils.isBlank(studentName);
    }

}
