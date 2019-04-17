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

package com.voxlearning.utopia.admin.service.crm;

import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.admin.dao.CrmSchoolEvaluateDao;
import com.voxlearning.utopia.admin.entity.CrmSchoolEvaluate;
import com.voxlearning.utopia.admin.service.AbstractAdminService;
import com.voxlearning.utopia.service.school.client.SchoolLoaderClient;
import com.voxlearning.utopia.service.user.api.entities.School;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 学校评分
 * Created by yaguang,wang on 2017/1/6.
 */
@Named
public class CrmSchoolEvaluateService extends AbstractAdminService {

    @Inject private SchoolLoaderClient schoolLoaderClient;

    @Inject CrmSchoolEvaluateDao crmSchoolEvaluateDao;

    public List<CrmSchoolEvaluate> loadCrmSchoolEvaluateBySchoolId(Long schoolId) {
        return crmSchoolEvaluateDao.findBySchoolId(schoolId);
    }

    public MapMessage addSchoolEvaluate(Long schoolId, Integer placeScore, Integer teachScore, Integer studentScore, Integer commercializeScore
            , String remark, String account, String accountName) {
        School school = schoolLoaderClient.getSchoolLoader()
                .loadSchool(schoolId)
                .getUninterruptibly();
        if (school == null) {
            return MapMessage.errorMessage("该学校未找到");
        }
        if (errorScore(placeScore)) {
            return MapMessage.errorMessage("地理位置评分填写错误");
        }
        if (errorScore(teachScore)) {
            return MapMessage.errorMessage("教学质量评分填写错误");
        }
        if (errorScore(studentScore)) {
            return MapMessage.errorMessage("生源水平评分填写错误");
        }
        if (errorScore(commercializeScore)) {
            return MapMessage.errorMessage("商业化潜力评分填写错误");
        }
        CrmSchoolEvaluate evaluate = new CrmSchoolEvaluate();
        evaluate.setSchoolId(schoolId);
        evaluate.setPlaceScore(placeScore);
        evaluate.setTeachScore(teachScore);
        evaluate.setStudentScore(studentScore);
        evaluate.setCommercializeScore(commercializeScore);
        evaluate.setRemark(remark);
        evaluate.setAccount(account);
        evaluate.setAccountName(accountName);
        crmSchoolEvaluateDao.insert(evaluate);
        return MapMessage.successMessage();
    }

    private Boolean errorScore(Integer score) {
        return score < 0 || score > 5;
    }
}
