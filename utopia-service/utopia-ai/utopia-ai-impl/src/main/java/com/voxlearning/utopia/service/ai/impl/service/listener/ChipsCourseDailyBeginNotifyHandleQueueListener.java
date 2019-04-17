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
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsEnglishProductTimetableDao;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.question.api.StoneDataLoader;
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
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.course.daily.begin.notify.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.course.daily.begin.notify.queue")
        },
        maxPermits = 64
)
public class ChipsCourseDailyBeginNotifyHandleQueueListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    private ChipsUserService chipsUserService;

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

            String cnInfo = Optional.ofNullable(unit)
                    .map(StoneUnitData::getJsonData)
                    .map(StoneUnitData.Unit::getImage_discription)
                    .orElse("");

            String enInfo = Optional.ofNullable(unit)
                    .map(StoneUnitData::getJsonData)
                    .map(StoneUnitData.Unit::getImage_title)
                    .orElse("");

            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData("早上好！今日的学习旅程已开启 \n\r", "#FF6551"));
            templateDataMap.put("keyword1", new WechatTemplateData( enInfo + " " + cnInfo + "\n\r", null));
            templateDataMap.put("keyword2", new WechatTemplateData("今晚 23:59 点前完成学习任务！", null));
            templateDataMap.put("remark", new WechatTemplateData("\n\r→点击这里开始口语学习之旅", "#FF6551"));
            try {
                wechatServiceClient.getWechatService()
                        .processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_STUDY_DAILY_NOTIFY.name(), templateDataMap, Collections.emptyMap());
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
            }
        }
    }
}
