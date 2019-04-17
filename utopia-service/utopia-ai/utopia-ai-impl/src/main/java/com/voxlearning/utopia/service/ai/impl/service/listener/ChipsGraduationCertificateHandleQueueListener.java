package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserUnitResultHistoryDao;
import com.voxlearning.utopia.service.ai.impl.service.AiChipsEnglishConfigServiceImpl;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.ai.util.OrderProductUtil;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.api.entity.OrderProductItem;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.UserProfile;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * 毕业证
 */
@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.course.end.certificate.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.course.end.certificate.queue")
        },
        maxPermits = 4
)
public class ChipsGraduationCertificateHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private UserOrderLoaderClient userOrderLoaderClient;

    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;

    @Inject
    private UserLoaderClient userLoaderClient;

    @Inject
    private AiChipsEnglishConfigServiceImpl aiChipsEnglishConfigService;

    @Inject
    private ChipsUserService chipsUserService;

    private static final String SEND_RULE_PREFIX = "chips_send_rule_graduation_certificate";

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


            OrderProductItem item = userOrderLoaderClient.loadProductItemsByProductId(productId).stream().findFirst().orElse(null);
            if (item == null || StringUtils.isBlank(item.getAppItemId())) {
                logger.warn("product item  is null. message:{}", json);
                return;
            }


            ChipsEnglishPageContentConfig obj = aiChipsEnglishConfigService.loadChipsConfigByName(SEND_RULE_PREFIX);
            int val = SafeConverter.toInt(Optional.ofNullable(obj).map(ChipsEnglishPageContentConfig::getValue).orElse(""));
            List<AIUserUnitResultHistory> aiUserBookResultList = aiUserUnitResultHistoryDao.loadByUserId(userId).stream().filter(e -> item.getAppItemId().equals(e.getBookId())).collect(Collectors.toList());
            if (aiUserBookResultList.size() < val) {
                return;
            }

            OrderProduct orderProduct = userOrderLoaderClient.loadOrderProductById(productId);
            if (orderProduct == null) {
                logger.warn("product is null. message:{}", json);
            }

            if (!OrderProductUtil.isShortProduct(orderProduct)) {
                return;
            }
            Date endDate = Optional.ofNullable(orderProduct)
                    .map(e -> e.getAttributes())
                    .filter(e -> StringUtils.isNotBlank(e))
                    .map(e -> JsonUtils.fromJson(e))
                    .filter(e -> MapUtils.isNotEmpty(e))
                    .map(e -> SafeConverter.toDate(e.get("endDate")))
                    .orElse(new Date());
            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData("恭喜宝贝毕业啦！\n\r", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData(orderProduct.getName(), null));
            templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(endDate, FORMAT_SQL_DATE), null));
            templateDataMap.put("remark", new WechatTemplateData("\n\r→点击领取毕业证书，晒晒孩子的学习成果~", "#FF6551"));
            Map<String, Object> map = new HashMap<>();
            String userName = Optional.ofNullable(userLoaderClient.loadUser(userId))
                    .filter(e -> e.getProfile() != null)
                    .map(User::getProfile)
                    .map(UserProfile::getNickName)
                    .filter(e -> StringUtils.isNotBlank(e))
                    .orElse("");
            map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/getcertificate.vpage?user=" + userName);
            try {
                wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_COURSE_GRADUATION_CERTIFICATE.name(), templateDataMap, map);
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }
        }
    }
}
