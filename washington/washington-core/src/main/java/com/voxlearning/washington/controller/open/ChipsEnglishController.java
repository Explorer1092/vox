package com.voxlearning.washington.controller.open;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.spi.queue.AlpsQueueProducer;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageProducer;
import com.voxlearning.utopia.service.ai.client.AiLoaderClient;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.ResponseBody;

import javax.inject.Inject;
import java.util.HashMap;
import java.util.Map;

/**
 * @author songtao
 * @since 2018/4/25
 */
@Controller
@RequestMapping("/v1/chips")
public class ChipsEnglishController extends AbstractApiController {

    @Inject
    private AiLoaderClient aiLoaderClient;
//    @ImportService(interfaceClass = ChipsUserVideoLoader.class)
//    private ChipsUserVideoLoader chipsUserVideoLoader;

    @AlpsQueueProducer(queue = "utopia.chips.share.video.count.queue")
    private MessageProducer producer;

    @RequestMapping(value = "share/video.vpage", method = RequestMethod.POST)
    @ResponseBody
    public MapMessage shareVideo() {
        String id = getRequestString("id");
        if (StringUtils.isBlank(id)) {
            return MapMessage.successMessage().add("url", "").add("status", AIUserVideo.ExamineStatus.Failed);
        }
        AIUserVideo aiUserVideo = aiLoaderClient.getRemoteReference().loadUserVideoById(id);
//        Set<Long> blackList = chipsUserVideoLoader.loadVideoBlackList();
//        Long blackUser = Optional.of(aiUserVideo).map(e -> e.getUserId()).filter(e -> blackList.contains(e)).orElse(null);
//        if (blackUser != null) {
//            return MapMessage.successMessage().add("url", "").add("status", "BLACK");
//        }
        if (aiUserVideo == null || Boolean.TRUE.equals(aiUserVideo.getDisabled()) || aiUserVideo.getStatus() == AIUserVideo.ExamineStatus.Failed) {
            return MapMessage.successMessage().add("url", "").add("status", AIUserVideo.ExamineStatus.Failed);
        }

        Map<String, Object> res = new HashMap<>();
        res.put("ID", id);
        producer.produce(Message.newMessage().withPlainTextBody(JsonUtils.toJson(res)));

        return MapMessage.successMessage().add("url", aiUserVideo.getVideo()).add("status", aiUserVideo.getStatus());
    }


}
