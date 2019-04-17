package com.voxlearning.utopia.service.vendor.impl.handler;

import com.voxlearning.alps.core.util.MapUtils;
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
 * @since 2017-09-04 下午6:58
 **/
@Named("MySelfStudyEventHandler.updateNotify")
public class UpdateUserNotify implements MySelfStudyEventHandler {
    @Inject
    private StudyAppDataDao studyAppDataDao;

    @Override
    public MySelfStudyActionType getMySelfStudyActionType() {
        return MySelfStudyActionType.UPDATE_USER_NOTIFY;
    }

    @Override
    public void handle(MySelfStudyActionEvent event) {
        Long userId = event.getUserId();
        SelfStudyType selfStudyType = event.getSelfStudyType();
        Map<String, Object> attributes = event.getAttributes();
        if (MapUtils.isEmpty(attributes))
            return;
        String notifyContent = SafeConverter.toString(attributes.get("notifyContent"));
        String notifyUniqueId = SafeConverter.toString(attributes.get("notifyUniqueId"));
        studyAppDataDao.updateNotify(userId, selfStudyType, notifyContent, notifyUniqueId);
    }
}
