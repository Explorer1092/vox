/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.admin.controller.legacy;

import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;
import com.voxlearning.utopia.admin.service.crm.CrmUserService;
import com.voxlearning.utopia.admin.service.legacy.AfentiAdminService;
import org.slf4j.Logger;

import javax.annotation.Resource;

abstract public class AbstractAdminLegacyController extends AbstractAdminSystemController {
    protected Logger logger = LoggerFactory.getLogger(this.getClass());

    @Resource protected CrmUserService crmUserService;
    @Resource protected AfentiAdminService afentiAdminService;
}