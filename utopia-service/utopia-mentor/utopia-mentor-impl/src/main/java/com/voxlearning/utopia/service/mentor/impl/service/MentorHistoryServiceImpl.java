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

package com.voxlearning.utopia.service.mentor.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.api.concurrent.AlpsFuture;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.remote.core.support.ValueWrapperFuture;
import com.voxlearning.utopia.entity.ucenter.MentorHistory;
import com.voxlearning.utopia.service.mentor.api.MentorHistoryService;
import com.voxlearning.utopia.service.mentor.impl.persistence.MentorHistoryPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

@Named("com.voxlearning.utopia.service.mentor.impl.service.MentorHistoryServiceImpl")
@ExposeService(interfaceClass = MentorHistoryService.class)
public class MentorHistoryServiceImpl extends SpringContainerSupport implements MentorHistoryService {

    @Inject private MentorHistoryPersistence mentorHistoryPersistence;

    @Override
    public AlpsFuture<List<MentorHistory>> loadAutoMentorRewardJobData() {
        return new ValueWrapperFuture<>(mentorHistoryPersistence.loadAutoMentorRewardJobData());
    }
}
