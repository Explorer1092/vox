package com.voxlearning.utopia.service.push.impl.builder;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.utopia.service.push.api.constant.PushConstants;
import com.voxlearning.utopia.service.push.api.constant.PushTargetType;
import com.voxlearning.utopia.service.push.api.support.PushContext;

import java.util.HashMap;
import java.util.Map;

/**
 * @author xinxin
 * @since 6/26/17.
 */
public abstract class AbstractUmengInvokerMessageBuilder implements InvokeMessageBuilder {
    protected void fillTargetInfo(Map<String, Object> message, PushContext context) {
        if (context.getTargetType().equals(PushTargetType.ALIAS.name())) {
            message.put(PushConstants.PUSH_FIELD_UMENG_PUSH_TYPE, PushConstants.PUSH_VALUE_UMENG_PUSH_TYPE_CUSTOM);
            message.put(PushConstants.PUSH_FIELD_UMENG_ALIAS_TYPE, PushConstants.PUSH_VALUE_UMENG_ALIAS_TYPE_UID);
            message.put(PushConstants.PUSH_FIELD_UMENG_ALIAS, StringUtils.join(context.getAliases(), PushConstants.PUSH_UMENG_ALIAS_SPLIT));
        } else {
            message.put(PushConstants.PUSH_FIELD_UMENG_PUSH_TYPE, PushConstants.PUSH_VALUE_UMENG_PUSH_TYPE_GROUP);
            Map<String, Object> where = new HashMap<>();
            where.put(PushConstants.PUSH_FIELD_UMENG_FILTER_WHERE, context.getFilter());

            message.put(PushConstants.PUSH_FIELD_UMENG_FILTER, where);
        }
    }
}
