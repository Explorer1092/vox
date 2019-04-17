package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.OperationalActivitiesLoader;
import com.voxlearning.utopia.service.user.api.entities.User;

public class OperationalActivitiesLoaderClient implements OperationalActivitiesLoader {

    @ImportService(interfaceClass = OperationalActivitiesLoader.class)
    private OperationalActivitiesLoader hydraRemoteReference;

    @Override
    public MapMessage fetchMotherDayJztReport(String hid, Long sid, User user) {
        return hydraRemoteReference.fetchMotherDayJztReport(hid, sid, user);
    }

    @Override
    public MapMessage rewardStudent(String hid, Long sid, User user) {
        return hydraRemoteReference.rewardStudent(hid, sid, user);
    }
}
