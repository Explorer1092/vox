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
import com.voxlearning.utopia.service.ai.util.OrderProductUtil;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.course.end.feedback.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.course.end.feedback.queue")
        },
        maxPermits = 4
)
public class ChipsCourseFeedbackHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private ChipsUserService chipsUserService;

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

            // Only short course
            if (!OrderProductUtil.isShortProduct(orderProduct)) {
                return;
            }

            ChipsEnglishProductTimetable chipsEnglishProductTimetable = chipsEnglishProductTimetableDao.load(productId);
            int days = Optional.ofNullable(chipsEnglishProductTimetable).map(x -> x.getCourses().size()).orElse(8);
            Date now = new Date();
            ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(userId);
            String userWechatName = Optional.ofNullable(parentExtAttribute)
                    .filter(e -> StringUtils.isNotBlank(e.getWechatNick()))
                    .map(ParentExtAttribute::getWechatNick)
                    .orElse("家长");
            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData("课程反馈提醒\n" +
                    userWechatName + "，宝贝课程已结束，在这" + days + "天的学习过程中，您对我们" +
                    "的课程有什么建议和意见写出来哦，帮助我们更好地服务你，参与有奖哈。\n\r", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData(orderProduct.getName(), "#1BA9EF"));
            templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(now, FORMAT_SQL_DATE), "#1BA9EF"));
            templateDataMap.put("remark", new WechatTemplateData("\n\r→点击链接，反馈您和孩子对我们的建议~", "#FF6551"));
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_FEEDBACK.name(), templateDataMap, Collections.emptyMap());
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }
        }
    }
}
