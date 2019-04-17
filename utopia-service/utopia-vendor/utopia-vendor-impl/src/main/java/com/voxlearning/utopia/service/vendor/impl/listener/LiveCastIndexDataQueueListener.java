package com.voxlearning.utopia.service.vendor.impl.listener;

import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRefinedLessons;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRemind;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastTargetAndExtra;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEvent;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastIndexRefinedLessonsDao;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastIndexRemindDao;
import com.voxlearning.utopia.service.vendor.impl.handler.livecast.LiveCastEventHandlerManager;
import com.voxlearning.utopia.service.vendor.impl.service.MySelfStudyGlobalMsgServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-10-18 下午4:06
 **/

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.jzt.livecast.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.jzt.livecast.queue")
        },
        maxPermits = 64
)
public class LiveCastIndexDataQueueListener implements MessageListener {

    @Inject
    private LiveCastIndexRemindDao liveCastIndexRemindDao;

    @Inject
    private MySelfStudyGlobalMsgServiceImpl mySelfStudyGlobalMsgService;


    @Inject
    private LiveCastIndexRefinedLessonsDao liveCastIndexRefinedLessonsDao;

    @Inject
    private LiveCastEventHandlerManager liveCastEventHandlerManager;

    @Override
    public void onMessage(Message message) {
        Object obj = message.decodeBody();



        if (obj instanceof LiveCastEvent){
            LiveCastEvent liveCastEvent = LiveCastEvent.class.cast(obj);
            liveCastEventHandlerManager.dealEvent(liveCastEvent);
        }

        //fixme 以下为兼容原来的消息 下一次就可以干掉了
        if (obj instanceof LiveCastIndexRemind){
            LiveCastIndexRemind liveCastIndexRemind = LiveCastIndexRemind.class.cast(obj);
            liveCastIndexRemind.generateId();
            if (liveCastIndexRemind.getTarget().getType() == LiveCastTargetAndExtra.Target.Type.all) {
                mySelfStudyGlobalMsgService.$updateLiveCastIndexRemind(liveCastIndexRemind);
            }else
                liveCastIndexRemindDao.upsert(liveCastIndexRemind);
        } else if (obj instanceof LiveCastIndexRefinedLessons){
            LiveCastIndexRefinedLessons liveCastIndexRefinedLessons = LiveCastIndexRefinedLessons.class.cast(obj);
            liveCastIndexRefinedLessons.generateId();
            if (liveCastIndexRefinedLessons.getTarget().getType() == LiveCastIndexRemind.Target.Type.all)
                mySelfStudyGlobalMsgService.$updateLiveCastRefinedLessons(liveCastIndexRefinedLessons);
            else
                liveCastIndexRefinedLessonsDao.upsert(liveCastIndexRefinedLessons);
        }
    }
}
