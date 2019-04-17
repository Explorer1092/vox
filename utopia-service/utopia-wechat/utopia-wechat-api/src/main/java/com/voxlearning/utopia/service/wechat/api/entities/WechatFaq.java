/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2017 Shanghai Sunny Education, Inc. All Rights Reserved.
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

import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentField;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.utopia.core.AbstractDatabaseEntityWithDisabledField;
import com.voxlearning.utopia.service.wechat.api.constants.WechatType;
import lombok.Getter;
import lombok.Setter;

/**
 * @author xin.xin
 * @since 2014-04-18
 * FIXME: DISABLED字段可以设置数据库缺省值FALSE
 */
@Getter
@Setter
@DocumentConnection(configName = DocumentConnection.DEFAULT_JDBC_CONFIG_NAME)
@DocumentTable(table = "VOX_WECHAT_FAQ")
public class WechatFaq extends AbstractDatabaseEntityWithDisabledField {
    private static final long serialVersionUID = 3849988676481702498L;

    @DocumentField("CATALOG_ID") private Long catalogId;        //问题所属的分类ID
    @DocumentField("TITLE") private String title;               //问题的标题
    @DocumentField("DESCRIPTION") private String description;   //问题的简介
    @DocumentField("PICURL") private String picUrl;             //问题的标题图片（暂时不用）
    @DocumentField("KEYWORD") private String keyWord;           //问题关联的关键词，多个以逗号分隔
    @DocumentField("CONTENT") private String content;           //问题答案的内容
    @DocumentField("STATUS") private String status;             //问题的发布状态(draft/published)
    @DocumentField("TYPE") private Integer type;                //问题的所属微信类型(参考WechatType)

    public static WechatFaq newInstance(Long catalogId,
                                        String title,
                                        String keyWord,
                                        String content) {
        if (catalogId == null) throw new NullPointerException();
        if (title == null) throw new NullPointerException();
        if (keyWord == null) throw new NullPointerException();
        if (content == null) throw new NullPointerException();
        WechatFaq inst = new WechatFaq();
        inst.setDisabled(false);
        inst.setCatalogId(catalogId);
        inst.setTitle(title);
        inst.setKeyWord(keyWord);
        inst.setContent(content);
        inst.setStatus("draft");
        inst.setType(WechatType.PARENT.getType());
        return inst;
    }

    public WechatFaq withStatus(String status) {
        this.status = status;
        return this;
    }
}
