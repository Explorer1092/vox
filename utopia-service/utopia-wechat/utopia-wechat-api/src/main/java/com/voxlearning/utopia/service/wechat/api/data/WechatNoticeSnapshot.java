/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.utopia.service.wechat.api.data;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.io.Serializable;
import java.util.Date;

/**
 * @author HuanYin Jia
 * @since 2015/5/22
 */
@Getter
@Setter
@NoArgsConstructor
public class WechatNoticeSnapshot implements Serializable {
    private static final long serialVersionUID = 3495517410214199349L;

    private Long id;
    private String openId;
    private String message;
    private Integer messageType;
    private Integer state;
    private Date createTime;
    private String errorCode;
    private String stateDesc;
    private String typeDesc;
}
