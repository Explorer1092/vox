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

package com.voxlearning.utopia.admin.service;

import com.voxlearning.alps.dao.jdbc.factory.UtopiaSqlFactory;
import com.voxlearning.alps.dao.jdbc.template.UtopiaSql;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.business.consumer.BusinessTeacherServiceClient;
import com.voxlearning.utopia.service.content.consumer.PracticeLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkLoaderClient;
import com.voxlearning.utopia.service.newhomework.consumer.NewHomeworkServiceClient;
import com.voxlearning.utopia.service.user.client.UserLoginServiceClient;
import com.voxlearning.utopia.service.user.consumer.*;
import com.voxlearning.utopia.service.vendor.consumer.VendorLoaderClient;
import com.voxlearning.utopia.service.vendor.consumer.VendorServiceClient;

import javax.inject.Inject;

abstract public class AbstractCrmService extends SpringContainerSupport {

    protected UtopiaSql utopiaSql;
    protected UtopiaSql utopiaSqlHomework;
    @Inject
    private UtopiaSqlFactory utopiaSqlFactory;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        utopiaSql = utopiaSqlFactory.getDefaultUtopiaSql();
        utopiaSqlHomework = utopiaSqlFactory.getUtopiaSql("homework");
    }

    @Inject
    protected BusinessTeacherServiceClient businessTeacherServiceClient;
    @Inject
    protected GroupServiceClient groupServiceClient;
    @Inject
    protected NewHomeworkServiceClient newHomeworkServiceClient;
    @Inject
    protected NewHomeworkLoaderClient newHomeworkLoaderClient;
    @Inject
    protected ParentLoaderClient parentLoaderClient;
    @Inject
    protected ParentServiceClient parentServiceClient;
    @Inject
    protected PracticeLoaderClient practiceLoaderClient;
    @Deprecated
    @Inject
    protected DeprecatedSchoolServiceClient deprecatedSchoolServiceClient;
    @Inject
    protected StudentLoaderClient studentLoaderClient;
    @Inject
    protected UserLoaderClient userLoaderClient;
    @Inject
    protected UserLoginServiceClient userLoginServiceClient;
    @Inject
    protected UserServiceClient userServiceClient;
    @Inject
    protected VendorLoaderClient vendorLoaderClient;
    @Inject
    protected VendorServiceClient vendorServiceClient;
    @Inject
    protected DeprecatedGroupLoaderClient deprecatedGroupLoaderClient;
    @Inject
    protected DeprecatedClazzLoaderClient deprecatedClazzLoaderClient;
    @Inject
    protected ClazzServiceClient clazzServiceClient;
    @Inject
    protected TeacherLoaderClient teacherLoaderClient;

    @Inject
    protected SensitiveUserDataServiceClient sensitiveUserDataServiceClient;

}
