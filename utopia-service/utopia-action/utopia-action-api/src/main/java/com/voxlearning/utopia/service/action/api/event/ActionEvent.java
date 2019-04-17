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

package com.voxlearning.utopia.service.action.api.event;

import com.voxlearning.alps.spi.queue.MessageTransformer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Action event data structure.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since Aug 3, 2016
 */
@Getter
@Setter
public class ActionEvent implements Serializable, MessageTransformer {
    private static final long serialVersionUID = 8917556245377494813L;

    private Long userId;
    private ActionEventType type;
    private long timestamp = System.currentTimeMillis();
    private Map<String, Object> attributes = new LinkedHashMap<>();
}
