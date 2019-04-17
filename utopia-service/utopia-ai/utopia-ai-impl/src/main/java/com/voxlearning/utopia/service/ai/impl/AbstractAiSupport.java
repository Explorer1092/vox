package com.voxlearning.utopia.service.ai.impl;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.ai.cache.manager.UserOfficialProductBuyCacheManager;
import com.voxlearning.utopia.service.ai.constant.LessonType;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishProductTimetable;
import com.voxlearning.utopia.service.ai.impl.persistence.*;
import com.voxlearning.utopia.service.ai.impl.persistence.support.AIUserLessonBookSupport;
import com.voxlearning.utopia.service.ai.impl.persistence.task.ChipsUserDrawingTaskPersistence;
import com.voxlearning.utopia.service.ai.impl.service.AiChipsEnglishConfigServiceImpl;
import com.voxlearning.utopia.service.ai.impl.service.processor.questionresult.AIUserQuestionResultProcessor;
import com.voxlearning.utopia.service.ai.impl.service.queue.AIUserQuestionResultCollectionQueueProducer;
import com.voxlearning.utopia.service.ai.impl.support.ChipCourseSupport;
import com.voxlearning.utopia.service.ai.impl.support.WechatConfig;
import com.voxlearning.utopia.service.ai.internal.ChipsContentService;
import com.voxlearning.utopia.service.ai.internal.ChipsMessageService;
import com.voxlearning.utopia.service.ai.internal.ChipsUserService;
import com.voxlearning.utopia.service.ai.util.OrderProductUtil;
import com.voxlearning.utopia.service.content.api.entity.NewBookCatalog;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.order.api.entity.OrderProduct;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.question.consumer.QuestionLoaderClient;
import com.voxlearning.utopia.service.question.consumer.StoneDataLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;
import java.util.*;

import static com.voxlearning.alps.calendar.DateUtils.FORMAT_SQL_DATE;

/**
 * Created by Summer on 2018/3/27
 */
public abstract class AbstractAiSupport extends SpringContainerSupport {

    @Inject
    protected NewContentLoaderClient newContentLoaderClient;
    @Inject
    protected UserLoaderClient userLoaderClient;
    @Inject
    protected QuestionLoaderClient questionLoaderClient;
    @Inject
    protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    protected WechatServiceClient wechatServiceClient;

    @Inject
    protected AIUserUnitResultHistoryDao aiUserUnitResultHistoryDao;
    @Inject
    protected AIUserLessonResultHistoryDao aiUserLessonResultHistoryDao;
    @Inject
    protected AIUserQuestionResultHistoryDao aiUserQuestionResultHistoryDao;
    @Inject
    protected AIUserQuestionResultCollectionDao aiUserQuestionResultCollectionDao;
    @Inject
    protected AIUserUnitResultPlanDao aiUserUnitResultPlanDao;
    @Inject
    protected AIUserBookResultDao aiUserBookResultDao;
    @Inject
    protected AIUserVideoDao aiUserVideoDao;
    @Inject
    protected UserOfficialProductBuyCacheManager userOfficialProductBuyCacheManager;

    @Inject
    protected AIUserQuestionResultProcessor aiUserQuestionResultProcessor;

    @Inject
    protected AILessonPlayDao aiLessonPlayDao;

    @Inject
    protected AIUserLessonBookSupport aiUserLessonBookSupport;

    @Inject
    protected ChipCourseSupport chipCourseSupport;

    @Inject
    protected ChipsContentService chipsContentService;

    @Inject
    protected AIUserQuestionResultCollectionQueueProducer aiUserQuestionResultCollectionQueueProducer;

    @Inject
    protected StoneDataLoaderClient stoneDataLoaderClient;

    @Inject
    protected ChipEnglishInvitationPersistence chipEnglishInvitationPersistence;

    @Inject
    protected ChipsEnglishUserExtSplitDao chipsEnglishUserExtSplitDao;

    @Inject
    protected ChipsEnglishProductTimetableDao chipsEnglishProductTimetableDao;

    @Inject
    protected AiChipsEnglishConfigServiceImpl chipsEnglishConfigService;

    @Inject
    protected ChipsUserCoursePersistence chipsUserCoursePersistence;

    @Inject
    protected ChipsMessageService chipsMessageService;

    @Inject
    protected ChipsUserService chipsUserService;

    @Inject
    protected ChipsActivityInvitationPersistence chipsActivityInvitationPersistence;

    @Inject
    protected ChipsWechatUserUnitResultHistoryDao chipsWechatUserUnitResultHistoryDao;

    @Inject
    protected ChipsWechatUserLessonResultHistoryDao chipsWechatUserLessonResultHistoryDao;

    @Inject
    protected ChipsWechatUserQuestionResultHistoryDao chipsWechatUserQuestionResultHistoryDao;

    @Inject
    protected ChipsUserDrawingTaskPersistence chipsUserDrawingTaskPersistence;

    protected final static String ClassImgStr = "img_url";
    protected final static String AiTeacher = "ai_teacher";

    protected final static List<String> TEST_DAILY_SHARE_COUPONS = Arrays.asList("5b6bbe198edbc85553e12aa6",
            "5b6bbff3ac74592b30d99e71",
            "5b6bc070ac74592b30d99e74",
            "5b6bc100ac74592b30d99e76",
            "5b70ed88ac7459b58bf3589a",
            "5b70edd4ac7459b58bf3589c",
            "5b70ee468edbc8bb7aacc9cd",
            "5b70eeb58edbc8bb7aacc9cf",
            "5b70eeedac7459b58bf358a2",
            "5b70ef198edbc8bb7aacc9d5");

    protected final static List<String> ONLINE_DAILY_SHARE_COUPONS = Arrays.asList("5b73d6035272e5e845d6c1f8",
            "5b73d6366816f84db5411637",
            "5b73d66c5272e5e845d6c2c5",
            "5b73d6ae5272e5e845d6c344",
            "5b73d6ee6816f84db5411798",
            "5b73d7275272e5e845d6c42b",
            "5b73d7536816f84db5411856",
            "5b73d78d6816f84db54118c7",
            "5b73d7b95272e5e845d6c537",
            "5b73d7e66816f84db541196c");
    protected final static String SHARE_RECORD_CHANNEL = "SHARE_RECORD";


    protected static final String COUPON_SEND_UNIT_IDX_KEY = "chips_english_coupon_unit_idx";
    protected static final String TRAVEL_ENGLISH_BOOK_ID_KEY = "chips_english_current_book_id";
    protected static final String CHIPS_ENGLISH_BOOK_IDS_KEY = "chips_english_current_book_ids";


    protected void notifyBookResult(Long userId, String bookId) {
        Set<String> productIds = chipsUserService.loadUserBoughtProduct(userId);
        if (CollectionUtils.isEmpty(productIds)) {
            return;
        }

        OrderProduct orderProduct = Optional.ofNullable(userOrderLoaderClient.loadOrderProductByAppItemIds(Collections.singleton(bookId)))
                .map(e -> e.get(bookId))
                .map(e -> e.stream().filter(e1 -> productIds.contains(e1.getId())).findFirst().orElse(null))
                .orElse(null);

        if (orderProduct == null) {
            return;
        }

        if(!OrderProductUtil.isShortProduct(orderProduct)){
            return;
        }

        ChipsEnglishProductTimetable timetable = chipsEnglishProductTimetableDao.load(orderProduct.getId());
        Date beginDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getBeginDate).orElse(new Date());
        Date endDate = Optional.ofNullable(timetable).map(ChipsEnglishProductTimetable::getEndDate).orElse(new Date());

        Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
        templateDataMap.put("first", new WechatTemplateData("快来看看宝贝的定级报告", "#FF6551"));
        templateDataMap.put("keyword1", new WechatTemplateData(orderProduct.getName(), "#1BA9EF"));
        templateDataMap.put("keyword2", new WechatTemplateData(DateUtils.dateToString(DateUtils.addDays(beginDate, -1), FORMAT_SQL_DATE) + " - " + DateUtils.dateToString(endDate, FORMAT_SQL_DATE), "#1BA9EF"));
        templateDataMap.put("remark", new WechatTemplateData("\n\r→点击这里查看定级报告", "#FF6551"));
        try {
            Map<String, Object> map = new HashMap<>();
            map.put("url", WechatConfig.getBaseSiteUrl() + "/chips/center/report.vpage?book=" + bookId);
            wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(userId, WechatTemplateMessageType.CHIPS_STUDY_DAILY_SUMMARY.name(), templateDataMap, map);
        } catch (Exception e) {
            logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", userId, templateDataMap, e);
        }
    }

    protected LessonType getLessonType(String name) {
        if (StringUtils.equals(name, "热身")) {
            return LessonType.WarmUp;
        } else if (StringUtils.equals(name, "情景对话")) {
            return LessonType.Dialogue;
        } else if (StringUtils.equals(name, "任务")) {
            return LessonType.Task;
        }
        return null;
    }

    /**
     * @see ChipCourseSupport#fetchUnitListExcludeTrialV2
     */
    @Deprecated
    protected List<NewBookCatalog> fetchUnitListExcludeTrial(String bookId) {
        return chipCourseSupport.fetchUnitListExcludeTrial(bookId);
    }


    protected List<String> currentChipsEnglishBookIds(){
        try {
            ChipsEnglishPageContentConfig obj = chipsEnglishConfigService.loadChipsConfigByName(CHIPS_ENGLISH_BOOK_IDS_KEY);
            String englishBookId = SafeConverter.toString(Optional.ofNullable(obj).map(ChipsEnglishPageContentConfig::getValue)
                    .map(String::trim).orElse(""));
            // Book id list
            return  Arrays.asList(englishBookId.split(","));
        } catch (Exception e) {
            return Collections.emptyList();
        }

    }

}
