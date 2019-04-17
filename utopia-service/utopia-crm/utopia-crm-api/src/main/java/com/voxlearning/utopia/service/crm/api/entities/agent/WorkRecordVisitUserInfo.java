package com.voxlearning.utopia.service.crm.api.entities.agent;

import com.voxlearning.alps.annotation.meta.Subject;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;
import java.util.Objects;


/**
 * 工作记录拜访人员信息
 */
@Getter
@Setter
public class WorkRecordVisitUserInfo implements Serializable{

    private static final long serialVersionUID = -5615951137033781632L;
    public static final Integer TEACHER_JOB = -1;
    public static final String TEACHER_JOB_NAME = "老师";
    private Long id;
    private String name;
    private Integer job;
    private String jobName;
    private String regionName;
    private Subject subject;
    private String subjectName;
    private String result;

    private Date visitTime;//拜访时间

    public boolean isRealTeacher() {
        return Objects.equals(job, TEACHER_JOB) && id != null && id > 1000L;
    }
}
