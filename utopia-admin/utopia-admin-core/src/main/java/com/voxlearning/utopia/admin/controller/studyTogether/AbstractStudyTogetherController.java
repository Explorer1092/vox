package com.voxlearning.utopia.admin.controller.studyTogether;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.galaxy.service.studycourse.api.consumer.StudyCourseStructLoaderClient;
import com.voxlearning.utopia.admin.controller.AbstractAdminController;
import com.voxlearning.utopia.admin.controller.AbstractAdminSystemController;

import javax.inject.Inject;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author jiangpeng
 * @since 2019-01-02 3:00 PM
 **/
public class AbstractStudyTogetherController extends AbstractAdminSystemController {

    @Inject
    protected StudyCourseStructLoaderClient studyCourseStructLoaderClient;

    protected List<String> getAllLessonId(){
        return studyCourseStructLoaderClient.loadAllCourseStructSku().stream()
                .map(t -> SafeConverter.toString(t.getId())).sorted(Comparator.comparing(SafeConverter::toString)).collect(Collectors.toList());
    }
}
