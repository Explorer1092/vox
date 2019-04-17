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

package com.voxlearning.utopia.service.newexam.impl.service;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newexam.api.DPNewExamService;
import com.voxlearning.utopia.service.newexam.api.mapper.NewExamRegistrationLoaderMapper;
import com.voxlearning.utopia.service.newexam.impl.loader.NewExamRegistrationLoaderImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;

@Named
@Service(interfaceClass = DPNewExamService.class)
@ExposeService(interfaceClass = DPNewExamService.class)
public class DPNewExamServiceImpl implements DPNewExamService {

    @Inject
    private NewExamServiceImpl newExamService;
    @Inject
    private NewExamRegistrationLoaderImpl newExamRegistrationLoader;

    @Override
    public MapMessage loadAllExamsByStudentId(Long studentId) {
        return newExamService.loadAllExamsByStudentId(studentId);
    }

    @Override
    public List<Map<String, Object>> loadExamsCanBeEnteredByStudentId(Long studentId) {
        return newExamService.loadExamsCanBeEnteredByStudentId(studentId);
    }

    @Override
    public MapMessage handlerStudentExaminationAuthority(Long sid, String newExamId) {
        return newExamService.handlerStudentExaminationAuthority(sid, newExamId);
    }

    @Override
    public MapMessage handlerStudentExaminationAuthorityV2(Long sid, String newExamId, boolean makeUp) {
        return newExamService.handlerStudentExaminationAuthority(sid, newExamId, makeUp);
    }

    @Override
    public MapMessage loadByNewExamIdAndPage(NewExamRegistrationLoaderMapper newExamRegistrationLoaderMapper){
        return newExamRegistrationLoader.loadByNewExamIdAndPage(newExamRegistrationLoaderMapper);
    }

    @Override
    public Integer loadNewExamStudentCount(String newExamId){
        return newExamService.loadNewExamStudentCount(newExamId);
    }
}
