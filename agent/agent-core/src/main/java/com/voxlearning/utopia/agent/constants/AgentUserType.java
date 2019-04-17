/*
 *
 *  * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *  *
 *  *  Copyright 2006-2014 Vox Learning Technology, Inc. All Rights Reserved.
 *  *
 *  *  NOTICE: All information contained herein is, and remains the property of
 *  *  Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 *  *  and technical concepts contained herein are proprietary to Vox Learning
 *  *  Technology, Inc. and its suppliers and may be covered by patents, patents
 *  *  in process, and are protected by trade secret or copyright law. Dissemination
 *  *  of this information or reproduction of this material is strictly forbidden
 *  *  unless prior written permission is obtained from Vox Learning Technology, Inc.
 *
 */

package com.voxlearning.utopia.agent.constants;

import lombok.AccessLevel;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

/**
 *
 * Created by Shuai.Huan on 2014/7/11.
 */
@RequiredArgsConstructor(access = AccessLevel.PRIVATE)
public enum AgentUserType {

    INITIAL(0, "新建用户"),
    NORMAL(1, "普通用户"),
    CLOSED(9, "关闭用户");

    @Getter private final int status;
    @Getter private final String desc;

}
