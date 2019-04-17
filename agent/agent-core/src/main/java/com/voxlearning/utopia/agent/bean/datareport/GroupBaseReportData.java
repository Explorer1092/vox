package com.voxlearning.utopia.agent.bean.datareport;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.utopia.service.crm.api.constants.agent.AgentSchoolPopularityType;
import lombok.Getter;
import lombok.Setter;


/**
 * @author deliang.che
 * @date 2018-03-19
 **/
@Getter
@Setter
public class GroupBaseReportData {
    private Integer day;
    private String chargePerson;
    private String cityName;
    private String countyName;
    private Long schoolId;
    private String schoolName;
    private SchoolLevel schoolLevel;
    private AgentSchoolPopularityType schoolPopularity;
    private Integer clazzLevel;
    private String clazzName;
    private String subject;
    private Long teacherId;
    private String teacherName;
    private int auState;
}
