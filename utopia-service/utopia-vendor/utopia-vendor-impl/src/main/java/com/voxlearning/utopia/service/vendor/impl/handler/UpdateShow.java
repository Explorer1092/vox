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
 * @since 2017-06-01 下午2:03
 **/
@Named("MySelfStudyEventHandler.updateShow")
public class UpdateShow implements MySelfStudyEventHandler{

    @Inject
    private StudyAppDataDao studyAppDataDao;

    @Override
    public MySelfStudyActionType getMySelfStudyActionType() {
        return MySelfStudyActionType.UPDATE_SHOW;
    }

    @Override
    public void handle(MySelfStudyActionEvent event) {
        Long userId = event.getUserId();
        SelfStudyType selfStudyType = event.getSelfStudyType();
        Map<String, Object> attributes = event.getAttributes();
        if (MapUtils.isEmpty(attributes))
            return;
        boolean show = SafeConverter.toBoolean(attributes.get("show"));
        studyAppDataDao.updateShow(userId, selfStudyType, show);
    }
}
