package com.voxlearning.utopia.service.parent.homework.impl;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.core.utils.ObjectUtils;
import com.voxlearning.utopia.service.parent.homework.api.HomeworkUserProgressLoader;
import com.voxlearning.utopia.service.parent.homework.api.entity.HomeworkUserProgress;
import com.voxlearning.utopia.service.parent.homework.impl.dao.HomeworkUserProgressDao;
import com.voxlearning.utopia.service.parent.homework.impl.util.HomeworkUtil;
import com.voxlearning.utopia.service.parent.homework.provider.intelligentTeaching.impl.IntelligentTeachingServiceImpl;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 业务进度loader实现
 *
 * @author chongfeng.qi
 * @since Feb 14, 2019
 */
@Named
@ExposeService(interfaceClass = HomeworkUserProgressLoader.class)
@Slf4j
public class HomeworkUserProgressLoaderImpl implements HomeworkUserProgressLoader {
    @Inject
    private HomeworkUserProgressDao homeworkUserProgressDao;
    @Inject
    private IntelligentTeachingServiceImpl intelligentTeachingService;

    /**
     * 根据业务类型查询用户进度
     *
     * @param userId 用户id
     * @param bizType 业务标识
     * @return
     */
    @Override
    public HomeworkUserProgress loadUserProgress(Long userId, String bizType) {
        if (userId == null) {
            return null;
        }
        if (StringUtils.isBlank(bizType)) {
            return null;
        }
        return homeworkUserProgressDao.load(HomeworkUtil.generatorID(userId, bizType));
    }

    /**
     * 根据业务类型查询课程、进度
     *
     * @param userId    用户id
     * @param sectionId 课时id
     * @param bizType   业务类型
     * @return
     */
    @Override
    public MapMessage loadCourseProgresses(Long userId, String bookId, String unitId, String sectionId, String bizType) {
        //学习进度
        HomeworkUserProgress homeworkUserProgress = this.loadUserProgress(userId, bizType);
        Set<String> courseSet = ObjectUtils.get(() ->
                homeworkUserProgress.getUserProgresses().stream()
                        .filter(u->StringUtils.equals(u.getSectionId(), sectionId)).map(u -> u.getCourse()).collect(Collectors.toSet()), Collections.EMPTY_SET);
        List<Map<String, Object>> courses = intelligentTeachingService.loadCoursesBySectionId(sectionId).stream().map(c ->
                MapUtils.m(
                        "isLearned", courseSet.contains(c.getId()),
                        "id", c.getId(),
                        "name", c.getName(),
                        "description", c.getDescription(),
                        "backgroundImage", c.getBackgroundImage())).collect(Collectors.toList());
        return MapMessage.successMessage().add("data", MapUtils.map("courses", courses));
    }
}
