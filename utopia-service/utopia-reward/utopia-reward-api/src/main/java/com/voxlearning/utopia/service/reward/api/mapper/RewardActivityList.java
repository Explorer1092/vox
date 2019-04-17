package com.voxlearning.utopia.service.reward.api.mapper;

import com.voxlearning.utopia.service.reward.entity.RewardActivity;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.List;

@Getter
@Setter
@NoArgsConstructor
public class RewardActivityList implements Serializable {
    private static final long serialVersionUID = 2839359292124950801L;

    private List<RewardActivity> rewardActivityList;
    private long version;

}
