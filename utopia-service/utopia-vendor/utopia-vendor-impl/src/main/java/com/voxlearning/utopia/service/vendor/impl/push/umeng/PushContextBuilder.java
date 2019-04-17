package com.voxlearning.utopia.service.vendor.impl.push.umeng;

import com.voxlearning.utopia.service.push.api.constant.AppMessageSource;
import com.voxlearning.utopia.service.push.api.constant.PushConstants;
import com.voxlearning.utopia.service.push.api.support.PushContext;
import com.voxlearning.utopia.service.push.api.constant.PushTargetType;
import com.voxlearning.utopia.service.push.api.support.PushRetryContext;

import java.time.Instant;
import java.time.format.DateTimeFormatter;
import java.util.*;

/**
 * @author xinxin
 * @since 10/11/2016
 */
public class PushContextBuilder {
    private PushContext context;

    public static PushContextBuilder instance() {
        PushContextBuilder builder = new PushContextBuilder();
        PushContext context = new PushContext();
        builder.context = context;

        context.setExtInfo(new HashMap<>());
        context.setClientInfo(new HashMap<>());
        context.setAliases(new HashSet<>());

        return builder;
    }

    public static PushContextBuilder retryInstance(PushContext ctx) {
        PushContextBuilder builder = new PushContextBuilder();
        PushContext context = new PushRetryContext();
        builder.context = context;

        context.setSource(ctx.getSource());
        context.setAliases(ctx.getAliases());
        context.setContent(ctx.getContent());
        context.setExtInfo(ctx.getExtInfo());
        context.setTargetType(ctx.getTargetType());
        context.setTicker(ctx.getTicker());
        context.setTitle(ctx.getTitle());

        return builder;
    }

    public PushContext context() {
        Objects.requireNonNull(context.getTargetType());
        Objects.requireNonNull(context.getContent());

        return context;
    }

    public PushContextBuilder source(AppMessageSource source) {
        this.context.setSource(source);
        return this;
    }

    public PushContextBuilder targetType(PushTargetType type) {
        this.context.setTargetType(type.name());
        return this;
    }

    public PushContextBuilder alias(Collection<Long> aliases) {
        this.context.getAliases().addAll(aliases);
        return this;
    }

    /**
     * 消息内容
     */
    public PushContextBuilder content(String content) {
        this.context.setContent(content);
        return this;
    }

    /**
     * 消息跳转URL
     */
    public PushContextBuilder url(String url) {
        this.context.getExtInfo().put(PushConstants.PUSH_FIELD_UMENG_PAYLOAD_BODY_URL, url);
        return this;
    }

    /**
     * 自定义的消息标识
     */
    public PushContextBuilder customerId(String customId) {
        this.context.getExtInfo().put(PushConstants.PUSH_FIELD_UMENG_THIRDPARTY_ID, customId);
        return this;
    }

    /**
     * 定时发送的时间
     */
    public PushContextBuilder startTime(Instant startDate) {
        if (startDate.isAfter(Instant.now())) {
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");
            this.context.getExtInfo().put(PushConstants.PUSH_FIELD_UMENG_POLICY_START_TIME, formatter.format(startDate));
        }
        return this;
    }

    public PushContextBuilder description(String desc) {
        this.context.getExtInfo().put(PushConstants.PUSH_FIELD_UMENG_DESC, desc);
        return this;
    }

    public PushContextBuilder title(String title) {
        this.context.setTitle(title);
        return this;
    }

    public PushContextBuilder duration(Integer duration) {
        if (null != duration && duration > 0) {
            this.context.setDuration(duration);
        }
        return this;
    }

    public PushContextBuilder clientInfo(Map<String, Object> extInfo) {
        this.context.setClientInfo(extInfo);
        return this;
    }

    public PushContextBuilder filter(Condition condition) {
        this.context.setFilter(condition.toMap());
        return this;
    }

}
