package com.voxlearning.utopia.service.newhomework.impl.loader;

import com.voxlearning.alps.annotation.meta.Subject;
import com.voxlearning.alps.annotation.meta.Term;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.data.SchoolYear;
import com.voxlearning.utopia.service.newhomework.api.OralCommunicationRecommendRecordLoader;
import com.voxlearning.utopia.service.newhomework.api.entity.OralCommunicationRecommendRecord;
import com.voxlearning.utopia.service.newhomework.impl.support.NewHomeworkSpringBean;

import javax.inject.Named;

/**
 * \* Created: liuhuichao
 * \* Date: 2019/1/3
 * \* Time: 8:13 PM
 * \* Description:口语交际推介
 * \
 */
@Named
public class OralCommunicationRecommendRecordLoaderImpl extends NewHomeworkSpringBean implements OralCommunicationRecommendRecordLoader {

    @Override
    public OralCommunicationRecommendRecord loadOralCommunicationRecommendRecord(Subject subject, Long teacherId) {
        return oralCommunicationRecommendRecordDao.loadOralCommunicationRecommendRecord(subject,teacherId);
    }

    @Override
    public void updateOralCommunicationRecommendRecord(OralCommunicationRecommendRecord oralCommunicationRecommendRecord) {
        String id = "{}-{}-{}-{}";
        SchoolYear schoolYear = SchoolYear.newInstance();
        Integer year = schoolYear.year();
        Term term = schoolYear.currentTerm();
        id = StringUtils.formatMessage(id, year, term, oralCommunicationRecommendRecord.getSubject(), oralCommunicationRecommendRecord.getTeacherId());
        if (StringUtils.isEmpty(oralCommunicationRecommendRecord.getId())) {
            oralCommunicationRecommendRecord.setId(id);
        }
        oralCommunicationRecommendRecordDao.upsert(oralCommunicationRecommendRecord);
    }
}
