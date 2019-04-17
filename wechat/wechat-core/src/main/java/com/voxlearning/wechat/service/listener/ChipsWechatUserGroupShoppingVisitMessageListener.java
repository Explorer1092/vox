package com.voxlearning.wechat.service.listener;

import com.voxlearning.alps.annotation.remote.ImportService;
import com.voxlearning.alps.calendar.DateUtils;
import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.logger.LoggerFactory;
import com.voxlearning.alps.spi.queue.*;
import com.voxlearning.utopia.service.ai.api.ChipsEnglishUserLoader;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserLoader;
import com.voxlearning.utopia.service.ai.api.ChipsWechatUserService;
import com.voxlearning.utopia.service.ai.client.AiChipsEnglishConfigServiceClient;
import com.voxlearning.utopia.service.ai.constant.WechatUserType;
import com.voxlearning.utopia.service.ai.data.ChipsWechatUser;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishClass;
import com.voxlearning.utopia.service.ai.entity.ChipsEnglishPageContentConfig;
import com.voxlearning.utopia.service.user.api.entities.ParentExtAttribute;
import com.voxlearning.utopia.service.user.api.entities.User;
import com.voxlearning.utopia.service.user.consumer.ParentLoaderClient;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.client.WechatLoaderClient;
import com.voxlearning.utopia.service.wechat.client.WechatServiceClient;
import com.voxlearning.wechat.cache.WechatWebCacheSystem;
import com.voxlearning.wechat.support.WechatUserInfo;
import com.voxlearning.wechat.support.WechatUserInfoHelper;
import com.voxlearning.wechat.support.utils.TokenHelper;
import org.slf4j.Logger;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;
import java.util.stream.Collectors;

@Named
@QueueConsumer(
        destinations = {
                @QueueDestination(system = QueueSystem.KFK, config = "primary", queue = "utopia.chips.wechat.user.group.shopping.visit.queue"),
                @QueueDestination(system = QueueSystem.KFK, config = "main-backup", queue = "utopia.chips.wechat.user.group.shopping.visit.queue")
        },
        maxPermits = 4
)
public class ChipsWechatUserGroupShoppingVisitMessageListener implements MessageListener {
    private final Logger logger = LoggerFactory.getLogger(getClass());

    @Inject
    private AiChipsEnglishConfigServiceClient chipsEnglishConfigService;

    @ImportService(interfaceClass = ChipsWechatUserLoader.class)
    private ChipsWechatUserLoader chipsWechatUserLoader;

    @ImportService(interfaceClass = ChipsWechatUserService.class)
    private ChipsWechatUserService chipsWechatUserService;

    @ImportService(interfaceClass = ChipsEnglishUserLoader.class)
    private ChipsEnglishUserLoader chipsEnglishUserLoader;

    @Inject
    private WechatLoaderClient wechatLoaderClient;

    @Inject
    private WechatServiceClient wechatServiceClient;

    @Inject
    private ParentLoaderClient parentLoaderClient;

    @Inject
    private WechatWebCacheSystem wechatWebCacheSystem;

    @Inject
    private TokenHelper tokenHelper;

    private static String TEACHER_NAME = "clazz_teacher_user_cfg";

    private static String PRF = "ChipsWechatUserGroupShoppingVisitMessageListener_";

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
            if (StringUtils.isBlank(openId)) {
                return;
            }
            boolean exist = wechatWebCacheSystem.CBS.unflushable.load(PRF + openId) != null;
            if (exist) {
                return;
            }
            wechatWebCacheSystem.CBS.unflushable.set(PRF + openId, 60 * 60, "1");

            User user = wechatLoaderClient.loadWechatUser(openId);
            String nickName = Optional.ofNullable(openId)
                    .map(open -> {
                        ChipsWechatUser entity = chipsWechatUserLoader.loadByOpenId(open, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                        if (entity != null) {
                            return entity.getNickName();
                        }
                        if (user == null) {
                            WechatUserInfo info = WechatUserInfoHelper.getInfo(tokenHelper.getAccessToken(WechatType.CHIPS), openId);
                            if (info != null) {
                                chipsWechatUserService.register(open, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name(), info.getNickname(), info.getHeadimgurl(), null);
                                return info.getNickname();
                            }
                            chipsWechatUserService.register(open, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name());
                            return "";
                        }

                        ParentExtAttribute parentExt = parentLoaderClient.loadParentExtAttribute(user.getId());
                        if (parentExt != null) {
                            chipsWechatUserService.register(open, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name(), parentExt.getWechatNick(), parentExt.getWechatImage(), user.getId());
                            return parentExt.getWechatNick();
                        }
                        chipsWechatUserService.register(open, WechatUserType.CHIPS_OFFICIAL_ACCOUNTS.name(), "", "", user.getId());
                        return "";
                    })
                    .filter(StringUtils::isNotBlank)
                    .orElse("");
            processWechateTemplate(user, nickName);
        }

    }


    private void processWechateTemplate(User user, String nickName) {
        Set<String> teachers = null;
        if (user != null) {
            Collection<ChipsEnglishClass> clazzList = loadMyClazz(user.getId());
            if (CollectionUtils.isNotEmpty(clazzList)) {
                teachers = clazzList.stream().map(ChipsEnglishClass::getTeacher).collect(Collectors.toSet());
            }
        }

        Set<String> teacherCp = teachers;
        Set<Long> userIds = Optional.ofNullable(chipsEnglishConfigService.loadChipsEnglishConfigByName(TEACHER_NAME))
                .map(ChipsEnglishPageContentConfig::getValue)
                .filter(StringUtils::isNotBlank)
                .map(JsonUtils::fromJson)
                .filter(MapUtils::isNotEmpty)
                .map(e -> {
                    Set<Long> users = new HashSet<>();
                    for(Map.Entry<String, Object> entry : e.entrySet()) {
                        if (teacherCp == null || teacherCp.contains(entry.getKey())) {
                            users.add(SafeConverter.toLong(entry.getValue()));
                        }
                    }
                    return users;
                })
                .orElse(Collections.emptySet());
        String date = DateUtils.dateToString(new Date());
        for(Long senUser : userIds) {
            String frist = (user != null ? user.getId().toString(): "") + " " +nickName + "正在浏览续费广告页，快去私信她关单吧～";
            Map<String, WechatTemplateData> templateDataMap = new HashMap<>();
            templateDataMap.put("first", new WechatTemplateData(frist, null));
            templateDataMap.put("keyword1", new WechatTemplateData("微信", null));
            templateDataMap.put("keyword2", new WechatTemplateData(nickName, null));
            templateDataMap.put("keyword3", new WechatTemplateData("", null));
            templateDataMap.put("keyword4", new WechatTemplateData(date, null));
            templateDataMap.put("remark", new WechatTemplateData("", null));
            try {
                wechatServiceClient.getWechatService().processWechatTemplateMessageNotice(senUser, WechatTemplateMessageType.CHIPS_AD_VISIT_NOTIFY.name(), templateDataMap, Collections.emptyMap());
            } catch (Exception e) {
                logger.warn("send WechatTemplateMessage error. user:{}, dataMap:{} ", senUser, templateDataMap, e);
            }
        }
    }

    public Collection<ChipsEnglishClass> loadMyClazz(Long userId) {
        if (userId == null) {
            return Collections.emptyList();
        }
        return chipsEnglishUserLoader.loadMyClazz(userId);
    }
}
