/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.advertisement.client.UserAdvertisementServiceClient;
import com.voxlearning.utopia.service.afenti.consumer.*;
import com.voxlearning.utopia.service.business.consumer.*;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.config.client.PageBlockContentServiceClient;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.content.consumer.*;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamReportLoaderClient;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamResultLoaderClient;
import com.voxlearning.utopia.service.newexam.consumer.client.NewExamServiceClient;
import com.voxlearning.utopia.service.newhomework.api.service.VacationHomeworkService;
import com.voxlearning.utopia.service.newhomework.consumer.*;
import com.voxlearning.utopia.service.news.client.JxtNewsLoaderClient;
import com.voxlearning.utopia.service.news.client.JxtNewsServiceClient;
import com.voxlearning.utopia.service.order.api.loader.UserOrderLoader;
import com.voxlearning.utopia.service.order.api.service.UserOrderService;
import com.voxlearning.utopia.service.psr.consumer.UtopiaPsrServiceClient;
import com.voxlearning.utopia.service.question.consumer.*;
import com.voxlearning.utopia.service.reward.client.newversion.NewRewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardLoaderClient;
import com.voxlearning.utopia.service.reward.consumer.RewardServiceClient;
import com.voxlearning.utopia.service.sms.consumer.SmsServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.service.integral.ClazzIntegralService;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.*;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.utopia.service.wonderland.api.WonderlandLoader;
import com.voxlearning.utopia.service.zone.client.*;
import com.voxlearning.washington.cache.WashingtonCacheSystem;
import com.voxlearning.washington.controller.TikuStrategy;
import com.voxlearning.washington.helpers.HomeworkResultProcessor;
import com.voxlearning.washington.helpers.SmsServiceHelper;
import com.voxlearning.washington.service.homework.LoadHomeworkHelper;
import lombok.Getter;

import javax.inject.Inject;

abstract public class AbstractClientController extends SpringContainerSupport {

    @Inject private RaikouSystem raikouSystem;

    // In Alphabetical Order
    @Inject protected AfentiLoaderClient afentiLoaderClient;
    @Inject protected AfentiServiceClient afentiServiceClient;
    @Inject protected AfentiCastleServiceClient afentiCastleServiceClient;
    @Inject protected AfentiElfServiceClient afentiElfServiceClient;
    @Inject protected AfentiActivityServiceClient afentiActivityServiceClient;
    @Inject protected AppMessageServiceClient appMessageServiceClient;
    @Inject protected AsyncGroupServiceClient asyncGroupServiceClient;

    @Inject protected BusinessClazzIntegralServiceClient businessClazzIntegralServiceClient;
    @Inject protected BusinessFinanceServiceClient businessFinanceServiceClient;
    @Inject protected BusinessHomeworkServiceClient businessHomeworkServiceClient;
    @Inject protected BusinessStudentServiceClient businessStudentServiceClient;
    @Inject protected BusinessTeacherServiceClient businessTeacherServiceClient;

    @Inject protected ChineseContentLoaderClient chineseContentLoaderClient;
    @Inject protected ClazzBookLoaderClient clazzBookLoaderClient;
    @Inject protected ClazzJournalServiceClient clazzJournalServiceClient;
    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected ClazzServiceClient clazzServiceClient;
    @Inject protected ClazzZoneServiceClient clazzZoneServiceClient;
    @Inject protected ContentLoaderClient contentLoaderClient;
    @Inject protected ContentServiceClient contentServiceClient;

    @Inject protected EnglishContentLoaderClient englishContentLoaderClient;

    @Inject protected FlashGameServiceClient flashGameServiceClient;

    @Inject protected GiftLoaderClient giftLoaderClient;
    @Getter
    @Inject protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject protected DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject protected GroupServiceClient groupServiceClient;

    @Inject protected HomeworkCommentLoaderClient homeworkCommentLoaderClient;

    @Deprecated
    @Inject protected InvitationLoaderClient deprecatedInvitationLoaderClient;
    @Deprecated
    @Inject protected InvitationServiceClient deprecatedInvitationServiceClient;
    @Inject protected NewInvitationServiceClient newInvitationServiceClient;

    @Inject protected JxtServiceClient jxtServiceClient;
    @Inject protected JxtLoaderClient jxtLoaderClient;
    @Inject protected JxtNewsLoaderClient jxtNewsLoaderClient;
    @Inject protected JxtNewsServiceClient jxtNewsServiceClient;

    @Inject protected LoadHomeworkHelper loadHomeworkHelper;

    @Inject protected MathContentLoaderClient mathContentLoaderClient;
    @Inject protected MiscLoaderClient miscLoaderClient;
    @Inject protected MiscServiceClient miscServiceClient;
    @Inject protected MissionLoaderClient missionLoaderClient;

    @Inject protected NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;
    @Inject protected NewClazzBookLoaderClient newClazzBookLoaderClient;
    @Inject protected NewContentLoaderClient newContentLoaderClient;
    @Inject protected NewContentServiceClient newContentServiceClient;
    @Inject protected NewExamLoaderClient newExamLoaderClient;
    @Inject protected NewExamServiceClient newExamServiceClient;
    @Inject protected NewExamReportLoaderClient newExamReportLoaderClient;
    @Inject protected NewExamResultLoaderClient newExamResultLoaderClient;
    @Inject protected NewHomeworkServiceClient newHomeworkServiceClient;
    @Inject protected NewHomeworkContentServiceClient newHomeworkContentServiceClient;
    @Inject protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject protected NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject protected NewHomeworkProcessResultLoaderClient newHomeworkProcessResultLoaderClient;
    @Inject protected NewHomeworkReportServiceClient newHomeworkReportServiceClient;
    @Inject protected NewHomeworkPartLoaderClient newHomeworkPartLoaderClient;
    @Inject protected NewEnglishContentLoaderClient newEnglishContentLoaderClient;
    @Inject protected NewChineseContentLoaderClient newChineseContentLoaderClient;
    @Inject protected NewHomeworkCacheServiceClient newHomeworkCacheServiceClient;

    @Inject protected OfflineHomeworkServiceClient offlineHomeworkServiceClient;
    @Inject protected OfflineHomeworkLoaderClient offlineHomeworkLoaderClient;

    @Getter
    @Inject protected PageBlockContentServiceClient pageBlockContentServiceClient;
    @Inject protected PaperLoaderClient paperLoaderClient;
    @Inject protected TikuStrategy tikuStrategy;
    @Getter
    @Inject protected ParentLoaderClient parentLoaderClient;
    @Inject protected ParentMessageServiceClient parentMessageServiceClient;
    @Inject protected ParentServiceClient parentServiceClient;
    @Inject protected PersonalZoneLoaderClient personalZoneLoaderClient;
    @Inject protected PersonalZoneServiceClient personalZoneServiceClient;
    @Inject protected PictureBookHomeworkServiceClient pictureBookHomeworkServiceClient;
    @Inject protected PracticeLoaderClient practiceLoaderClient;
    @Inject protected PracticeServiceClient practiceServiceClient;

    @Inject protected QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject protected QuestionLoaderClient questionLoaderClient;

    @Getter
    @Inject protected ResearchStaffLoaderClient researchStaffLoaderClient;
    @Inject protected ResearchStaffServiceClient researchStaffServiceClient;
    @Inject protected RewardLoaderClient rewardLoaderClient;
    @Inject protected RewardServiceClient rewardServiceClient;
    @Inject protected NewRewardLoaderClient newRewardLoaderClient;

    @Inject protected ScoreCalculationLoaderClient scoreCalculationLoaderClient;
    @Inject protected SmsServiceClient smsServiceClient;
    @Inject protected StudentAccomplishmentLoaderClient studentAccomplishmentLoaderClient;
    @Getter
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected StudentServiceClient studentServiceClient;
    @Inject protected StudentSystemClazzServiceClient studentSystemClazzServiceClient;
    @Inject protected StudentMagicCastleServiceClient studentMagicCastleServiceClient;
    @Getter
    @Inject protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

    @Inject protected TeacherAlterationServiceClient teacherAlterationServiceClient;
    @Inject protected TeacherClazzServiceClient teacherClazzServiceClient;
    @Inject protected TeacherSystemClazzServiceClient teacherSystemClazzServiceClient;
    @Inject protected TeacherSystemClazzInfoServiceClient teacherSystemClazzInfoServiceClient;
    @Getter
    @Inject protected TeacherLoaderClient teacherLoaderClient;
    @Inject protected TeacherServiceClient teacherServiceClient;
    @Inject protected ThirdPartyLoaderClient thirdPartyLoaderClient;
    @Inject protected TinyGroupLoaderClient tinyGroupLoaderClient;
    @Inject protected TinyGroupServiceClient tinyGroupServiceClient;
    @Inject protected TtsListeningServiceClient ttsListeningServiceClient;

    @Inject protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject protected UserAttributeServiceClient userAttributeServiceClient;
    @Inject protected UserBookLoaderClient userBookLoaderClient;
    @Getter
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected UserLoginServiceClient userLoginServiceClient;
    @Inject protected UserServiceClient userServiceClient;
    @Inject protected UserWorkbookLoaderClient userWorkbookLoaderClient;
    @Inject protected UtopiaPsrServiceClient utopiaPsrServiceClient;

    @Inject protected VacationHomeworkLoaderClient vacationHomeworkLoaderClient;
    @Inject protected VacationHomeworkServiceClient vacationHomeworkServiceClient;
    @Inject protected VacationHomeworkReportLoaderClient vacationHomeworkReportLoaderClient;
    @Inject protected VacationHomeworkService vacationHomeworkService;
    @Inject protected VendorLoaderClient vendorLoaderClient;
    @Inject protected VendorServiceClient vendorServiceClient;
    @Inject protected VideoHomeworkServiceClient videoHomeworkServiceClient;
    @Inject protected VoiceRecommendLoaderClient voiceRecommendLoaderClient;
    @Inject protected DubbingScoreRecommendLoaderClient dubbingScoreRecommendLoaderClient;
    @Inject protected BasicReviewHomeworkLoaderClient basicReviewHomeworkLoaderClient;

    @Inject protected WashingtonCacheSystem washingtonCacheSystem;
    @Inject protected WechatLoaderClient wechatLoaderClient;
    @Inject protected WechatServiceClient wechatServiceClient;
    @Inject protected WordStockLoaderClient wordStockLoaderClient;
    @Inject protected WorkbookConfigLoaderClient workbookConfigLoaderClient;

    @Inject protected XxWorkbookContentLoaderClient xxWorkbookContentLoaderClient;
    @Inject protected XxWorkbookLoaderClient xxWorkbookLoaderClient;

    @Inject protected ZoneLoaderClient zoneLoaderClient;

    @Inject protected UserAdvertisementServiceClient userAdvertisementServiceClient;
    @Inject protected ReadReciteHomeworkServiceClient readReciteHomeworkServiceClient;
    @Inject protected DubbingHomeworkServiceClient dubbingHomeworkServiceClient;
    @Inject protected HomeworkResultProcessor homeworkResultProcessor;
    @Inject protected NewHomeworkQueueServiceClient newHomeworkQueueServiceClient;
    @Inject protected WordRecognitionHomeworkServiceClient wordRecognitionHomeworkServiceClient;
    @Inject protected OralCommunicationClient oralCommunicationClient;
    @Inject protected WordTeachHomeworkServiceClient wordTeachHomeworkServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;
    @ImportService(interfaceClass = ClazzIntegralService.class) protected ClazzIntegralService clazzIntegralService;
    @ImportService(interfaceClass = WonderlandLoader.class) protected WonderlandLoader wonderlandLoader;
    @ImportService(interfaceClass = UserOrderLoader.class) protected UserOrderLoader userOrderLoader;
    @ImportService(interfaceClass = UserOrderService.class) protected UserOrderService userOrderService;

    private SmsServiceHelper smsServiceHelper = null;

    public synchronized SmsServiceHelper getSmsServiceHelper() {
        if (smsServiceHelper == null) {
            smsServiceHelper = new SmsServiceHelper();
            smsServiceHelper.setRaikouSystem(raikouSystem);
            smsServiceHelper.setSmsServiceClient(smsServiceClient);
            smsServiceHelper.setUserLoaderClient(userLoaderClient);
        }
        return smsServiceHelper;
    }
}
