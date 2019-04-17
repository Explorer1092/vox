/*
 * VOX LEARNING TECHNOLOGY, INC. CONFIDENTIAL
 *
 * Copyright 2006-2013 Vox Learning Technology, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Vox Learning Technology, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Vox Learning
 * Technology, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Vox Learning Technology, Inc.
 */

package com.voxlearning.washington.data;

import lombok.AccessLevel;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.HashMap;
import java.util.Map;

/**
 * @author RuiBao
 * @version 0.1
 * @since 13-12-5
 */
@Data
@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class MobileAuthContext implements Serializable {
    private static final long serialVersionUID = 3692267512665413439L;
    private int code;
    private Map<String, Object> data;

    public static MobileAuthContext successContext() {
        MobileAuthContext context = new MobileAuthContext();
        context.setCode(0);
        context.setData(new HashMap<String, Object>());
        return context;
    }

    public static MobileAuthContext errorContext(int code) {
        MobileAuthContext context = new MobileAuthContext();
        context.setCode(code);
        context.setData(new HashMap<String, Object>());
        return context;
    }

    public MobileAuthContext add(String key, Object value) {
        data.put(key, value);
        return this;
    }

    public Object get(String key) {
        return data.get(key);
    }
}
