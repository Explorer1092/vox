package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.MQUtils;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressService;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserProgress;
import com.voxlearning.utopia.service.parent.homework.api.entity.UserProgress;
import com.voxlearning.utopia.service.parent.homework.api.model.BizType;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserProgressDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * 用户进度服务实现
 *
 * @author Wenlong Meng
 * @since Feb 19, 2019
 */
@Named
@ExposeService(interfaceClass = HomeworkUserProgressService.class)
@Slf4j
public class HomeworkUserProgressServiceImpl implements HomeworkUserProgressService {

    @Inject
    private HomeworkUserProgressDao homeworkUserProgressDao;
    @Inject
    private HomeworkUserProgressLoader homeworkUserProgressLoader;

    /**
     * 保存用户进度
     *
     * @param userId
     * @param bizType
     * @param userProgress
     * @return
     */
    @Override
    public MapMessage save(Long userId, String bizType, UserProgress userProgress) {
        if (ObjectUtils.anyBlank(userId, bizType, userProgress)) {
            return MapMessage.errorMessage("参数不能为空");
        }
        HomeworkUserProgress homeworkUserProgress = homeworkUserProgressLoader.loadUserProgress(userId, bizType);
        if (homeworkUserProgress == null) {
            homeworkUserProgress = new HomeworkUserProgress();
            homeworkUserProgress.setId(HomeworkUtil.generatorID(userId, bizType));
            homeworkUserProgress.setBizType(bizType);
            homeworkUserProgress.setUserId(userId);
        }
        List<UserProgress> userProgresses = homeworkUserProgress.getUserProgresses();
        if (CollectionUtils.isEmpty(userProgresses)) {
            userProgresses = new ArrayList<>();
        }
        if(userProgresses.contains(userProgress) && userProgress.getSectionId().equals("*")){//TODO
            for(UserProgress up: userProgresses){
                if(up.equals(userProgress)){
                    up.setExtInfo(userProgress.getExtInfo());
                    break;
                }
            }
        }
        if(!userProgresses.contains(userProgress)){
            userProgress.setCreateTime(new Date());
            userProgresses.add(0, userProgress);
            homeworkUserProgress.setUserProgresses(userProgresses);
            homeworkUserProgressDao.upsert(homeworkUserProgress);
        }

        //发送课程完成MQ
        MQUtils.send("platform.queue.parent.homework.course.topic",
                MapUtils.m("messageType", "finished",
                "userId", userId,
                "op","learn",
                "bizType", BizType.INTELLIAGENT_TEACHING.name()));

        return MapMessage.successMessage();
    }
}
