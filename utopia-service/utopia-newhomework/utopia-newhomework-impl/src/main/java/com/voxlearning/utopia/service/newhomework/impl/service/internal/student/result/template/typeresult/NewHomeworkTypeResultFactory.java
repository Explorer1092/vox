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

package com.voxlearning.utopia.service.newhomework.impl.service.internal.student.result.template.typeresult;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.question.api.constant.NewHomeworkTypeResultProcessTemp;

import javax.inject.Named;
import java.util.HashMap;
import java.util.Map;

/**
 * @author zhangbin
 * @since 2017/7/2 19:00
 */

@Named
public class NewHomeworkTypeResultFactory extends SpringContainerSupport {

    private Map<NewHomeworkTypeResultProcessTemp, NewHomeworkTypeResultProcessTemplate> templateMap;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        this.templateMap = new HashMap<>();
        Map<String, NewHomeworkTypeResultProcessTemplate> beans = applicationContext.getBeansOfType(NewHomeworkTypeResultProcessTemplate.class);
        for (NewHomeworkTypeResultProcessTemplate bean : beans.values()) {
            this.templateMap.put(bean.getNewHomeworkTypeResultTemp(), bean);
        }
    }

    public NewHomeworkTypeResultProcessTemplate getTemplate(NewHomeworkTypeResultProcessTemp newHomeworkTypeResultProcessTemp) {
        if (newHomeworkTypeResultProcessTemp == null) {
            return null;
        }
        return this.templateMap.get(newHomeworkTypeResultProcessTemp);
    }
}

