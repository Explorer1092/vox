package com.voxlearning.utopia.service.wechat.impl.service.wechat.processor.template.message;

import com.voxlearning.alps.core.util.CollectionUtils;
import com.voxlearning.alps.core.util.MapUtils;
import com.voxlearning.alps.lang.convert.SafeConverter;
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.lang.util.SpringContainerSupport;
import com.voxlearning.utopia.service.wechat.api.constants.WechatNoticeState;
import com.voxlearning.utopia.service.wechat.api.constants.WechatTemplateMessageType;
import com.voxlearning.utopia.service.wechat.api.data.WechatTemplateData;
import com.voxlearning.utopia.service.wechat.api.entities.UserWechatRef;
import com.voxlearning.utopia.service.wechat.api.entities.WechatTemplateMessageRecord;
import com.voxlearning.utopia.service.wechat.consumer.helpers.WechatCodeManager;
import com.voxlearning.utopia.service.wechat.impl.dao.UserWechatRefPersistence;
import com.voxlearning.utopia.service.wechat.impl.dao.WechatTemplateMessageRecordPersistence;
import com.voxlearning.utopia.service.wechat.impl.support.WechatTemplateMessageUtil;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.*;

@Named
public abstract class WechatTemplateMessageProcessor extends SpringContainerSupport {

    @Inject private WechatCodeManager wechatCodeManager;
    @Inject private UserWechatRefPersistence userWechatRefPersistence;
    @Inject private WechatTemplateMessageRecordPersistence wechatTemplateMessageRecordPersistence;
    @Inject private WechatTemplateMessageProcessorManager wechatTemplateMessageProcessorManager;

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        wechatTemplateMessageProcessorManager.register(this);
    }

    protected abstract WechatTemplateMessageType type();

    protected abstract Map<String, Object> params(Map<String, Object> extMap);

    public void process(Long userId, Map<String, WechatTemplateData> templateDataMap, Map<String, Object> extMap) {
        List<UserWechatRef> wechatRef = userWechatRefPersistence.findByUserId(userId, type().getWechatType().getType());
        if (CollectionUtils.isEmpty(wechatRef)) {
            return;
        }
        Map<String, Object> params = params(extMap);

        if (MapUtils.isEmpty(params) && MapUtils.isEmpty(extMap)) {
            logger.warn("no params. type:{}", type());
            return;
        }

        Map<String, Object> paramsMap = new HashMap<>();
        if (MapUtils.isNotEmpty(params)) {
            paramsMap.putAll(params);
        }

        if (MapUtils.isNotEmpty(extMap)) {
            paramsMap.putAll(extMap);
        }
        paramsMap.put("data", MapUtils.isNotEmpty(templateDataMap) ? templateDataMap : Collections.emptyMap());
        String token = wechatCodeManager.generateAccessToken(type().getWechatType());
        for (UserWechatRef userWechatRef : wechatRef) {
            paramsMap.put("touser", userWechatRef.getOpenId());
            WechatNoticeState state = WechatNoticeState.WAITTING;
            WechatTemplateMessageRecord messageNotice = WechatTemplateMessageRecord.newInstance(userId, userWechatRef.getOpenId(), JsonUtils.toJson(paramsMap), type(), state);
            Map<String, Object> resMap = WechatTemplateMessageUtil.send(token, paramsMap);
            String msgId = Optional.of(resMap)
                    .filter(MapUtils::isNotEmpty)
                    .map(e -> SafeConverter.toString(e.get("msgid"), ""))
                    .orElse(null);
            if (MapUtils.isNotEmpty(resMap) && "0".equals(SafeConverter.toString(resMap.get("errcode"), ""))) {
                state = WechatNoticeState.SENDED;
            } else {
                state = WechatNoticeState.FAILED;
                messageNotice.setErrorCode(MapUtils.isNotEmpty(resMap) ? SafeConverter.toString(resMap.get("errcode"), "") : "500");
            }
            messageNotice.setState(state);
            messageNotice.setMessageId(msgId);
            wechatTemplateMessageRecordPersistence.insertOrUpdate(messageNotice);
        }
    }
}
