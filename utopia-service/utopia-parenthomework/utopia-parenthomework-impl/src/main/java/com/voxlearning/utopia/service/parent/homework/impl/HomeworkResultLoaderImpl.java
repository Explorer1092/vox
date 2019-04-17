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

import com.google.common.collect.Sets;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.core.utils.LoggerUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkResultLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkProcessResult;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkResult;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkProcessResultDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkResultDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserRefDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 作业结果查询实现，提供作业结果及详情查询功能
 *
 * @author Wenlong Meng
 * @version 20181107
 * @date 2018-11-07
 */
@Named
@ExposeService(interfaceClass = HomeworkResultLoader.class)
public class HomeworkResultLoaderImpl extends SpringContainerSupport implements HomeworkResultLoader {

    //local variables
    /**
     * 作业结果详情dao
     */
    @Inject
    private HomeworkProcessResultDao homeworkProcessResultDao;
    /**
     * 作业结果dao
     */
    @Inject
    private HomeworkResultDao homeworkResultDao;
    /**
     * 用户与作业关联dao
     */
    @Inject
    private HomeworkUserRefDao homeworkUserRefDao;
    private static Set<String> bizTypes = Sets.newHashSet("EXAM", "MENTAL_ARITHMETIC");

    //Logic


    /**
     * 根据id查询作业结果
     *
     * @param id 作业结果id
     * @return 作业结果
     */
    @Override
    public HomeworkResult loadHomeworkResult(String id) {
        return this.homeworkResultDao.load(id);
    }

    /**
     * 根据作业id、用户id查询作业结果
     *
     * @param homeworkId 作业id
     * @param userId     用户id
     * @return 作业结果
     */
    @Override
    public HomeworkResult loadHomeworkResult(String homeworkId, Long userId) {
        return this.homeworkResultDao.load(HomeworkUtil.generatorID(homeworkId, userId));
    }

    /**
     * 根据作业id、用户id查询所有作业结果，包括订正、重做
     *
     * @param homeworkId 作业id
     * @param userId     用户id
     * @return 作业结果
     */
    @Override
    public List<HomeworkResult> loadHomeworkResults(String homeworkId, Long userId) {
        return this.homeworkResultDao.loadHomeworkResults(homeworkId, userId);
    }

    /**
     * 根据用户id查询作业结果
     *
     * @param userId 用户id
     * @return 作业结果
     */
    @Override
    public List<HomeworkResult> loadHomeworkResultByUserId(Long userId) {
        //查询学生与作业关联，并构建作业结果id集合
        List<String> homeworkResultIds = homeworkUserRefDao.loadHomeworkUserRef(userId).stream().map(e->HomeworkUtil.generatorID(e.getHomeworkId(), e.getUserId())).collect(Collectors.toList());
        //根据作业结果id查询作业结果
        return homeworkResultDao.loads(homeworkResultIds).values().stream().collect(Collectors.toList());
    }

    /**
     * 根据用户id、时间的作业结果:下分页
     *
     * @param userId 用户id
     * @param start 开始条数
     * @param size 获取条数
     * @param startTime 获取条数
     * @return 作业结果
     */
    @Override
    public List<HomeworkResult> loadHomeworkResultDown(Long userId, Integer start, Integer size, Date startTime) {
        LoggerUtils.info("loadHomeworkResultDown", userId, start, size, startTime);
        //查询学生与作业关联，并构建作业结果id集合
        List<String> homeworkResultIds = homeworkUserRefDao.loadHomeworkIdsDown(userId, 0, 300, startTime).stream().map(e->HomeworkUtil.generatorID(e, userId)).collect(Collectors.toList());
        if(CollectionUtils.isEmpty(homeworkResultIds) || homeworkResultIds.size() <= start){
            return Collections.emptyList();
        }

        List<HomeworkResult> result = new ArrayList<>();
        for(String homeworkId : homeworkResultIds){
            HomeworkResult homeworkResult = homeworkResultDao.load(homeworkId);
            if(check(homeworkResult, startTime)){
                result.add(homeworkResult);
            }
            if(result.size() >= start + size){
                break;
            }
        }

        //根据作业结果id查询作业结果
        return result.size()>start ? new ArrayList<>(result.subList(start, Math.min(start+size, result.size()))) : Collections.EMPTY_LIST;
    }

    /**
     * 过滤
     *
     * @param homeworkResult
     * @param startTime
     * @return
     */
    private boolean check(HomeworkResult homeworkResult, Date startTime){
        return homeworkResult != null && contains(homeworkResult) && homeworkResult.getFinished() && homeworkResult.getEndTime().before(startTime);
    }
    private boolean contains(HomeworkResult h){
        return StringUtils.isEmpty(h.getBizType()) || bizTypes.contains(h.getBizType());
    }

    /**
     * 根据作业结果id查询结果详情
     *
     * @param homeworkResultId 作业结果id
     * @return 作业结果详情
     */
    @Override
    public List<HomeworkProcessResult> loadHomeworkProcessResults(String homeworkResultId) {
        return this.homeworkProcessResultDao.loadHomeworkProcessResult(homeworkResultId);
    }

}
