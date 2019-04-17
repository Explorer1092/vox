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

package com.voxlearning.utopia.service.business.impl.service.student.internal.app.card;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.raikou.system.api.RaikouSystem;
import com.voxlearning.utopia.core.helper.VersionUtil;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.AbstractStudentAppIndexDataLoader;
import com.voxlearning.utopia.service.business.impl.service.student.internal.app.StudentAppIndexDataContext;
import com.voxlearning.utopia.service.question.api.constant.NewExamType;
import com.voxlearning.utopia.service.region.api.entities.extension.ExRegion;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.School;
import com.voxlearning.utopia.service.user.api.entities.extension.StudentDetail;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

/**
 * Created by tanguohong on 2016/3/16.
 */
@Named
public class LoadStudentAppNewExamCard extends AbstractStudentAppIndexDataLoader {

    @Inject private RaikouSystem raikouSystem;

    //下载学生考试卡片
    @Override
    protected StudentAppIndexDataContext doAppProcess(StudentAppIndexDataContext context) {
        StudentDetail studentDetail = context.getStudent();
        if (studentDetail != null && studentDetail.getClazz() != null && VersionUtil.compareVersion(context.ver, "1.9.0") >= 0) {
            Clazz clazz = studentDetail.getClazz();
            School school = raikouSystem.loadSchool(clazz.getSchoolId());
            if (school != null && school.getRegionCode() != null) {
                ExRegion exRegion = raikouSystem.loadRegion(school.getRegionCode());
                List<Map<String, Object>> enterableExamList = newExamServiceClient.loadExamsCanBeEntered(studentDetail, school, exRegion, 30);
                if (CollectionUtils.isNotEmpty(enterableExamList)) {
                    List<Map<String, Object>> list = new LinkedList<>();
                    enterableExamList.forEach(e -> {
                        Map<String, Object> result = new HashMap<>();
                        if (!NewExamType.independent.name().equals(SafeConverter.toString(e.get("examType")))) {
                            result.put("homeworkId", e.get("id"));
                            result.put("homeworkType", "NEWEXAM_" + SafeConverter.toString(e.get("subject")));
                            result.put("types", Collections.singletonList("NEWEXAM"));
                            result.put("startComment", "开始测试");
                            // 临时处理
                            if ("E_10100292326757".equals(e.get("id")) || "E_10100292331384".equals(e.get("id"))) {
                                result.put("desc", e.get("name"));
                            } else {
                                result.put("desc", Subject.of(SafeConverter.toString(e.get("subject"))).getValue() + "测试");
                            }
                            // 单元测评卡片特殊处理
                            //if (NewExamType.independent.name().equals(SafeConverter.toString(e.get("examType")))) {
                            //    result.put("desc", "单元测评");
                            //}
                            result.put("endDate", e.get("examStopAt"));
                            list.add(result);
                        }
                    });
                    context.__enterableNewExamCards.addAll(list);
                }
            }
        }
        return context;
    }
}
