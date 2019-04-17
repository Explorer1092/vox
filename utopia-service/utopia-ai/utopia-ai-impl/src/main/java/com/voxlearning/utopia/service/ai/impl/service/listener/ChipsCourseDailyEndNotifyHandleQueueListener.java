package com.voxlearning.utopia.service.ai.impl.service.listener;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.data.StoneUnitData;
import com.voxlearning.utopia.service.ai.entity.AIUserUnitResultHistory;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.AIUserUnitResultHistoryDao;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.message.api.entity.AppMessage;
import com.voxlearning.utopia.service.message.client.MessageCommandServiceClient;
import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.question.api.StoneDataLoader;
import com.voxlearning.utopia.service.vendor.api.AppMessageService;
import com.voxlearning.utopia.service.vendor.api.constant.ParentMessageType;
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
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.course.daily.end.notify.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.course.daily.end.notify.queue")
        },
        maxPermits = 4
)
public class ChipsCourseDailyEndNotifyHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;

    @Inject
    private MessageCommandServiceClient messageCommandServiceClient;

    @Inject
    private ChipsUserService chipsUserService;

    @ImportService(interfaceClass = AppMessageService.class)
    private AppMessageService appMessageService;

    @ImportService(interfaceClass = StoneDataLoader.class)
    private StoneDataLoader stoneDataLoader;

    private final static String DATE_FORMATE = "yyyy-MM-dd";

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

            ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(productId);
            if (timetable == null) {
                return;
            }

            Set<String> productIds = chipsUserService.loadUserBoughtProduct(userId);
            if (CollectionUtils.isEmpty(productIds) || !productIds.contains(productId)) {
                return;
            }


            String now = DateUtils.dateToString(new Date(), DATE_FORMATE);
            String unitId = Optional.ofNullable(timetable.getCourses())
                    .map(e -> e.stream().filter(e1 -> DateUtils.dateToString(e1.getBeginDate(), DATE_FORMATE).equals(now)).findFirst().orElse(null))
                    .map(ChipsEnglishProductTimetable.Course::getUnitId)
                    .orElse("");
            if (StringUtils.isBlank(unitId)) {
                return;
            }

            StoneUnitData unit = Optional.ofNullable(unitId)
                    .map(e -> stoneDataLoader.loadStoneDataIncludeDisabled(Collections.singletonList(e)))
                    .map(e -> e.get(unitId))
                    .map(StoneUnitData::newInstance)
                    .orElse(null);
            if (unit == null) {
                return;
            }

            AIUserUnitResultHistory aiUserUnitResultHistory = aiUserUnitResultHistoryDao.loadByUserId(userId).stream().filter(e -> unit.getId().equals(e.getUnitId())).findFirst().orElse(null);
            if (aiUserUnitResultHistory != null) {
                return;
            }

            String cnInfo = Optional.ofNullable(unit)
                    .map(StoneUnitData::getJsonData)
                    .map(StoneUnitData.Unit::getImage_discription)
                    .orElse("");

            String enInfo = Optional.ofNullable(unit)
                    .map(StoneUnitData::getJsonData)
                    .map(StoneUnitData.Unit::getImage_title)
                    .orElse("");

            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData("系统察觉到您的宝贝还没有完成今日的学习\n\r", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData(enInfo + " " + cnInfo, "#1BA9EF"));
            templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(new Date(), FORMAT_SQL_DATE), "#1BA9EF"));
            templateDataMap.put("remark", new WechatTemplateData("\n\r→点击这里开始口语学习之旅", "#FF6551"));
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_STUDY_DAILY_NOTIFY.name(), templateDataMap, Collections.emptyMap());
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }

            sendAppJpushMessageById(userId, "薯条英语已经开课啦！系统察觉到宝贝现在还没有完成今日学习，请家长赶快督促宝贝打开家长通，进入薯条英语学习吧~");
        }
    }

    private void sendAppJpushMessageById(Long userId, String noticeContent) {
        AppMessage appMessage = new AppMessage();
        appMessage.setUserId(userId);
        appMessage.setMessageType(ParentMessageType.REMINDER.getType());
        appMessage.setTitle("通知");
        appMessage.setContent(noticeContent);
        appMessage.setLinkUrl("https://wechat.17zuoye.com/chips/center/tostudy.vpage");
        appMessage.setLinkType(0);
        appMessage.setExtInfo(new HashMap<>());
        try{
            messageCommandServiceClient.getMessageCommandService().createAppMessage(appMessage);
        } catch (Exception e) {
            logger.error("createAppMessage error.appMessage:{}", appMessage, e);
        }

        Map<String, Object> jpushExtInfo = new HashMap<>();
        jpushExtInfo.put("tag", "AI_PUSH");
        try {
            appMessageService.sendAppJpushMessageByIds(noticeContent, AppMessageSource.PARENT, Collections.singletonList(userId), jpushExtInfo);
        } catch (Exception e) {
            logger.error("sendAppJpushMessageByIds error.noticeContent:{}, userId:{}, jpushExtInfo:{}", noticeContent, userId, jpushExtInfo, e);
        }
    }
}
