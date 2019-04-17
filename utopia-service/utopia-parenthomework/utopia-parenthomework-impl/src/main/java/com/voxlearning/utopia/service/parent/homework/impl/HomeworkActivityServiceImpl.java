package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkActivityService;
import com.voxlearning.utopia.service.parent.homework.api.entity.Activity;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkActivityDao;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;

import static com.voxlearning.utopia.service.parent.homework.impl.util.Constants.ACTIVITY_ID;

/**
 * 作业活动服务实现
 *
 * @author Wenlong Meng
 * @since Feb 24, 2019
 */
@Named
@ExposeService(interfaceClass = HomeworkActivityService.class)
@Log4j
public class HomeworkActivityServiceImpl implements HomeworkActivityService {

    //local variable
    @Inject private HomeworkActivityDao activityDao;

    //Logic

    /**
     * 创建活动
     *
     * @param activity 活动
     * @return
     */
    @Override
    public int save(Activity activity) {
        if(activity.getId() == null){
            activity.setCreateTime(new Date());
            activity.setId(ACTIVITY_ID);
        }
        activity.setUpdateTime(new Date());
        activityDao.upsert(activity);
        return 1;
    }

    /**
     * 查询活动信息
     *
     * @param id 活动id
     * @return
     */
    @Override
    public Activity load(String id) {
        return activityDao.load(id);
    }

}