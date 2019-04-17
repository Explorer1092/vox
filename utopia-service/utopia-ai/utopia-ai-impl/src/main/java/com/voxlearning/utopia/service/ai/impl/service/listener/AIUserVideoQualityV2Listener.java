package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.entity.AIUserVideo;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserVideoDao;
import lombok.Data;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.io.Serializable;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 视频识别V2
 * 回调的message结构：{"#id":[{"#videoUrl":[{"left_eye_stat":0,"mouth_stat":1,"right_eye_stat":0,"face_quality":1}]}]}
 * e.g:
 * {"262316.SD_10300000536606.5c650830ac745970cc8c5612.1": [
 * {"https://17zy-homework.oss-cn-beijing.aliyuncs.com/dubbing/test/1550125127852_1550125131417.mp4": [{"mouth_stat": 0, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}, {"mouth_stat": 0, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}, {"mouth_stat": 0, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}]},
 * {"https://17zy-homework.oss-cn-beijing.aliyuncs.com/dubbing/test/1550125163393_1550125165892.mp4": [{"mouth_stat": 1, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}, {"mouth_stat": 1, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}]},
 * {"https://17zy-homework.oss-cn-beijing.aliyuncs.com/dubbing/test/1550125200204_1550125204075.mp4": [{"mouth_stat": 0, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}, {"mouth_stat": 0, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}, {"mouth_stat": 0, "right_eye_stat": 1, "face_quality": 1, "left_eye_stat": 1}]}
 * ]}
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.ai.user.video.handle.pic.res.v2.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.ai.user.video.handle.pic.res.v2.queue")
        },
        maxPermits = 64
)
public class AIUserVideoQualityV2Listener implements MessageListener {
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
            MessageResult result = JsonUtils.fromJson(data, MessageResult.class);
            if (result == null) {
                logger.error("AIUserVideoQualityV2Listener data is illegal. body：{}", data);
                return;
            }

            if (MapUtils.isEmpty(result)) {
                logger.error("AIUserVideoQualityV2Listener data is illegal. body：{}", data);
                return;
            }

            for (Map.Entry<String, List<VideoResult>> entry : result.entrySet()) {
                if (CollectionUtils.isEmpty(entry.getValue())) {
                    continue;
                }

                String key = entry.getKey();
                String[] splitString = key.split("\\.");
                if (splitString == null || splitString.length < 2) {
                    continue;
                }

                AIUserVideo aiUserVideo = new AIUserVideo();
                switch (splitString[0]) {
                    case "1":
                        Long user = SafeConverter.toLong(splitString[0]);
                        String session = splitString[2];
                        aiUserVideo.setUserId(user);
                        aiUserVideo.setId(user + session);
                        break;
                    case "3":
                        String userVideoId = splitString[1];
                        aiUserVideo.setId(userVideoId);
                        break;
                    case "2":
                        break;
                    default:
                        Long userId = SafeConverter.toLong(splitString[0]);
                        String sessionId = splitString[2];
                        aiUserVideo.setUserId(userId);
                        aiUserVideo.setId(userId + sessionId);
                        break;
                }

                aiUserVideo.setUpdateTime(new Date());
                List<Map> extList = new ArrayList<>();
                int unqualified = 0;
                int total = 0;
                for (VideoResult videoResult : entry.getValue()) {
                    for (Map.Entry<String, List<VideoQuality>> videoEntry : videoResult.entrySet()) {
                        List<VideoQuality> listVideo = videoEntry.getValue();
                        if (CollectionUtils.isEmpty(listVideo)) {
                            continue;
                        }
                        Map ext = new HashMap();
                        ext.put("url", videoEntry.getKey());
                        ext.put("result", listVideo);
                        extList.add(ext);

                        total += listVideo.size();
                        unqualified += listVideo.stream()
                                .filter(e -> e.getFace_quality() == 0 ||
                                        e.getLeft_eye_stat() == 0 ||
                                        e.getMouth_stat() == 0 ||
                                        e.getRight_eye_stat() == 0)
                                .collect(Collectors.toList()).size();
                    }
                }

                Map<String, Object> extMap = new HashMap<>();
                extMap.put("faceDetectV2", extList);
                aiUserVideo.setExt(extMap);
                if (unqualified * 2 > total && total != 0) {
                    aiUserVideo.setCategory(AIUserVideo.Category.Bad);
                } else {
                    aiUserVideo.setCategory(AIUserVideo.Category.Common);
                }
                aiUserVideoDao.upsert(aiUserVideo);
            }
        }
    }


    @Data
    private static class MessageResult extends HashMap<String, List<VideoResult>> {
    }

    @Data
    private static class VideoResult extends HashMap<String, List<VideoQuality>> {
    }

    @Data
    private static class VideoQuality implements Serializable {
        private static final long serialVersionUID = 0L;
        private int left_eye_stat;
        private int mouth_stat;
        private int right_eye_stat;
        private int face_quality;
    }
}
