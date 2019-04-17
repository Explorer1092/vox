package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.alibaba.dubbo.config.annotation.Service;
import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.TeacherAssignmentRecordLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.TeacherAssignmentRecord;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Named;

/**
 * @author guoqiang.li
 * @since 2016/3/2
 */
@Named
@Service(interfaceClass = TeacherAssignmentRecordLoader.class)
@ExposeService(interfaceClass = TeacherAssignmentRecordLoader.class)
public class TeacherAssignmentRecordLoaderImpl extends NewHomeworkSpringBean implements TeacherAssignmentRecordLoader {
    @Override
    public TeacherAssignmentRecord loadTeacherAssignmentRecord(Subject subject, Long userId, String bookId) {
        String id = "{}-{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term, subject, userId, bookId);
        TeacherAssignmentRecord record = teacherAssignmentRecordDao.load(id);
        if (record != null) {
            record.initializeIfNecessary();
        }
        return record;
    }
}
