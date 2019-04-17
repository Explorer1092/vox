package com.voxlearning.utopia.service.ai.impl.service;

import com.voxlearning.alps.annotation.remote.ExposeService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserLoader;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.data.ChipsWechatUser;
import com.voxlearning.utopia.service.ai.entity.ChipsWechatUserEntity;
import com.voxlearning.utopia.service.ai.impl.persistence.ChipsWechatUserPersistence;

import javax.inject.Inject;
import javax.inject.Named;

@Named
@ExposeService(interfaceClass = ChipsWechatUserLoader.class)
public class ChipsWechatUserLoaderImpl implements ChipsWechatUserLoader {

    @Inject
    private ChipsWechatUserPersistence wechatUserPersistence;

    @Override
    public ChipsWechatUser loadByOpenId(String openId, String type) {
        if (StringUtils.isAnyBlank(openId, type)) {
            return null;
        }
        WechatUserType userType = WechatUserType.safeOf(type);
        ChipsWechatUserEntity wechatUserEntity = wechatUserPersistence.loadByOpenIdAndType(openId, userType.getCode());
        if (wechatUserEntity == null) {
            return null;
        }
        ChipsWechatUser user = new ChipsWechatUser();
        user.setNickName(wechatUserEntity.getNickName());
        user.setAvatar(wechatUserEntity.getAvatar());
        user.setOpenId(wechatUserEntity.getOpenId());
        user.setUserId(wechatUserEntity.getUserId());
        user.setWechatUserId(wechatUserEntity.getId());

        return user;
    }
}
