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

package com.voxlearning.utopia.service.newhomework.impl.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.content.client.PracticeServiceClient;
import com.voxlearning.utopia.service.content.consumer.*;
import com.voxlearning.utopia.service.newhomework.impl.athena.AthenaReviewLoaderClient;
import com.voxlearning.utopia.service.newhomework.impl.dao.*;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.basicreview.BasicReviewHomeworkReportDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkPackageDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkProcessResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.vacation.VacationHomeworkResultDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.DubbingRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.ImageTextRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.dao.voicerecommend.VoiceRecommendDao;
import com.voxlearning.utopia.service.newhomework.impl.loader.*;
import com.voxlearning.utopia.service.newhomework.impl.pubsub.NewHomeworkPublisher;
import com.voxlearning.utopia.service.newhomework.impl.service.DiagnoseReportImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkCacheServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkResultServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.NewHomeworkServiceImpl;
import com.voxlearning.utopia.service.newhomework.impl.service.helper.VoxScoreLevelHelper;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.report.DubbingWithScoreRecommendProcessor;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.finish.FH_CreateBasicReviewHomeworkReport;
import com.voxlearning.utopia.service.newhomework.impl.service.internal.student.work.DoHomeworkProcessor;
import com.voxlearning.utopia.service.question.consumer.*;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.client.GrayFunctionManagerClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;

abstract public class NewHomeworkSpringBean extends SpringContainerSupport {
    @Inject
    protected ChineseContentLoaderClient chineseContentLoaderClient;
    @Inject
    protected DoHomeworkProcessor doHomeworkProcessor;
    @Inject
    protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    protected NewAccomplishmentDao newAccomplishmentDao;
    @Inject
    protected NewHomeworkServiceImpl newHomeworkService;
    @Inject
    protected NewHomeworkLoaderImpl newHomeworkLoader;
    @Inject
    protected NewHomeworkResultLoaderImpl newHomeworkResultLoader;
    @Inject
    protected NewAccomplishmentLoaderImpl newAccomplishmentLoader;
    @Inject
    protected NewHomeworkResultServiceImpl newHomeworkResultService;
    @Inject
    protected NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;
    @Inject
    protected NewHomeworkCacheServiceImpl newHomeworkCacheService;
    @Inject
    protected NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject
    protected TeacherAssignmentRecordDao teacherAssignmentRecordDao;
    @Inject
    protected TotalAssignmentRecordDao totalAssignmentRecordDao;
    @Inject
    protected ParentLoaderClient parentLoaderClient;
    @Inject
    protected PictureBookLoaderClient pictureBookLoaderClient;
    @Inject
    protected QuestionLoaderClient questionLoaderClient;
    @Inject
    protected VideoLoaderClient videoLoaderClient;
    @Inject
    protected QuestionContentTypeLoaderClient questionContentTypeLoaderClient;
    @Inject
    protected StudentLoaderClient studentLoaderClient;
    @Inject
    protected TeacherLoaderClient teacherLoaderClient;
    @Inject
    protected TeachingObjectiveLoaderClient teachingObjectiveLoaderClient;
    @Inject
    protected TestMethodLoaderClient testMethodLoaderClient;
    @Inject
    protected UserLoaderClient userLoaderClient;
    @Inject
    protected NewClazzBookServiceClient newClazzBookServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;
    @Inject
    protected NewContentLoaderClient newContentLoaderClient;
    @Inject
    protected GrayFunctionManagerClient grayFunctionManagerClient;
    @Inject
    protected NewHomeworkPrizeDao newHomeworkPrizeDao;
    @Inject
    protected PracticeLoaderClient practiceLoaderClient;
    @Inject
    protected NewEnglishContentLoaderClient newEnglishContentLoaderClient;
    @Inject
    protected EnglishContentLoaderClient englishContentLoaderClient;
    @Inject
    protected HomeworkCommentPersistence homeworkCommentPersistence;
    @Inject
    protected NewUnreadHomeworkCommentDao unreadHomeworkCommentDao;
    @Inject
    protected VacationHomeworkPackageDao vacationHomeworkPackageDao;
    @Inject
    protected VacationHomeworkDao vacationHomeworkDao;
    @Inject
    protected VacationHomeworkResultDao vacationHomeworkResultDao;
    @Inject
    protected VacationHomeworkProcessResultDao vacationHomeworkProcessResultDao;
    @Inject
    protected VoiceRecommendDao voiceRecommendDao;
    @Inject
    protected FeatureLoaderClient featureLoaderClient;
    @Inject
    protected SolutionMethodRefLoaderClient solutionMethodRefLoaderClient;
    @Inject
    protected HomeworkTaskRecordDao homeworkTaskRecordDao;
    @Inject
    protected PracticeServiceClient practiceServiceClient;
    @Inject
    protected NewHomeworkPublisher newHomeworkPublisher;
    @Inject
    protected BasicReviewHomeworkPackageDao basicReviewHomeworkPackageDao;
    @Inject
    protected AthenaReviewLoaderClient athenaReviewLoaderClient;
    @Inject
    protected TeacherAssignmentRecordLoaderImpl teacherAssignmentRecordLoader;
    @Inject
    protected TotalAssignmentRecordLoaderImpl totalAssignmentRecordLoader;
    @Inject
    protected BasicReviewHomeworkCacheLoaderImpl basicReviewHomeworkCacheLoader;
    @Inject
    protected BasicReviewHomeworkReportDao basicReviewHomeworkReportDao;
    @Inject
    protected TermReviewLoaderClient termReviewLoaderClient;
    @Inject
    protected FH_CreateBasicReviewHomeworkReport fh_createBasicReviewHomeworkReport;
    @Inject
    protected DubbingLoaderClient dubbingLoaderClient;
    @Inject
    protected PictureBookPlusHistoryDao pictureBookPlusHistoryDao;
    @Inject
    protected PictureBookPlusRecommendRecordDao pictureBookPlusRecommendRecordDao;
    @Inject
    protected AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject
    protected DubbingWithScoreRecommendProcessor dubbingWithScoreRecommendProcessor;
    @Inject
    protected DubbingRecommendDao dubbingRecommendDao;
    @Inject
    protected DiagnoseReportImpl diagnoseReport;
    @Inject
    protected IntelDiagnosisClient intelDiagnosisClient;
    @Inject
    protected StoneDataLoaderClient stoneDataLoaderClient;
    @Inject
    protected VoxScoreLevelHelper voxScoreLevelHelper;
    @Inject
    protected ImageTextRecommendDao imageTextRecommendDao;
    @Inject
    protected OralCommunicationRecommendRecordDao oralCommunicationRecommendRecordDao;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;
}
