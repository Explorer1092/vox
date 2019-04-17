package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;


/**
 * online和offline模式老师数据基类
 *
 * @author deliang.che
 * @date 2018-03-19
 **/

@Getter
@Setter
public class TeacherBaseReportData {
    private Integer day;
    private String chargePerson;
    private String cityName;
    private String countyName;
    private Long schoolId;
    private String schoolName;
    private SchoolLevel schoolLevel;
    private AgentSchoolPopularityType schoolPopularity;
    private Long teacherId;
    private String teacherName;
    private int auState;
    private String subject;
    private Date regTime;                   //注册日期
    private Date auTime;                    //认证日期
}
