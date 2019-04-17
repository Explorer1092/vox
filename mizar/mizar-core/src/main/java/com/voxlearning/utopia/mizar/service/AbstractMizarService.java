/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.mizar.service;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarChangeRecordLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.loader.MizarUserLoaderClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarChangeRecordServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarNotifyServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarServiceClient;
import com.voxlearning.utopia.service.mizar.consumer.service.MizarUserServiceClient;

import javax.inject.Inject;

/**
 * Base Abstract Mizar Service Class.
 * <p>
 * Created by Yuechen.Wang on 16-9-19.
 */
abstract public class AbstractMizarService extends SpringContainerSupport {

    // Loader
    @Inject protected MizarLoaderClient mizarLoaderClient;
    @Inject protected MizarUserLoaderClient mizarUserLoaderClient;
    @Inject protected MizarChangeRecordLoaderClient mizarChangeRecordLoaderClient;

    // Service
    @Inject protected MizarServiceClient mizarServiceClient;
    @Inject protected MizarUserServiceClient mizarUserServiceClient;
    @Inject protected MizarChangeRecordServiceClient mizarChangeRecordServiceClient;
    @Inject protected MizarNotifyServiceClient mizarNotifyServiceClient;

}
