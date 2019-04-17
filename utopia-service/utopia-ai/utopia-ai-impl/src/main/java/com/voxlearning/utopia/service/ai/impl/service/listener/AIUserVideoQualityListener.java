package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.alibaba.fastjson.JSONObject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserVideoDao;
import lombok.Getter;
import lombok.Setter;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视频识别
 * Created by Summer on 2018/8/29
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.ai.user.video.handle.pic.res.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.ai.user.video.handle.pic.res.queue")
        },
        maxPermits = 64
)
public class AIUserVideoQualityListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private AIUserVideoDao aiUserVideoDao;
    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("video handle pic queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body instanceof String) {
            String data = (String) body;
            VideoResult result = JSONObject.parseObject(data, VideoResult.class);
            if (result == null) {
                logger.error("AIUserVideoQualityListener data is illegal. body：{}", data);
                return;
            }

            if (CollectionUtils.isEmpty(result.getURL())) {
                return;
            }

            int total = result.getURL()
                    .stream()
                    .filter(e -> MapUtils.isNotEmpty(e))
                    .filter(e -> e.values().stream().filter(e1 -> e1.getFace_quality() == 0  ||
                                                                  e1.getLeft_eye_stat() == 0 ||
                                                                  e1.getRight_eye_stat() == 0 ||
                                                                  e1.getMouth_stat() == 0)
                                                    .findFirst().orElse(null) == null)
                    .collect(Collectors.toList()).size();
            AIUserVideo aiUserVideo = new AIUserVideo();
            List<Map<String, Object>> mapList = new ArrayList<>();
            result.getURL().forEach(e ->  {
                Map<String, Object> map = new HashMap<>();
                map.put("url", e.keySet().stream().findFirst().orElse(null));
                map.put("result", e.values().stream().findFirst().orElse(null));
                mapList.add(map);
            });
            Map<String, Object> extMap = new HashMap<>();
            extMap.put("faceDetect", mapList);
            if (MapUtils.isNotEmpty(aiUserVideo.getExt())) {
                extMap.putAll(aiUserVideo.getExt());
            }
            aiUserVideo.setExt(extMap);
            aiUserVideo.setUpdateTime(new Date());
            aiUserVideo.setUserId(result.fetchUserId());
            aiUserVideo.setId(result.fetchUserId() + result.fetchSessionId());
            if (total * 2 > result.getURL().size()) {
                aiUserVideo.setCategory(AIUserVideo.Category.Common);
            } else {
                aiUserVideo.setCategory(AIUserVideo.Category.Bad);
            }
            aiUserVideoDao.upsert(aiUserVideo);
        }
    }

    @Setter
    @Getter
    private static class VideoResult implements Serializable {
        private static final long serialVersionUID = 0L;
        private String ID;
        private List<Map<String, VideoQuality>> URL;

        public Long fetchUserId() {
            String[] splitString = ID.split("\\.");
            if (splitString == null || splitString.length < 3) {
                return null;
            }
            return SafeConverter.toLong(splitString[0]);
        }

        public String fetchSessionId() {
            String[] splitString = ID.split("\\.");
            if (splitString == null || splitString.length < 3) {
                return "";
            }
            return splitString[2];
        }
    }

    @Setter
    @Getter
    private static class VideoQuality implements Serializable {
        private static final long serialVersionUID = 0L;
        private int left_eye_stat;
        private int mouth_stat;
        private int right_eye_stat;
        private int face_quality;
    }
}
