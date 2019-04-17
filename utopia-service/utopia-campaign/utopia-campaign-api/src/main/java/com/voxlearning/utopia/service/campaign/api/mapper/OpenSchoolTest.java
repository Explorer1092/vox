package com.voxlearning.utopia.service.campaign.api.mapper;

import lombok.Getter;
import lombok.Setter;

import java.util.Set;

@Getter
@Setter
public class OpenSchoolTest implements java.io.Serializable {
    private static final long serialVersionUID = 7445427406216746142L;

    private Long teacherId;
    private Set<Long> groupList;
    //private boolean assignReward;
    private boolean shareReward;

}
