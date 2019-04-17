package com.voxlearning.utopia.service.newhomework.impl.listener;

import com.voxlearning.alps.core.concurrent.AlpsThreadPool;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.spi.pubsub.PubsubDestination;
import com.voxlearning.alps.spi.pubsub.PubsubSubscriber;
import com.voxlearning.alps.spi.pubsub.PubsubSystem;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.alps.spi.queue.MessageListener;
import com.voxlearning.utopia.service.newhomework.api.entity.ocr.IndependentOcrProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.OcrMentalImageDetail;
import com.voxlearning.utopia.service.newhomework.impl.service.IndependentOcrServiceImpl;
import lombok.Getter;
import lombok.Setter;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Objects;

/**
 * @author majianxin
 * @version V1.0
 * @date 2019/3/27
 */
@Named
@PubsubSubscriber(
        destinations = {
                @PubsubDestination(system = PubsubSystem.KFK, connection = "primary", topic = "parent.ocr.save.info.notify")
        },
        maxPermits = 64
)
public class ParentIndependentOcrListener extends SpringContainerSupport implements MessageListener {

    @Inject private IndependentOcrServiceImpl independentOcrService;

    @Override
    public void onMessage(Message message) {
        Object body = message.decodeBody();
        if (body instanceof String) {
            ParentIndependentOcr parentIndependentOcr = JsonUtils.fromJson((String) body, ParentIndependentOcr.class);
            if (parentIndependentOcr == null) {
                return;
            }

            if (Objects.equals(parentIndependentOcr.getType(), "save")) {
                save(parentIndependentOcr);
            } else if (Objects.equals(parentIndependentOcr.getType(), "delete")) {
                independentOcrService.deleteByImgUrls(parentIndependentOcr.getStudentId(), parentIndependentOcr.getImgUrls());
            }
        }
    }


    private void save(ParentIndependentOcr parentIndependentOcr) {
        List<IndependentOcrProcessResult> independentOcrProcessResults = new ArrayList<>();

        for (OcrMentalImageDetail detail : parentIndependentOcr.getEntity()) {
            IndependentOcrProcessResult result = new IndependentOcrProcessResult();
            Date date = new Date(parentIndependentOcr.getCreateTime());
            IndependentOcrProcessResult.ID id = new IndependentOcrProcessResult.ID(date, parentIndependentOcr.getStudentId());
            result.setId(id.toString());
            result.setStudentId(parentIndependentOcr.getStudentId());
            result.setUserId(parentIndependentOcr.getParentId());
            result.setDisabled(false);
            result.setClientName(parentIndependentOcr.getClientName());
            result.setClientType(parentIndependentOcr.getClientType());
            result.setUpdateAt(date);
            result.setCreateAt(date);
            result.setOcrMentalImageDetail(detail);
            independentOcrProcessResults.add(result);
        }
        independentOcrService.batchProcessOcrResult(independentOcrProcessResults);

        // 用户上传图片处理
        AlpsThreadPool
                .getInstance()
                .submit(() -> independentOcrService.classifyImage(String.valueOf(parentIndependentOcr.getCreateTime()), parentIndependentOcr.getParentId(), independentOcrProcessResults));
    }

    @Getter
    @Setter
    private static class ParentIndependentOcr {
        private String type;    // save,delete
        private Long studentId;
        private Long parentId;
        private List<OcrMentalImageDetail> entity;
        private String clientName;
        private String clientType;
        private Long createTime;
        private List<String> imgUrls;   // 删除图片url
    }
}
