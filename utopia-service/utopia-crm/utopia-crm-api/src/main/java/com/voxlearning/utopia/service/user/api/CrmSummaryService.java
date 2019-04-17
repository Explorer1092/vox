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

package com.voxlearning.utopia.service.user.api;

import com.voxlearning.alps.annotation.remote.ServiceRetries;
import com.voxlearning.alps.annotation.remote.ServiceTimeout;
import com.voxlearning.alps.annotation.remote.ServiceVersion;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.alps.spi.common.IPingable;
import com.voxlearning.utopia.api.constant.CrmContactType;
import com.voxlearning.utopia.api.constant.CrmTaskRecordCategory;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;

import java.util.List;
import java.util.concurrent.TimeUnit;

/**
 * Created by Summer Yang on 2016/5/10.
 */
@ServiceVersion(version = "2.0.0")
@ServiceTimeout(timeout = 30, unit = TimeUnit.SECONDS)
@ServiceRetries
public interface CrmSummaryService extends IPingable {

    MapMessage updateTeacherFakeType(Long teacherId, CrmTeacherFakeValidationType type, String desc);

    MapMessage removeTeacherFakeType(Long teacherId);

    MapMessage saveCrmTeacherFake(CrmTeacherFake fake);

    MapMessage updateCrmTeacherFake(String id, CrmTeacherFake fake);

    CrmTeacherFake loadCrmTeacherFake(String id);

    List<CrmTeacherFake> findFakedTeacher(Long teacherId);

    List<CrmTeacherFake> findFakerIdIs(Long fakerId);

    List<CrmTeacherFake> findTeacherIdIs(Long teacherId);

    Page<CrmTeacherFake> smartFindCrmTeacherFake(ReviewStatus reviewStatus, Long teacherId, Pageable pageable);

    MapMessage addWechatTaskRecord(Long userId, String recorder, String recorderName, CrmTaskRecordCategory recordCategory, String content);

    MapMessage addUserTaskRecord(Long userId, String recorder, String recorderName, CrmTaskRecordCategory recordCategory, CrmContactType contactType, String title, String content, Integer callTime, String audioUrl, String callerId);

}
