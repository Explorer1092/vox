package com.voxlearning.utopia.service.reward.util;

import com.voxlearning.utopia.service.user.api.entities.User;

public interface GrayFuncMngCallback {

    boolean isGrayAvailable(User user, String func1, String func2);

}
