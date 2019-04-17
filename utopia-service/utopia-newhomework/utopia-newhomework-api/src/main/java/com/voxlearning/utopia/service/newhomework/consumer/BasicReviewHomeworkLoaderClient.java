package com.voxlearning.utopia.service.newhomework.consumer;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.newhomework.api.BasicReviewHomeworkLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.basicreview.BasicReviewHomeworkPackage;

import java.util.Collection;
import java.util.List;
import java.util.Map;

/**
 * @author guoqiang.li
 * @since 2017/11/8
 */
public class BasicReviewHomeworkLoaderClient implements BasicReviewHomeworkLoader {
    @ImportService(interfaceClass = BasicReviewHomeworkLoader.class)
    private BasicReviewHomeworkLoader remoteReference;

    @Override
    public BasicReviewHomeworkPackage load(String packageId) {
        return remoteReference.load(packageId);
    }

    @Override
    public Map<Long, List<BasicReviewHomeworkPackage>> loadBasicReviewHomeworkPackageByClazzGroupIds(Collection<Long> groupId) {
        return remoteReference.loadBasicReviewHomeworkPackageByClazzGroupIds(groupId);
    }

    @Override
    public MapMessage loadStudentDayPackages(String packageId, Long studentId) {
        return remoteReference.loadStudentDayPackages(packageId, studentId);
    }
}
