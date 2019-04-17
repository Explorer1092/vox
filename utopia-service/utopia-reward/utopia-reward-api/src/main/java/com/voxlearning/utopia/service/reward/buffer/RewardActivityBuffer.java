package com.voxlearning.utopia.service.reward.buffer;

import com.voxlearning.utopia.service.reward.api.mapper.RewardActivityList;
import com.voxlearning.utopia.service.reward.entity.RewardActivity;

import java.util.List;

public interface RewardActivityBuffer {

    void attach(RewardActivityList data);

    long getVersion();

    RewardActivityList dump();

    default List<RewardActivity> loadRewardActivity() {
        return dump().getRewardActivityList();
    }

    interface Aware {
        RewardActivityBuffer getRewardActivityBuffer();

        void resetRewardActivityBuffer();
    }

}
