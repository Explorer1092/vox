package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.util.MapMessage;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserService;
import com.voxlearning.utopia.service.ai.constant.ChipsErrorType;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExposeService(interfaceClass = ChipsWechatUserService.class)
public class ChipsWechatUserServiceImpl implements ChipsWechatUserService {

    @Inject
    private ChipsWechatUserPersistence wechatUserPersistence;

    @Override
    public MapMessage register(String openId, String type) {
        if (StringUtils.isAnyBlank(openId, type)) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        WechatUserType userType = WechatUserType.safeOf(type);
        wechatUserPersistence.insertOrUpdate(openId, userType);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage register(String openId, String type, String nickName, String avatar, Long userId) {
        if (StringUtils.isAnyBlank(openId, type)) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        WechatUserType userType = WechatUserType.safeOf(type);
        wechatUserPersistence.insertOrUpdate(openId, userType, nickName, avatar, userId);
        return MapMessage.successMessage();
    }

    @Override
    public MapMessage updateUserInfo(Long wechatUserId, String nickName, String avatar) {
        if (wechatUserId == null) {
            return MapMessage.errorMessage(ChipsErrorType.PARAMETER_ERROR.getInfo()).setErrorCode(ChipsErrorType.PARAMETER_ERROR.getCode());
        }
        wechatUserPersistence.update(wechatUserId, nickName, avatar);
        return MapMessage.successMessage();
    }
}
