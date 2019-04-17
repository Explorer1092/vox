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

package com.voxlearning.utopia.service.newhomework.impl.template.processresult;

import com.voxlearning.alps.annotation.meta.SchoolLevel;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.repackaged.org.apache.commons.beanutils.BeanUtils;
import com.voxlearning.utopia.service.newhomework.api.entity.sub.SubHomeworkProcessResult;
import com.voxlearning.utopia.service.newhomework.api.mapper.HomeworkProcessMapper;
import com.voxlearning.utopia.service.newhomework.impl.loader.NewHomeworkProcessResultLoaderImpl;
import com.voxlearning.utopia.service.newhomework.impl.template.ProcessResultTemplate;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author xuesong.zhang
 * @since 2016/8/4
 */
@Named
public class JuniorProcessResult extends ProcessResultTemplate {

    @Inject private NewHomeworkProcessResultLoaderImpl newHomeworkProcessResultLoader;

    @Override
    public SchoolLevel getSchoolLevel() {
        return SchoolLevel.JUNIOR;
    }

    @Override
    public List<HomeworkProcessMapper> getProcessResult(Collection<String> processIds) {
        List<HomeworkProcessMapper> resultList = new ArrayList<>();
        if (CollectionUtils.isNotEmpty(processIds)) {
            Map<String, SubHomeworkProcessResult> subProcessResultMap = newHomeworkProcessResultLoader.loadSubHomeworkProcessResults(processIds);
            if (MapUtils.isNotEmpty(subProcessResultMap)) {
                subProcessResultMap.values().forEach(o -> {
                    HomeworkProcessMapper mapper = new HomeworkProcessMapper();
                    try {
                        BeanUtils.copyProperties(mapper, o);
                        resultList.add(mapper);
                        mapper.setProcessId(o.getId());
                    } catch (Exception ignored) {
                    }
                });
            }
        }

        return resultList;
    }
}
