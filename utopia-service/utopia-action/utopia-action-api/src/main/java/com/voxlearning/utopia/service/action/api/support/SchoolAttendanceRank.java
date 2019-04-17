package com.voxlearning.utopia.service.action.api.support;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

/**
 * @author xinxin
 * @since 25/8/2016
 */
@Getter
@Setter
public class SchoolAttendanceRank implements Serializable {
    private static final long serialVersionUID = 3724070538265083999L;

    private Long schoolId;
    private List<ClazzAttendanceInfo> ranks;
}
