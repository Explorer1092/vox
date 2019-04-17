package com.voxlearning.utopia.schedule;

import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.spi.remote.ListMode;
import com.voxlearning.utopia.schedule.instance.ScheduleService;

import java.util.Collections;
import java.util.List;

@Install
public class ServiceExposeController
        implements com.voxlearning.alps.spi.remote.ServiceExposeController {

    @Override
    public ListMode getMode() {
        return ListMode.WHITE;
    }

    @Override
    public List<Class<?>> getList() {
        return Collections.singletonList(ScheduleService.class);
    }

    @Override
    public int getOrder() {
        return HIGHEST_PRECEDENCE;
    }
}
