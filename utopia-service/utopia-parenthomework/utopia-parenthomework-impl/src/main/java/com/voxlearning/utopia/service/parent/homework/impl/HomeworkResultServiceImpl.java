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

package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkProcessResultDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkResultDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;

/**
 * 作业结果更新服务实现
 *
 * @author Wenlong Meng
 * @version 20181107
 * @date 2018-11-07
 */
@Named
@ExposeService(interfaceClass = HomeworkResultService.class)
public class HomeworkResultServiceImpl extends SpringContainerSupport implements HomeworkResultService {

    @Inject
    private HomeworkResultDao homeworkResultDao;
    @Inject
    private HomeworkProcessResultDao homeworkProcessResultDao;

    /**
     * 保存作业结果
     *
     * @param homeworkResult 作业结果
     * @return
     */
    @Override
    public MapMessage saveHomeworkResult(HomeworkResult homeworkResult) {
        //保存
        homeworkResultDao.upsert(homeworkResult);
        return MapMessage.successMessage();
    }

    /**
     * 保存作业结果详情
     *
     * @param homeworkProcessResults 作业结果详情
     * @return
     */
    @Override
    public MapMessage saveHomeworkProcessResult(List<HomeworkProcessResult> homeworkProcessResults) {
        //生成ID
        homeworkProcessResults.stream().forEach(e->e.setId(HomeworkUtil.generatorID(e.getHomeworkResultId(), e.getQuestionId())));
        try{
            //保存
            homeworkProcessResultDao.inserts(homeworkProcessResults);
        }catch (Exception e){
            LoggerUtils.info("saveHomeworkProcessResult.error", e.getMessage());
            if(!e.getMessage().contains("duplicate key")){//忽略重复提交
                throw e;
            }
        }

        return MapMessage.successMessage();
    }

}
