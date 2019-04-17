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
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.client.INewExamRegistrationLoaderClient;
import com.voxlearning.utopia.service.newexam.api.entity.NewExamRegistration;
import com.voxlearning.utopia.service.newexam.api.loader.NewExamRegistrationLoader;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamForExport;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamRegistrationLoaderMapper;
import lombok.Getter;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * Created by tanguohong on 2016/3/7.
 */
public class NewExamRegistrationLoaderClient implements INewExamRegistrationLoaderClient {

    @Getter
    @ImportService(interfaceClass = NewExamRegistrationLoader.class)
    private NewExamRegistrationLoader remoteReference;

    @Override
    public NewExamRegistration loadById(String id) {
        return remoteReference.loadById(id);
    }

    @Override
    public Map<String, NewExamRegistration> loadByIds(Collection<String> ids) {
        return remoteReference.loadByIds(ids);
    }

    @Override
    public MapMessage loadByNewExamIdAndPage(NewExamRegistrationLoaderMapper newExamRegistrationLoaderMapper) {
        return remoteReference.loadByNewExamIdAndPage(newExamRegistrationLoaderMapper);
    }

    @Override
    public List<NewExamForExport> loadByNewExam(String newExamId) {
        return remoteReference.loadByNewExam(newExamId);
    }
}
