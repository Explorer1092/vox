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

package com.voxlearning.utopia.service.business.consumer;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.business.api.BusinessHomeworkService;

import java.util.List;
import java.util.Map;

public class BusinessHomeworkServiceClient implements BusinessHomeworkService {
    @ImportService(interfaceClass = BusinessHomeworkService.class)
    private BusinessHomeworkService remoteReference;

    @Override
    public List<Map<String, String>> getBookInfo(String type, String queryStr) {
        return remoteReference.getBookInfo(type, queryStr);
    }

    @Override
    public MapMessage getReadingDraftByReadingId(Long readingId){
        return remoteReference.getReadingDraftByReadingId(readingId);
    }

}
