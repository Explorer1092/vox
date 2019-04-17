package com.voxlearning.utopia.service.newhomework.api.mapper.middleschool;

import com.voxlearning.utopia.service.user.api.mappers.GroupMapper;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * 中学作业供CRM查询的字段
 * @author xinchi.kong
 * @since 2016-12-07
 */
@Getter
@Setter
public class MiddleSchoolHomeworkCrmHistory implements Serializable  {

    private static final long serialVersionUID = -3604874627834660428L;

    private String homeworkId;
    private String name;
    private GroupMapper group;
    private Date createTime;
    private Date startTime;
    private Date closeTime;
    private Long studentCount;
    private Long finishedCount;
    private Boolean disabled;
}
