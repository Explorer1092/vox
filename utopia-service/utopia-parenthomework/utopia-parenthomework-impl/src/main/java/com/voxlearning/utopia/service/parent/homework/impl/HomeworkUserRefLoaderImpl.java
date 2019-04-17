package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesLoader;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserRefLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserRef;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserPreferencesDao;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserRefDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;
import java.util.Date;

/**
 * 布置作业查询接口实现
 *
 * @author Wenlong Meng
 * @version 20181111
 */
@Named
@ExposeService(interfaceClass = HomeworkUserRefLoader.class)
@Log4j
public class HomeworkUserRefLoaderImpl extends SpringContainerSupport implements HomeworkUserRefLoader {

    //local variables
    @Inject
    protected HomeworkUserRefDao homeworkUserRefDao;

    //Logic

    /**
     * 根据学生id查询最近一次布置作业
     *
     * @param userId 学生id
     * @return 作业学生关系
     */
    @Override
    public HomeworkUserRef last(Long userId) {
        return homeworkUserRefDao.last(userId);
    }

    /**
     * 根据学生id和时间查询布置过的题
     * @param userId 学生id
     * @param time 时间
     * @return 作业学生关系
     */
    @Override
    public Collection<HomeworkUserRef> lastTime(Long userId, Date time) {
        return homeworkUserRefDao.lastTime(userId, time);
    }
}
