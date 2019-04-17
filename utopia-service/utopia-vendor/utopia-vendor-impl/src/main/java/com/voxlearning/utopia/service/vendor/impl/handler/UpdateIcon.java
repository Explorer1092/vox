package com.voxlearning.utopia.service.vendor.impl.handler;

import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.api.constant.MySelfStudyActionType;
import com.voxlearning.utopia.service.vendor.impl.dao.StudyAppDataDao;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-05-23 下午3:47
 **/
@Named("MySelfStudyEventHandler.updateIcon")
public class UpdateIcon implements MySelfStudyEventHandler {


    @Inject
    private StudyAppDataDao studyAppDataDao;

    @Override
    public MySelfStudyActionType getMySelfStudyActionType() {
        return MySelfStudyActionType.UPDATE_ICON;
    }

    @Override
    public void handle(MySelfStudyActionEvent event) {
        Long userId = event.getUserId();
        SelfStudyType selfStudyType = event.getSelfStudyType();
        if (selfStudyType == null || selfStudyType == SelfStudyType.UNKNOWN)
            return;
        Map<String, Object> attributes = event.getAttributes();
        if (attributes != null){
            String iconUrl = SafeConverter.toString(attributes.get("iconUrl"));
            studyAppDataDao.updateIcon(userId, selfStudyType, iconUrl);
        }
    }
}
