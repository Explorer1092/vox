package com.voxlearning.utopia.cjlschool.support;

import com.voxlearning.utopia.cjlschool.processor.CJLClassProcessor;
import com.voxlearning.utopia.cjlschool.processor.CJLStudentProcessor;
import com.voxlearning.utopia.cjlschool.processor.CJLTeacherCourseProcessor;
import com.voxlearning.utopia.cjlschool.processor.CJLTeacherProcessor;
import com.voxlearning.utopia.service.mizar.api.constants.cjlschool.CJLEntityType;

import javax.inject.Inject;
import javax.inject.Named;

/**
 * Created by Yuechen.Wang on 2017/7/20.
 */
@Named("dataProcessorFactory")
public class CJLDataProcessorFactory {

    @Inject private CJLClassProcessor classProcessor;
    @Inject private CJLTeacherProcessor teacherProcessor;
    @Inject private CJLTeacherCourseProcessor teacherCourseProcessor;
    @Inject private CJLStudentProcessor studentProcessor;


    public CJLDataProcessor getProcessor(CJLEntityType type) {
        if (type == null) {
            return null;
        }
        switch (type) {
            case CLASS:
                return classProcessor;
            case TEACHER:
                return teacherProcessor;
            case STUDENT:
                return studentProcessor;
            case TEACHER_COURSE:
                return teacherCourseProcessor;
            default:
                return null;
        }
    }

}
