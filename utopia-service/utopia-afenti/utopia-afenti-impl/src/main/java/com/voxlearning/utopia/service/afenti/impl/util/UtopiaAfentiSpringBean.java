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

package com.voxlearning.utopia.service.afenti.impl.util;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.afenti.impl.dao.*;
import com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiGuidePersistence;
import com.voxlearning.utopia.service.afenti.impl.persistence.UserAfentiStatsPersistence;
import com.voxlearning.utopia.service.content.consumer.NewContentLoaderClient;
import com.voxlearning.utopia.service.content.consumer.NewKnowledgePointLoaderClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.consumer.DeprecatedClazzLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.StudentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.UserLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;

import javax.inject.Inject;

abstract public class UtopiaAfentiSpringBean extends SpringContainerSupport {
    @Inject private UtopiaSqlFactory utopiaSqlFactory;

    protected UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    // utopia-afenti persistence
    @Inject protected AfentiLearningPlanUnitRankManagerPersistence afentiLearningPlanUnitRankManagerPersistence;
    @Inject protected AfentiLearningPlanUserBookRefPersistence afentiLearningPlanUserBookRefPersistence;
    @Inject protected AfentiLearningPlanUserRankStatPersistence afentiLearningPlanUserRankStatPersistence;
    @Inject protected AfentiLearningPlanUserFootprintPersistence afentiLearningPlanUserFootprintPersistence;
    @Inject protected AfentiUserAchievementRecordPersistence afentiUserAchievementRecordPersistence;
    @Inject protected AfentiInvitationRecordPersistence afentiInvitationRecordPersistence;

    // utopia-dao persistence
    @Inject protected UserActivatedProductPersistence userActivatedProductPersistence;

    // dao
    @Inject protected UserAfentiStatsPersistence userAfentiStatsPersistence;
    @Inject protected UserAfentiGuidePersistence userAfentiGuidePersistence;
    @Inject protected WrongQuestionLibraryDao wrongQuestionLibraryDao;
    @Inject protected AfentiLoginDetailDao afentiLoginDetailDao;
    @Inject protected AfentiLearningPlanPushExamHistoryDao afentiLearningPlanPushExamHistoryDao;
    @Inject protected AfentiQuizResultDao afentiQuizResultDao;
    @Inject protected AfentiQuizStatDao afentiQuizStatDao;
    @Inject protected UserCourseRefDao userCourseRefDao;

    // client
    @Inject protected StudentLoaderClient studentLoaderClient;
    @Inject protected NewContentLoaderClient newContentLoaderClient;
    @Inject protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject protected UserLoaderClient userLoaderClient;
    @Inject protected ParentLoaderClient parentLoaderClient;
    @Inject protected NewKnowledgePointLoaderClient newKnowledgePointLoaderClient;
    @Inject protected AppMessageServiceClient appMessageServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;
}
