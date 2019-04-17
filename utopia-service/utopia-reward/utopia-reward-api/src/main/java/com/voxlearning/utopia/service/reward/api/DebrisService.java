package com.voxlearning.utopia.service.reward.api;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.reward.entity.Debris;
import com.voxlearning.utopia.service.reward.entity.DebrisHistory;
import com.voxlearning.utopia.service.user.api.entities.User;

import java.util.List;

public interface DebrisService {

    List<DebrisHistory> loadDebrisHistoryByUserId(Long userId);

    Debris loadDebrisByUserId(Long userId);

    MapMessage changeDebris(User user, DebrisHistory debrisHistory);

    MapMessage changeDebris(Long userId, Integer debrisType, Long debris, String comment);

}
