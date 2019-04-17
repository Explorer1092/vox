package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.course.begin.notify.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.course.begin.notify.queue")
        },
        maxPermits = 4
)
public class ChipsCourseBeginNotifyHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("course begin notify handle share queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body != null && body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("AIUserShareVideoHandleQueueListener error. message:{}", body);
                return;
            }

            long userId = SafeConverter.toLong(param.get("U"));
            String productId = SafeConverter.toString(param.get("P"));
            if (Long.compare(userId, 0L) < 0 || StringUtils.isBlank(productId)) {
               return;
            }


            Set<String> productIds = chipsUserService.loadUserBoughtProduct(userId);
            if (CollectionUtils.isEmpty(productIds) || !productIds.contains(productId)) {
                return;
            }

            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (orderProduct == null) {
                return;
            }

            Date date = Optional.ofNullable(chipsEnglishProductTimetableDao.load(productId))
                    .map(ChipsEnglishProductTimetable::getBeginDate)
                    .orElse(DateUtils.addDays(new Date(), 1));
            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("userName", new WechatTemplateData("您好，课程开始啦", null));
            templateDataMap.put("courseName", new WechatTemplateData(orderProduct.getName(), null));
            templateDataMap.put("date", new WechatTemplateData(DateUtils.dateToString(date, "MM月dd日"), null));
            templateDataMap.put("remark", new WechatTemplateData("\n" +
                    "开课前，确认一下你都准备好啦↓↓↓\n" +
                    "\n" +
                    "添加你的专属老师微信\n" +
                    "\n" +
                    "记得下载家长通APP并更新至最新版\n" +
                    "\n" +
                    "今晚8点微信群中开班，等你哟", null));
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_BEGIN.name(), templateDataMap, Collections.emptyMap());
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }
        }
    }
}
