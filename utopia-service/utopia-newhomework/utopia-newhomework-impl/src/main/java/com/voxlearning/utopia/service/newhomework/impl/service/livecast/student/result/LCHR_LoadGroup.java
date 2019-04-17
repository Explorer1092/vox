package com.voxlearning.utopia.service.newhomework.impl.service.livecast.student.result;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.api.constant.ErrorCodeConstants;
import com.voxlearning.utopia.service.newhomework.api.context.livecast.LiveCastHomeworkResultContext;
import com.voxlearning.utopia.service.user.api.entities.third.ThirdPartyGroup;
import com.voxlearning.utopia.service.user.consumer.ThirdPartyGroupLoaderClient;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Collections;

/**
 * @author xuesong.zhang
 * @since 2017/1/3
 */
@Named
public class LCHR_LoadGroup extends SpringContainerSupport implements LiveCastHomeworkResultTask {
    @Inject private ThirdPartyGroupLoaderClient thirdPartyGroupLoaderClient;

    @Override
    public void execute(LiveCastHomeworkResultContext context) {

        ThirdPartyGroup group = thirdPartyGroupLoaderClient.loadThirdPartyGroupsIncludeDisabled(Collections.singletonList(context.getClazzGroupId())).getOrDefault(context.getClazzGroupId(), null);

        if (group == null) {
            context.errorResponse();
            context.setErrorCode(ErrorCodeConstants.ERROR_CODE_CLAZZ_GROUP_NOT_EXIST);
        }
    }
}
