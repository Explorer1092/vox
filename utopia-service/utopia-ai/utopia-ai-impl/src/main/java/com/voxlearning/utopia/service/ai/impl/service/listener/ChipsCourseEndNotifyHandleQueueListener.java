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
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;


@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.course.end.notify.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.course.end.notify.queue")
        },
        maxPermits = 4
)
public class ChipsCourseEndNotifyHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private ChipsUserService chipsUserService;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

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

            ChipsEnglishProductTimetable productTimetable = chipsEnglishProductTimetableDao.load(productId);
            if (productTimetable == null || CollectionUtils.isEmpty(productTimetable.getCourses()) || productTimetable.getEndDate() == null) {
                return;
            }

            Date now = new Date();
            if (now.after(productTimetable.getEndDate())) {
                return;
            }

            String bookId = productTimetable.getCourses().stream().filter(e -> e.getBeginDate().before(now)).map(ChipsEnglishProductTimetable.Course::getBookId).findFirst().orElse("");
            if (StringUtils.isBlank(bookId)) {
                return;
            }

            Date date = productTimetable.getEndDate();
            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData("本期集训营即将进入最后2天倒计时！\n" +
                    "\n" +
                    "赶快检查下宝贝的课程有没有落下，记得提醒宝贝及时补哦~\n\r", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData(orderProduct.getName(), "#1BA9EF"));
            templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(date, FORMAT_SQL_DATE), "#1BA9EF"));
            templateDataMap.put("remark", new WechatTemplateData("\n\r→点我查看课程详情", "#FF6551"));
            try {
                Map<String, Object> map = new HashMap<>();
                map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/travelcatalog.vpage?book=" + bookId+"&pname="+orderProduct.getName());
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_END.name(), templateDataMap, map);
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, productId:{}, dataMap:{} ", userId, productId, JsonUtils.toJson(templateDataMap), e);
            }
        }
    }
}
