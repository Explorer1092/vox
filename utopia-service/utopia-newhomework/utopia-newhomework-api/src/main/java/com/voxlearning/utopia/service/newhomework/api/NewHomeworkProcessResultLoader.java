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

package com.voxlearning.utopia.service.newhomework.api;

import com.voxlearning.alps.annotation.remote.*;
import com.voxlearning.alps.api.cyclops.CyclopsMonitor;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.service.newhomework.api.context.CorrectHomeworkContext;
import com.voxlearning.utopia.service.newhomework.api.mapper.NewHomeworkProcessResult;

import java.util.Collection;
import java.util.Map;
import java.util.concurrent.TimeUnit;

@ServiceVersion(version = "20170215")
@ServiceTimeout(timeout = 10, unit = TimeUnit.SECONDS)
@ServiceRetries(retries = 2)
@CyclopsMonitor("utopia")
public interface NewHomeworkProcessResultLoader extends IPingable {

//    @Idempotent
//    @Deprecated
//    NewHomeworkProcessResult load(String id);

    @Idempotent
    NewHomeworkProcessResult load(String homeworkId, String id);

//    @Idempotent
//    @Deprecated
//    Map<String, NewHomeworkProcessResult> loads(Collection<String> ids);

    @Idempotent
    Map<String, NewHomeworkProcessResult> loads(String homeworkId, Collection<String> ids);

    /**
     * 更新老师对题的批改信息
     * 参数有点多，原因是为了避免许多校验带来的性能损耗，直接在update的时候都做了。
     * 注:correctType 批改类型，这个属性在未来一段时间可能都是空，在dao中默认给出CORRECT
     */
    @ServiceMethod(timeout = 10, unit = TimeUnit.SECONDS)
    Boolean updateCorrection(CorrectHomeworkContext context);
}
