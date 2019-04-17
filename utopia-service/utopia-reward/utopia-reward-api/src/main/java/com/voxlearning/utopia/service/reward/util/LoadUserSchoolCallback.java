package com.voxlearning.utopia.service.reward.util;

import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.User;

public interface LoadUserSchoolCallback {

    School loadUserSchool(User user);
}
