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

package com.voxlearning.utopia.service.business.impl.service.student.internal;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 生成模考卡片
 * 包含 报名卡片与考试卡片
 *
 * @author guoqiang.li
 * @since 2016/3/8
 */
@Named
public class LoadStudentNewExamCard extends AbstractStudentIndexDataLoader {

    @Inject private RaikouSystem raikouSystem;

    @Override
    protected StudentIndexDataContext doProcess(StudentIndexDataContext context) {
        StudentDetail studentDetail = context.getStudent();
        if (studentDetail != null && studentDetail.getClazz() != null) {
            Clazz clazz = studentDetail.getClazz();
            School school = raikouSystem.loadSchool(clazz.getSchoolId());
            if (school != null && school.getRegionCode() != null) {
                ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                List<Map<String, Object>> enterableExamList = newExamServiceClient.loadExamsCanBeEntered(studentDetail, school, exRegion, 30);
                if (CollectionUtils.isNotEmpty(enterableExamList)) {
                    List<Map<String, Object>> list = enterableExamList.stream()
                            .filter(n -> !NewExamType.independent.name().equals(SafeConverter.toString(n.get("examType"))))
                            .collect(Collectors.toList());
                    context.__enterableNewExamCards.addAll(list);
                    context.__newExamExist = true;
                }
            }
        }
        return context;
    }
}
