package com.voxlearning.utopia.service.piclisten.impl.listener;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.piclisten.impl.handler.PicListenCollectDataHandler;
import com.voxlearning.utopia.service.vendor.api.entity.PicListenCollectData;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * @author jiangpeng
 * @since 2017-03-15 下午5:57
 **/
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "primary",
                        queue = "utopia.vendor.collect.queue"
                ),
                @QueueDestination(
                        system = QueueSystem.KFK,
                        config = "main-backup",
                        queue = "utopia.vendor.collect.queue"
                )
        }
)
public class PicListenCollectDataQueueListen extends SpringContainerSupport implements MessageListener {


    @Inject
    private PicListenCollectDataHandler picListenCollectDataHandler;

    @Override
    public void onMessage(Message message) {
        Object decoded = message.decodeBody();
        if (decoded instanceof PicListenCollectData){
            PicListenCollectData data = (PicListenCollectData) decoded;
            picListenCollectDataHandler.handleData(data);
        }
    }
}
