package com.voxlearning.utopia.service.campaign.api.mapper.dp;

import lombok.Getter;
import lombok.Setter;

import java.util.List;

@Getter
@Setter
public class StudentPlanningCount implements java.io.Serializable {

    private Long groupId;
    private List<Long> studentIds;
    private Long studentSize;

    public Integer getStudentSize() {
        return studentIds == null ? 0 : studentIds.size();
    }
}
