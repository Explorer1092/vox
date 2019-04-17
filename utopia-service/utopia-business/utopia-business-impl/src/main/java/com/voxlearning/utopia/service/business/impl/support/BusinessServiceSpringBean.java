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

package com.voxlearning.utopia.service.business.impl.support;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.impl.dao.*;
import com.voxlearning.utopia.service.business.impl.persistence.ActivityDataPersistence;
import com.voxlearning.utopia.service.business.impl.persistence.ExamAnswerPersistence;
import com.voxlearning.utopia.service.business.impl.persistence.ExamAnswerRecordPersistence;
import com.voxlearning.utopia.service.clazz.client.AsyncGroupServiceClient;
import com.voxlearning.utopia.service.content.consumer.*;
import com.voxlearning.utopia.service.newhomework.consumer.HomeworkCommentServiceClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewAccomplishmentLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkResultLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderLoaderClient;
import com.voxlearning.utopia.service.order.consumer.UserOrderServiceClient;
import com.voxlearning.utopia.service.user.api.UserIntegralService;
import com.voxlearning.utopia.service.user.api.service.integral.ClazzIntegralService;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.AppMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.ParentMessageServiceClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;

import javax.inject.Inject;

/**
 * Business service base spring bean.
 *
 * @author Xiaohai Zhang
 * @since Jan 26, 2015
 */
abstract public class BusinessServiceSpringBean extends SpringContainerSupport {

    @Inject
    protected UtopiaSqlFactory utopiaSqlFactory;
    @Inject
    protected BusinessCacheSystem businessCacheSystem;

    protected UtopiaSql utopiaSql;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
    }

    @Inject
    protected ReadingPartsPersistence readingPartsPersistence;
    @Inject
    protected ReadingPersistence readingPersistence;
    @Inject
    protected ReadingQuestionsPersistence readingQuestionsPersistence;
    @Inject
    protected ActivityDataPersistence activityDataPersistence;
    @Inject
    protected ExamAnswerPersistence examAnswerPersistence;
    @Inject
    protected ExamAnswerRecordPersistence examAnswerRecordPersistence;
    @Inject
    protected UserOrderLoaderClient userOrderLoaderClient;
    @Inject
    protected UserOrderServiceClient userOrderServiceClient;
    @Inject
    protected NewAccomplishmentLoaderClient newAccomplishmentLoaderClient;
    @Inject
    protected ClazzBookLoaderClient clazzBookLoaderClient;
    @Inject
    protected ClazzBookServiceClient clazzBookServiceClient;
    @Inject
    protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    protected ContentServiceClient contentServiceClient;
    @Inject
    protected EnglishContentLoaderClient englishContentLoaderClient;
    @Inject
    protected DeprecatedGroupLoaderClient groupLoaderClient;
    @Inject
    protected HomeworkCommentServiceClient homeworkCommentServiceClient;
    @Inject
    protected NewContentLoaderClient newContentLoaderClient;
    @Inject
    protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject
    protected NewHomeworkResultLoaderClient newHomeworkResultLoaderClient;
    @Inject
    @Deprecated
    protected InvitationLoaderClient deprecatedInvitationLoaderClient;
    @Inject
    @Deprecated
    protected InvitationServiceClient deprecatedInvitationServiceClient;
    @Inject
    protected MathContentLoaderClient mathContentLoaderClient;
    @Inject
    protected ParentLoaderClient parentLoaderClient;
    @Inject
    protected ParentMessageServiceClient parentMessageServiceClient;
    @Deprecated
    @Inject
    protected DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;
    @Inject
    protected StudentLoaderClient studentLoaderClient;
    @Inject
    protected AsyncGroupServiceClient asyncGroupServiceClient;
    @Inject
    protected TeacherLevelServiceClient teacherLevelServiceClient;
    @Inject
    protected TeacherLoaderClient teacherLoaderClient;
    @Inject
    protected TeacherServiceClient teacherServiceClient;
    @Inject
    protected UserAggregationLoaderClient userAggregationLoaderClient;
    @Inject
    protected UserAttributeLoaderClient userAttributeLoaderClient;
    @Inject
    protected UserAttributeServiceClient userAttributeServiceClient;
    @Inject
    protected UserLoaderClient userLoaderClient;
    @Inject
    protected UserLoginServiceClient userLoginServiceClient;
    @Inject
    protected UserServiceClient userServiceClient;
    @Inject
    protected VendorLoaderClient vendorLoaderClient;
    @Inject
    protected WechatLoaderClient wechatLoaderClient;
    @Inject
    protected WechatServiceClient wechatServiceClient;
    @Inject
    protected UserTagLoaderClient userTagLoaderClient;
    @Inject
    protected StudentMagicCastleRecordPersistence studentMagicCastleRecordPersistence;
    @Inject
    protected StudentMagicLevelPersistence studentMagicLevelPersistence;

    @Inject
    protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;
    @Inject
    protected AppMessageServiceClient appMessageServiceClient;

    @ImportService(interfaceClass = UserIntegralService.class) protected UserIntegralService userIntegralService;
    @ImportService(interfaceClass = ClazzIntegralService.class) protected ClazzIntegralService clazzIntegralService;
}
