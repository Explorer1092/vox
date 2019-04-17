package com.voxlearning.washington.mapper;

import com.voxlearning.utopia.service.action.api.event.ActionEventType;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author xinxin
 * @since 5/8/2016
 */
@Getter
@Setter
public class StudentGrowthRecordMapper implements Serializable {
    private static final long serialVersionUID = 549089175041717916L;

    private String type;
    private String date;
    private Integer delta;
}
