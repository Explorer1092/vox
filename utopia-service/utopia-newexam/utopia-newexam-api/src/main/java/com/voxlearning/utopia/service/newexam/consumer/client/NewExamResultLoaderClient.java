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

package com.voxlearning.utopia.service.newexam.consumer.client;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newexam.api.client.INewExamResultLoaderClient;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamResult;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamResultLoader;
import lombok.Getter;

import java.util.Collection;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/7.
 */
public class NewExamResultLoaderClient implements INewExamResultLoaderClient {

    @Getter
    @ImportService(interfaceClass = NewExamResultLoader.class)
    private NewExamResultLoader remoteReference;

    @Override
    public NewExamResult loadNewExamResult(String newExamId, Long userId) {
        return remoteReference.loadNewExamResult(newExamId, userId);
    }

    @Override
    public Map<Long, NewExamResult> loadNewExamResult(String newExamId, Collection<Long> userIds) {
        return remoteReference.loadNewExamResult(newExamId, userIds);
    }

    @Override
    public Map<String, NewExamResult> loadNewExamResults(Collection<String> newExamResultIds) {
        return remoteReference.loadNewExamResults(newExamResultIds);
    }
}
