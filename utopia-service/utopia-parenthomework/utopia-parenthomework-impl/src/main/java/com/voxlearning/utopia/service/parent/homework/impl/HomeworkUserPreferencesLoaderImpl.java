package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserPreferencesLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserPreferences;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserPreferencesDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import lombok.extern.log4j.Log4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collection;

/**
 * 布置作业查询接口实现
 *
 * @author Wenlong Meng
 * @version 20181111
 */
@Named
@ExposeService(interfaceClass = HomeworkUserPreferencesLoader.class)
@Log4j
public class HomeworkUserPreferencesLoaderImpl extends SpringContainerSupport implements HomeworkUserPreferencesLoader {

    //local variables
    @Inject
    protected HomeworkUserPreferencesDao homeworkUserPreferencesDao;

    //Logic
    /**
     * 根据用户id查询设置偏好
     *
     * @param userId 学生id
     * @return 设置偏好
     */
    @Override
    public Collection<HomeworkUserPreferences> loadHomeworkUserPreferences(Long userId) {
        if (userId == null) {
            return CollectionUtils.emptyCollection();
        }
        return homeworkUserPreferencesDao.loadByStudentId(userId);
    }

    /**
     * 根据用户id、学科查询设置偏好
     *
     * @param userId 学生id
     * @param subject 学科
     * @return 设置偏好
     */
    @Override
    public HomeworkUserPreferences loadHomeworkUserPreference(Long userId, String subject) {
        return homeworkUserPreferencesDao.load(HomeworkUtil.generatorID(userId, subject));
    }

}
