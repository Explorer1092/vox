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

package com.voxlearning.utopia.service.vendor.api.entity;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.Map;

/**
 * 这个数据结构别在继续使用了，现在保留在这里仅仅为了中学端兼容
 * 另外，所有的实体信息也被删除了
 */
@Deprecated
@Getter
@Setter
public class AppUserMessageDynamic implements Serializable {
    private static final long serialVersionUID = 5078399183670281430L;

    private String id;
    private Long userId;
    private Integer messageType;            //消息类型
    private String title;                   //消息title
    private String content;                 //消息概要
    private String imageUrl;                //运营消息的图片url
    private String linkUrl;                 //消息跳转的url
    private Integer linkType;               //链接类型：0站外，1站内==站外用绝对地址，站内用相对地址
    private Map<String, Object> extInfo;    //扩展信息
    private Boolean isTop;                  //是否置顶
    private Long topEndTime;                //置顶截止时间
    private Boolean viewed;                 //是否已经被浏览
    private Long expiredTime;               //过期时间
    private String popupTitle;              //提示标题
    private String appTagMsgId;             //对应TagMessage的Id，可以为空
    private Long createTime;
}
