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

package com.voxlearning.utopia.service.wechat.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilder;
import com.voxlearning.alps.spi.cache.AtomicCallbackBuilderFactory;
import com.voxlearning.alps.spi.exception.cache.DuplicatedOperationException;
import com.voxlearning.alps.spi.queue.Message;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;
import com.voxlearning.utopia.service.wechat.api.UserMiniProgramCheckService;
import com.voxlearning.utopia.service.wechat.api.WechatService;
import com.voxlearning.utopia.service.wechat.api.constants.*;
import com.voxlearning.utopia.service.wechat.api.data.WechatNoticeUpdater;
import com.voxlearning.utopia.service.wechat.api.entities.*;
import com.voxlearning.utopia.service.wechat.api.mapper.BindResultMapper;
import lombok.Getter;
import org.slf4j.Logger;

import java.util.*;
import java.util.function.Consumer;

public class MiniProgramServiceClient {

    @Getter
    @ImportService(interfaceClass = UserMiniProgramCheckService.class)
    private UserMiniProgramCheckService userMiniProgramCheckService;



}
