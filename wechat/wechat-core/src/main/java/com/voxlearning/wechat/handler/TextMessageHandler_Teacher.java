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
import com.voxlearning.alps.lang.mapper.json.JsonUtils;
import com.voxlearning.alps.random.RandomUtils;
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

/**
 * @author Xin Xin
 * @since 10/21/15
 */
public class TextMessageHandler_Teacher extends AbstractHandler {
    @Override
    public String getFingerprint() {
        return WechatType.TEACHER.name() + ":" + MessageFields.FIELD_CONTENT;

    }

    @Override
    public String handle(MessageContext context) {
        List<String> keywords = MessageParser.analyze(context.getContent()); //分词

        List<WechatFaq> questions = wechatLoaderClient.matchWechatFaq(StringUtils.join(keywords, " "), WechatType.TEACHER);
        ServiceMessageBuilder sb = new ServiceMessageBuilder(context.getFromUserName());
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

            for (int i = 0; i < qs.size(); i++) {
                List<String> picUrls = JsonUtils.fromJsonToList(qs.get(i).getPicUrl(), String.class);
                String picUrl;
                if (i == 0) {
                    picUrl = picUrls.get(0);
                    if (picUrl.length() == 0) {
                        picUrl = "wechat-teacher-faq-bigfirst.png";
                    }
                } else {
                    picUrl = picUrls.get(1);
                }
                sb.buildArticleMsg(qs.get(i).getTitle(), qs.get(i).getDescription(), (picUrl.length() > 0 ? WechatConfig.getBaseSiteUrl() + "/teacher/test/wechatfaqimg/" + picUrl : ""), WechatConfig.getBaseSiteUrl() + "/teacher/faq/question.vpage?id=" + qs.get(i).getId());
            }
        }
        messageSender.sendServiceMsg(sb.toString(), WechatType.TEACHER);

        //transfer customer service
        ReplyMessageBuilder rb = new ReplyMessageBuilder(context);
        rb.buildMsgType(MessageType.TRANSFER_CUSTOMER_SERVICE);
        return rb.toString();
    }
}
