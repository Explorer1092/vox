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

package com.voxlearning.utopia.service.zone.impl.manager;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.PageImpl;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.service.zone.api.ClazzJournalManager;
import com.voxlearning.utopia.service.zone.api.entity.LikeDetail;
import com.voxlearning.utopia.service.zone.impl.persistence.ClazzJournalPersistence;
import com.voxlearning.utopia.service.zone.impl.persistence.LikeDetailPersistence;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.Date;

@Named
@ExposeService(interfaceClass = ClazzJournalManager.class)
public class ClazzJournalManagerImpl extends SpringContainerSupport implements ClazzJournalManager {

    @Inject private ClazzJournalPersistence clazzJournalPersistence;
    @Inject private LikeDetailPersistence likeDetailPersistence;

    @Override
    public int deleteClazzJournal(Long id) {
        return id == null ? 0 : clazzJournalPersistence.delJournal(id);
    }

    @Override
    public Page<LikeDetail> findLikeDetails(Date start, Date end, Pageable pageable) {
        if (start == null || end == null || pageable == null) {
            return new PageImpl<>(Collections.emptyList());
        }
        if (start.getTime() > end.getTime() || pageable.getPageSize() <= 0 || pageable.getPageNumber() < 0) {
            return new PageImpl<>(Collections.emptyList());
        }
        return likeDetailPersistence.findByCreateDatetimeRange(start, end, pageable);
    }
}
