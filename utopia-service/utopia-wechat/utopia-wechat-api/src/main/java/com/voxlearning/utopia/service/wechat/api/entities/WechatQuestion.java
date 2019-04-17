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

package com.voxlearning.utopia.service.wechat.api.entities;

import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.wechat.api.constants.SourceType;
import com.voxlearning.utopia.service.wechat.api.constants.WechatQuestionState;
import lombok.Getter;
import lombok.NonNull;
import lombok.Setter;

/**
 * @author xin
 * @since 14-4-24 上午10:11
 * <p/>
 * 本表记录微信端用户在线提交的问题
 * FIXME: DISABLED字段可以设置数据库缺省值FALSE
 */
@DocumentTable(table = "VOX_WECHAT_QUESTION")
public class WechatQuestion extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = -6209568010188379552L;

    @UtopiaSqlColumn(name = "OPEN_ID") @NonNull @Getter @Setter private String openId;  //微信帐号标识
    @UtopiaSqlColumn(name = "CONTENT") @NonNull @Getter @Setter private String content; //提问内容
    @UtopiaSqlColumn(name = "STATE") @NonNull @Getter @Setter private Integer state;    //处理状态
    @UtopiaSqlColumn(name = "REPLY") @Getter @Setter private String reply;                              //回复内容
    @UtopiaSqlColumn(name = "REPLYER") @Getter @Setter private String replyer;                          //回复人
    @UtopiaSqlColumn(name = "SOURCE_TYPE") @Getter @Setter private SourceType sourceType;                          //来源

    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(WechatQuestion.class, id);
    }

    public static WechatQuestion newInstance(String openId, String content, WechatQuestionState state, SourceType sourceType) {
        if (openId == null) throw new NullPointerException();
        if (content == null) throw new NullPointerException();
        if (state == null) throw new NullPointerException();
        if (sourceType == null) throw new NullPointerException();
        WechatQuestion inst = new WechatQuestion();
        inst.setDisabled(false);
        inst.setOpenId(openId);
        inst.setContent(content);
        inst.setSourceType(sourceType);
        inst.setState(state.getType());
        return inst;
    }
}
