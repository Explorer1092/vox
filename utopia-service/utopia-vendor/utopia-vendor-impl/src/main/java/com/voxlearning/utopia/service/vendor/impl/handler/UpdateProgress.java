package com.voxlearning.utopia.service.vendor.impl.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.service.vendor.api.entity.MySelfStudyData;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.api.constant.MySelfStudyActionType;
import com.voxlearning.utopia.service.vendor.impl.dao.MySelfStudyDataDao;
import com.voxlearning.utopia.service.vendor.impl.dao.StudyAppDataDao;
import com.voxlearning.utopia.service.vendor.impl.support.MySelfStudyDayRecorder;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Date;
import java.util.Map;

/**
 * 完成自学
 *
 * @author jiangpeng
 * @since 2016-10-20 下午1:40
 **/
@Named("MySelfStudyEventHandler.updateProgress")
public class UpdateProgress implements MySelfStudyEventHandler {
    @Override
    public MySelfStudyActionType getMySelfStudyActionType() {
        return MySelfStudyActionType.UPDATE_PROGRESS;
    }

    @Inject
    private MySelfStudyDataDao mySelfStudyDataDao;

    @Inject
    private MySelfStudyDayRecorder mySelfStudyDayRecorder;

    @Inject
    private StudyAppDataDao studyAppDataDao;



    @Override
    public void handle(MySelfStudyActionEvent event) {

        Long userId = event.getUserId();
        SelfStudyType selfStudyType = event.getSelfStudyType();
        MySelfStudyData mySelfStudyData = MySelfStudyData.newInstance(selfStudyType, userId);

        Map<String, Object> attributes = event.getAttributes();
        if (attributes != null){
            Boolean incDayCount = false;
            String studyProgress = SafeConverter.toString(attributes.get("studyProgress"), null);
            if (StringUtils.isNotBlank(studyProgress))
                mySelfStudyData.setStudyProgress(studyProgress);
            Long lastUseDateTimeMils = SafeConverter.toLong(attributes.get("lastUserDate"), 0L);
            if (lastUseDateTimeMils != 0){
                Date lastUseDate = new Date(lastUseDateTimeMils);
                mySelfStudyData.setLastUseDate(lastUseDate);
                incDayCount = !mySelfStudyDayRecorder.hasRecord(userId, selfStudyType, lastUseDateTimeMils);
            }

            Boolean result = mySelfStudyDataDao.insertIfAbsentOrElseUpdate(mySelfStudyData, incDayCount);
            if (result)
                mySelfStudyDayRecorder.record(userId, selfStudyType, lastUseDateTimeMils);


            //1.9.0 首页用新的数据源
            studyAppDataDao.updateProgress(userId, selfStudyType, studyProgress);
        }
    }
}
