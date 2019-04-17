package com.voxlearning.utopia.service.vendor.impl.handler.livecast;

import com.voxlearning.utopia.service.vendor.api.entity.LiveCastIndexRemind;
import com.voxlearning.utopia.service.vendor.api.entity.LiveCastTargetAndExtra;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEvent;
import com.voxlearning.utopia.service.vendor.api.event.LiveCastEventType;
import com.voxlearning.utopia.service.vendor.impl.dao.LiveCastIndexRemindDao;
import com.voxlearning.utopia.service.vendor.impl.service.MySelfStudyGlobalMsgServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-11-28 下午3:51
 **/
@Named
public class UpdateIndexRemind implements LiveCastEventHandler {

    @Inject
    private MySelfStudyGlobalMsgServiceImpl mySelfStudyGlobalMsgService;

    @Inject
    private LiveCastIndexRemindDao liveCastIndexRemindDao;

    @Override
    public LiveCastEventType getLiveCastEventType() {
        return LiveCastEventType.updateIndexRemind;
    }

    @Override
    public void handle(LiveCastEvent event) {
        Object eventPayLoad = event.getEventPayLoad();
        if (eventPayLoad instanceof LiveCastIndexRemind) {
            LiveCastIndexRemind liveCastIndexRemind = LiveCastIndexRemind.class.cast(eventPayLoad);
            liveCastIndexRemind.generateId();
            if (liveCastIndexRemind.getTarget().getType() == LiveCastTargetAndExtra.Target.Type.all) {
                mySelfStudyGlobalMsgService.$updateLiveCastIndexRemind(liveCastIndexRemind);
            } else
                liveCastIndexRemindDao.upsert(liveCastIndexRemind);
        }
    }
}
