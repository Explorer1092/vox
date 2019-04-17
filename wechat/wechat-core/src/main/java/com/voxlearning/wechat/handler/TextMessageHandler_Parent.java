/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.wechat.handler;

import com.voxlearning.alps.core.util.StringUtils;
import com.voxlearning.alps.random.RandomUtils;
import com.voxlearning.utopia.core.cdn.url2.CdnRuleType;
import com.voxlearning.utopia.service.wechat.api.constants.SourceType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import com.voxlearning.utopia.service.wechat.api.entities.WechatFaq;
import com.voxlearning.wechat.builder.ReplyMessageBuilder;
import com.voxlearning.wechat.builder.ServiceMessageBuilder;
import com.voxlearning.wechat.constants.MessageType;
import com.voxlearning.wechat.context.MessageContext;
import com.voxlearning.wechat.support.WechatConfig;
import com.voxlearning.wechat.support.utils.MessageFields;
import com.voxlearning.wechat.support.utils.MessageParser;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

/**
 * 文本消息处理
 * @author Xin Xin
 * @since 10/16/15
 */
public class TextMessageHandler_Parent extends AbstractHandler {

    @Override
    public String getFingerprint() {
        return WechatType.PARENT.name() + ":" + MessageFields.FIELD_CONTENT;
    }

    @Override
    public String handle(MessageContext context) {
        ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());

        if (context.getContent().trim().length() == 0) { //FIXME:微信会发一种content=""的奇怪消息
            return "success";
        }

        List<String> keywordsList = MessageParser.analyze(StringUtils.filterEmojiForMysql(context.getContent())); //分词
        Set<String> keywords = keywordsList.stream().filter(t -> t.trim().length() > 1).limit(20).collect(Collectors.toSet());
        if (keywords.size() > 0) {
            List<WechatFaq> questions = wechatLoaderClient.matchWechatFaq(StringUtils.join(keywords, " "), WechatType.PARENT);
            if (questions.size() == 0) {
                //没查到结果，将用户提交的问题存到库里
                wechatServiceClient.submitQuestion(context.getFromUserName(), context.getContent(), SourceType.WECHAT);

                return defaultReplyTextMsg(context);
            } else {
                //查到结果了，将分词结果存入库里
                wechatServiceClient.submitQuestion(context.getFromUserName(), "[" + StringUtils.join(keywords, " ") + "]", SourceType.WECHAT);
                List<WechatFaq> qs = new ArrayList<>();
                if (questions.size() <= 10) {
                    qs.addAll(questions);
                } else {
                    while (qs.size() < 10) {
                        int index = RandomUtils.nextInt(0, questions.size() - 1);
                        qs.add(questions.get(index));
                    }
                }

                String siteUrl = WechatConfig.getBaseSiteUrl();
                for (int i = 0; i < qs.size(); i++) {
                    String picUrl_prefix = cdnResourceUrlGenerator.combineCdnUrl(getRequestContext().getRequest(), CdnRuleType.AUTO.typeName(), "/wechat/public/images/help/icon_" + qs.get(i).getCatalogId());
                    sb.buildArticleMsg(qs.get(i).getTitle(), qs.get(i).getDescription(), (i == 0 ? picUrl_prefix + "/big-" + qs.get(i).getPicUrl() : picUrl_prefix + "/small-" + qs.get(i).getPicUrl()), siteUrl + "/faq/question.vpage?id=" + qs.get(i).getId());
                }
            }

            messageSender.sendServiceMsg(sb.toString(), WechatType.PARENT);

            //transfer customer service
            ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
            rb.buildMsgType(MessageType.TRANSFER_CUSTOMER_SERVICE);
            return rb.toString();
        }else{
            return defaultReplyTextMsg(context);
        }
    }
}
