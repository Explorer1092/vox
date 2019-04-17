package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.PictureBookPlusRecommendRecordLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.PictureBookPlusRecommendRecord;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Named;

@Named
public class PictureBookPlusRecommendRecordLoaderImpl extends NewHomeworkSpringBean implements PictureBookPlusRecommendRecordLoader {
    @Override
    public PictureBookPlusRecommendRecord loadPictureBookRecommendRecord(Subject subject, Long teacherId) {
        return pictureBookPlusRecommendRecordDao.loadPictureBookRecommendRecord(subject, teacherId);
    }

    @Override
    public void updatePictureBookRecommendRecord(PictureBookPlusRecommendRecord pictureBookRecommendRecord) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term, pictureBookRecommendRecord.getSubject(), pictureBookRecommendRecord.getTeacherId());
        if (StringUtils.isEmpty(pictureBookRecommendRecord.getId())) {
            pictureBookRecommendRecord.setId(id);
        }
        pictureBookPlusRecommendRecordDao.upsert(pictureBookRecommendRecord);
    }
}
