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

package com.voxlearning.utopia.service.user.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Page;
import com.voxlearning.alps.repackaged.org.springframework.data.domain.Pageable;
import com.voxlearning.utopia.api.constant.CrmContactType;
import com.voxlearning.utopia.api.constant.CrmTaskRecordCategory;
import com.voxlearning.utopia.api.constant.CrmTeacherFakeValidationType;
import com.voxlearning.utopia.api.constant.ReviewStatus;
import com.voxlearning.utopia.entity.crm.CrmTeacherFake;
import com.voxlearning.utopia.entity.crm.UserServiceRecord;
import com.voxlearning.utopia.service.user.api.CrmSummaryService;

import java.util.List;

/**
 * Created by Summer Yang on 2016/5/10.
 */
public class CrmSummaryServiceClient implements CrmSummaryService {

    @ImportService(interfaceClass = CrmSummaryService.class)
    private CrmSummaryService remoteReference;

    @Override
    public MapMessage updateTeacherFakeType(Long teacherId, CrmTeacherFakeValidationType type, String desc) {
        return remoteReference.updateTeacherFakeType(teacherId, type, desc);
    }

    public MapMessage removeTeacherFakeType(Long teacherId) {
        return remoteReference.removeTeacherFakeType(teacherId);
    }

    @Override
    public MapMessage saveCrmTeacherFake(CrmTeacherFake fake) {
        return remoteReference.saveCrmTeacherFake(fake);
    }

    @Override
    public MapMessage updateCrmTeacherFake(String id, CrmTeacherFake fake) {
        return remoteReference.updateCrmTeacherFake(id, fake);
    }

    @Override
    public CrmTeacherFake loadCrmTeacherFake(String id) {
        return remoteReference.loadCrmTeacherFake(id);
    }

    @Override
    public List<CrmTeacherFake> findFakedTeacher(Long teacherId) {
        return remoteReference.findFakedTeacher(teacherId);
    }

    @Override
    public List<CrmTeacherFake> findFakerIdIs(Long fakerId) {
        return remoteReference.findFakerIdIs(fakerId);
    }

    @Override
    public List<CrmTeacherFake> findTeacherIdIs(Long teacherId) {
        return remoteReference.findTeacherIdIs(teacherId);
    }

    @Override
    public Page<CrmTeacherFake> smartFindCrmTeacherFake(ReviewStatus reviewStatus, Long teacherId, Pageable pageable) {
        return remoteReference.smartFindCrmTeacherFake(reviewStatus, teacherId, pageable);
    }

    public MapMessage addWechatTaskRecord(Long userId, String recorder, String recorderName, CrmTaskRecordCategory recordCategory, String content) {
        if (userId == null || userId <= 0) {
            return MapMessage.errorMessage("用户ID为空");
        }
        if (recordCategory == null) {
            return MapMessage.errorMessage("记录分类为空");
        }
        if (StringUtils.isBlank(recorder)) {
            return MapMessage.errorMessage("记录人为空");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("记录内容为空");
        }
        return remoteReference.addWechatTaskRecord(userId, recorder, recorderName, recordCategory, content);
    }

    public MapMessage addUserTaskRecord(Long userId, String recorder, String recorderName, CrmTaskRecordCategory recordCategory, CrmContactType contactType, String title, String content, Integer callTime, String audioUrl, String callerId) {
        if (userId == null || userId <= 0) {
            return MapMessage.errorMessage("用户ID为空");
        }
        if (recordCategory == null) {
            return MapMessage.errorMessage("记录分类为空");
        }
        if (StringUtils.isBlank(content)) {
            return MapMessage.errorMessage("记录内容为空");
        }
        return remoteReference.addUserTaskRecord(userId, recorder, recorderName, recordCategory, contactType, title, content, callTime, audioUrl, callerId);
    }
}
