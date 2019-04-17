package com.voxlearning.utopia.service.parent.homework.impl.template.assign;

import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.utopia.service.parent.homework.api.entity.Homework;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserRef;
import com.voxlearning.utopia.service.parent.homework.api.mapper.HomeworkParam;
import com.voxlearning.utopia.service.parent.homework.impl.cache.CacheKey;
import com.voxlearning.utopia.service.parent.homework.impl.cache.HomeWorkCache;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserRefDao;
import com.voxlearning.utopia.service.parent.homework.impl.model.HomeworkContext;
import com.voxlearning.utopia.service.parent.homework.impl.template.HomeworkProcessor;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * 保存作业和用户的关系
 * @author chongfeng.qi
 * @date 20181120
 */
@Named
public class HomeworkStoreUserRefProcessor implements HomeworkProcessor {

    @Inject
    private HomeworkUserRefDao homeworkUserRefDao;

    @Override
    public void process(HomeworkContext hc) {
        Homework homework = hc.getHomework();
        HomeworkParam param = hc.getHomeworkParam();
        Long studentId = param.getStudentId();
        HomeworkUserRef homeworkUserRef = new HomeworkUserRef();
        homeworkUserRef.setHomeworkId(homework.getId());
        homeworkUserRef.setUserId(studentId);
        homeworkUserRef.setId(HomeworkUtil.generatorID(homework.getId(), studentId));
        homeworkUserRefDao.insert(homeworkUserRef);
        // 同一天同教材同单元同难度只能布置一次作业
        HomeWorkCache.set(DateUtils.getCurrentToDayEndSecond(), true, CacheKey.TODAYASSIGN, param.getBizType(), studentId, hc.getUnitId(), hc.getQuestionPackages().get(0).getName());
    }
}
