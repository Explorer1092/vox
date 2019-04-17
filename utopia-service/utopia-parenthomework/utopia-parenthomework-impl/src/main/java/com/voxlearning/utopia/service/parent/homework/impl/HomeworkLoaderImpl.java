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
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkPractice;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserRef;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkPracticeDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserRefDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * 作业loader实现
 *
 * @author Wenlong Meng
 * @version 20181109
 * @deta 2018-11-09
 */
@Named
@ExposeService(interfaceClass = HomeworkLoader.class)
public class HomeworkLoaderImpl extends SpringContainerSupport implements HomeworkLoader {

    @Inject
    private HomeworkDao homeworkDao;
    @Inject
    private HomeworkPracticeDao homeworkPracticeDao;
    @Inject
    private HomeworkUserRefDao homeworkUserRefDao;

    /**
     * 根据id批量查询作业信息
     *
     * @param ids 作业id集合
     * @return 作业id - 作业信息
     */
    @Override
    public Map<String, Homework> loadHomeworks(Collection<String> ids) {
        return homeworkDao.loads(ids);
    }

    /**
     * 根据学生id查询作业信息
     *
     * @param userId
     * @return
     */
    @Override
    public List<Homework> loadHomeworkByUserId(Long userId) {
        //后去学生对应作业id
        List<HomeworkUserRef> homeworkUserRefs = homeworkUserRefDao.loadHomeworkUserRef(userId);
        List<String> homeworkIds = homeworkUserRefs.stream().map(e->e.getHomeworkId()).collect(Collectors.toList());
        //根据作业id查询作业信息
        return this.loadHomeworks(homeworkIds).values().stream().collect(Collectors.toList());
    }

    /**
     * 根据id批量查询作业详情
     *
     * @param ids 作业id集合
     * @return 作业id - 作业详情
     */
    @Override
    public Map<String, HomeworkPractice> loadHomeworkPractices(Collection<String> ids) {
        return homeworkPracticeDao.loads(ids);
    }

}
