package com.voxlearning.utopia.agent.bean.outerresource;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.utopia.agent.mockexam.service.dto.enums.ExamPlanEnums;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

/**
 * 上层资源View
 *
 * @author deliang.che
 * @since  2019/1/16
 **/
@Getter
@Setter
public class AgentOuterResourceView implements Serializable{

    private static final long serialVersionUID = 1480605751388724980L;
    private Long id;
    private String name;     // 名称

    private String jobName; //职务名称

    private Subject subject;
    private String subjectName;

    private String provinceName;
    private String cityName;
    private String countyName;
    private String organizationName;//单位名称（老师为学校名称，上层资源为机构名称）

}
