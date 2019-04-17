package com.voxlearning.utopia.service.vendor.impl.handler;

import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.utopia.api.constant.SelfStudyType;
import com.voxlearning.utopia.api.constant.MySelfStudyActionEvent;
import com.voxlearning.utopia.api.constant.MySelfStudyActionType;
import com.voxlearning.utopia.service.vendor.impl.service.MySelfStudyGlobalMsgServiceImpl;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;

/**
 * @author jiangpeng
 * @since 2017-06-21 下午3:58
 **/
@Named("MySelfStudyEventHandler.updateGlobalMsg")
public class UpdateGlobalMsg implements MySelfStudyEventHandler{


    @Inject
    private MySelfStudyGlobalMsgServiceImpl mySelfStudyGlobalMsgService;

    @Override
    public MySelfStudyActionType getMySelfStudyActionType() {
        return MySelfStudyActionType.UPDATE_GLOBAL_MSG;
    }

    @Override
    public void handle(MySelfStudyActionEvent event) {
        SelfStudyType selfStudyType = event.getSelfStudyType();
        if (selfStudyType == null || selfStudyType ==SelfStudyType.UNKNOWN)
            return;
        Map<String, Object> attributes = event.getAttributes();
        if (MapUtils.isEmpty(attributes))
            return;
        String msg = SafeConverter.toString(attributes.get("msg"));
        mySelfStudyGlobalMsgService.$upsetMySelfStudyGlobalMsg(selfStudyType, msg);
    }
}
