package com.voxlearning.utopia.service.piclisten.impl.service;

import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.api.constant.MySelfStudyActionType;
import com.voxlearning.utopia.api.constant.SelfStudyType;

import javax.inject.Named;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2018-01-05 下午8:01
 **/
@Named
public class MySelfStudyQueueService {

     @AlpsQueueProducer(queue = "utopia.vendor.myselfstudy.queue")
     private MessageProducer producer;

     public void updateSelfStudyProgress(Long studentId, SelfStudyType selfStudyType, String progress){
          MySelfStudyActionEvent event = new MySelfStudyActionEvent(studentId, selfStudyType, MySelfStudyActionType.UPDATE_PROGRESS);
          Map<String, Object> map = new LinkedHashMap<>();

          map.put("studyProgress", progress == null ? "" : progress);
          map.put("lastUserDate", new Date().getTime());
          event.setAttributes(map);
          producer.produce(event.toMessage());
     }
}
