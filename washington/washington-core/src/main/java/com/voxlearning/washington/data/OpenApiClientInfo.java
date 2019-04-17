/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2015 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.washington.data;

import com.voxlearning.alps.random.RandomUtils;
import lombok.Data;

import java.io.Serializable;


@Data
public class OpenApiClientInfo implements Serializable {

    private static final long serialVersionUID = -4021018087336888681L;

    private Long uid;

    private String token;

    private Long creatAt;

    private Long actionAt;


    public static OpenApiClientInfo build(Long uid) {
        OpenApiClientInfo info = new OpenApiClientInfo();
        info.uid = uid;
        info.creatAt = System.currentTimeMillis();
        info.actionAt = info.creatAt;
        info.token = info.creatAt + RandomUtils.randomString(16);
        return info;
    }

}
