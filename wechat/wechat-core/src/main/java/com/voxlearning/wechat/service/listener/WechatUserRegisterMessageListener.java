package com.voxlearning.wechat.service.listener;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserService;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.user.consumer.ParentServiceClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.wechat.cache.WechatWebCacheSystem;
import com.voxlearning.wechat.constants.WechatRegisterEventType;
import com.voxlearning.wechat.support.WechatUserInfo;
import com.voxlearning.wechat.support.WechatUserInfoHelper;
import com.voxlearning.wechat.support.utils.StringExtUntil;
import com.voxlearning.wechat.support.utils.TokenHelper;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.Map;
import java.util.Optional;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.wechat.user.register.message.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.wechat.user.register.message.queue")
        },
        maxPermits = 4
)
public class WechatUserRegisterMessageListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private WechatWebCacheSystem wechatWebCacheSystem;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private ParentServiceClient parentServiceClient;

    @ImportService(interfaceClass = ChipsWechatUserService.class)
    private ChipsWechatUserService chipsWechatUserService;

    @Inject
    private WechatLoaderClient wechatLoaderClient;

    @Inject
    private TokenHelper tokenHelper;

    private static String PRE = "Wechat:WechatUserRegisterMessageListener:";

    @Override
    public void onMessage(Message message) {
        if (message == null) {
            logger.error("video handle pic queue no message");
            return;
        }
        Object body = message.decodeBody();

        if (body instanceof String) {
            String json = (String) body;
            Map<String, Object> param = JsonUtils.fromJson(json);
            if (param == null) {
                logger.error("WechatUserRegisterMessageListener error. message:{}", body);
                return;
            }

            String openId = SafeConverter.toString(param.get("ID"));
            WechatType type = WechatType.of(SafeConverter.toInt(param.get("T")));
            WechatRegisterEventType eventType = WechatRegisterEventType.safeOf(SafeConverter.toString(param.get("R")));
            if (StringUtils.isBlank(openId) || type == null || eventType == null) {
                logger.error("WechatUserRegisterMessageListener error. message:{}", body);
                return;
            }
            String key = PRE + StringExtUntil.md5(json);
            if (exist(key)) {
                return;
            }

            WechatUserInfo info = WechatUserInfoHelper.getInfo(tokenHelper.getAccessToken(type), openId);
            User user = wechatLoaderClient.loadWechatUser(openId);

            processParentExt(user, info);

            if (type == WechatType.CHIPS) {
                processChips(openId, info, user != null ? user.getId() : null);
            }

            save(key);
        }
    }

    private boolean exist(String key) {
        Object val = wechatWebCacheSystem.CBS.unflushable.load(key);
        return val != null;
    }

    private void save(String key) {
       wechatWebCacheSystem.CBS.unflushable.set(key, 60 * 60, "1");
    }

    private void processParentExt(User user, WechatUserInfo info) {
        if (user == null || !user.isParent() || info == null) {
            return;
        }
        Long userId = user.getId();
        try {
            ParentExtAttribute parentExtAttribute = parentLoaderClient.loadParentExtAttribute(userId);
            if (parentExtAttribute == null) {
                parentExtAttribute = new ParentExtAttribute(userId);
                parentExtAttribute.setWechatNick(info.getNickname());
                parentExtAttribute.setWechatImage(info.getHeadimgurl());
                parentServiceClient.generateParentExtAttribute(parentExtAttribute);
            } else {
                parentExtAttribute.setWechatImage(info.getHeadimgurl());
                parentExtAttribute.setWechatNick(info.getNickname());
                parentServiceClient.updateParentExtAttribute(parentExtAttribute);
            }
        } catch (Exception e) {
            logger.error("fetch and save WechatUserInfo error. userId:{}, openId:{}", userId, e);
        }
    }

    private void processChips(String openId, WechatUserInfo info, Long userId) {
        String nick = Optional.ofNullable(info)
                .map(WechatUserInfo::getNickname)
                .orElse("");
        String avatar = Optional.ofNullable(info)
                .map(WechatUserInfo::getHeadimgurl)
                .orElse("");
        chipsWechatUserService.register(openId, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name(), nick, avatar, userId);
    }
}
