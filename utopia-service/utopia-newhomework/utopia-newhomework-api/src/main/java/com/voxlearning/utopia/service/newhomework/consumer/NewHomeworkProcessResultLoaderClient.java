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

package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.utopia.service.newhomework.api.NewHomeworkProcessResultLoader;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;

import java.util.Collection;
import java.util.Map;

public class NewHomeworkProcessResultLoaderClient implements NewHomeworkProcessResultLoader {

    @ImportService(interfaceClass = NewHomeworkProcessResultLoader.class)
    private NewHomeworkProcessResultLoader remoteReference;

//    @Override
//    @Deprecated
//    public NewHomeworkProcessResult load(String id) {
//        return remoteReference.load(id);
//    }

    @Override
    public NewHomeworkProcessResult load(String homeworkId, String id) {
        return remoteReference.load(homeworkId, id);
    }

//    @Override
//    @Deprecated
//    public Map<String, NewHomeworkProcessResult> loads(Collection<String> ids) {
//        return remoteReference.loads(CollectionUtils.toLinkedHashSet(ids));
//    }

    @Override
    public Map<String, NewHomeworkProcessResult> loads(String homeworkId, Collection<String> ids) {
        return remoteReference.loads(homeworkId, ids);
    }

    @Override
    public Boolean updateCorrection(CorrectHomeworkContext context) {
        return remoteReference.updateCorrection(context);
    }
}
