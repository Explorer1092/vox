package com.voxlearning.utopia.service.vendor.impl.handler.livecast;

import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRefinedLessons;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRemind;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEvent;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEventType;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastIndexRefinedLessonsDao;
import com.voxlearning.utopia.service.vendor.impl.service.MySelfStudyGlobalMsgServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-11-28 下午4:07
 **/
@Named
public class UpateIndexRefinedLessons implements LiveCastEventHandler {


    @Inject
    private MySelfStudyGlobalMsgServiceImpl mySelfStudyGlobalMsgService;

    @Inject
    private LiveCastIndexRefinedLessonsDao liveCastIndexRefinedLessonsDao;

    @Override
    public LiveCastEventType getLiveCastEventType() {
        return LiveCastEventType.updateIndexRefinedLessons;
    }

    @Override
    public void handle(LiveCastEvent event) {
        Object eventPayLoad = event.getEventPayLoad();
        if (eventPayLoad instanceof LiveCastIndexRefinedLessons) {
            LiveCastIndexRefinedLessons liveCastIndexRefinedLessons = LiveCastIndexRefinedLessons.class.cast(eventPayLoad);
            liveCastIndexRefinedLessons.generateId();
            if (liveCastIndexRefinedLessons.getTarget().getType() == LiveCastIndexRemind.Target.Type.all)
                mySelfStudyGlobalMsgService.$updateLiveCastRefinedLessons(liveCastIndexRefinedLessons);
            else
                liveCastIndexRefinedLessonsDao.upsert(liveCastIndexRefinedLessons);
        }
    }
}
