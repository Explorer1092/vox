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

package com.voxlearning.utopia.agent.persist.entity;

import com.voxlearning.alps.annotation.cache.UtopiaCacheExpiration;
import com.voxlearning.alps.annotation.dao.DocumentConnection;
import com.voxlearning.alps.annotation.dao.DocumentFieldIgnore;
import com.voxlearning.alps.annotation.dao.jdbc.DocumentTable;
import com.voxlearning.alps.annotation.dao.jdbc.UtopiaSqlColumn;
import com.voxlearning.alps.core.util.CacheKeyGenerator;
import com.voxlearning.utopia.agent.constants.AgentTag;
import com.voxlearning.utopia.core.AbstractDatabaseEntity;
import lombok.Getter;
import lombok.Setter;

import java.util.Date;
import java.util.List;

/**
 * Notify Entity
 * Created by Shuai.Huan on 2014/7/21.
 */
@Getter
@Setter
@DocumentConnection(configName = "agent")
@DocumentTable(table = "AGENT_NOTIFY")
@UtopiaCacheExpiration
public class AgentNotify extends AbstractDatabaseEntity {

    private static final long serialVersionUID = 5712319652963168315L;

    @UtopiaSqlColumn String notifyType;              // 通知类型
    @UtopiaSqlColumn String notifyTitle;             // 通知题目
    @UtopiaSqlColumn String notifyContent;           // 通知内容
    @UtopiaSqlColumn String file1;                   // 附件1
    @UtopiaSqlColumn String file2;                   // 附件2
    @UtopiaSqlColumn String notifyUrl;               // 通知跳转链接   根据notifyType 通知类型判断是站内还是站外的吧   消息中心的跳转链接不一定会配站内的  消息类型为 消息中心通知  的时候直接 按站外的搞吧


    @DocumentFieldIgnore List<String> tagList;      // 通知上的标签列表

    @UtopiaSqlColumn Long createUserId; //创建人Id
    @UtopiaSqlColumn String createUserName; //创建人
    @UtopiaSqlColumn Integer sendRange;// 1 指定部门 2 指定用户
    @UtopiaSqlColumn String photoUrl;  //系统配图地址  目前只做 不展示
    @UtopiaSqlColumn Integer msgStatus; //0 草稿 1 已发送 -1 已删除
    @UtopiaSqlColumn Date sendDatetime; //发送时间
    @UtopiaSqlColumn Boolean disabled;//是否删除
    @UtopiaSqlColumn Integer sendNum; //发送量
    @UtopiaSqlColumn Integer openNum; //打开量
    public static String ck_id(Long id) {
        return CacheKeyGenerator.generateCacheKey(AgentNotify.class, id);
    }
}
