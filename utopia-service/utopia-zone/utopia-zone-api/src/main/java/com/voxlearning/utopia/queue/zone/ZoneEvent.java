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

package com.voxlearning.utopia.queue.zone;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.voxlearning.alps.spi.queue.MessageTransformer;
import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;
import java.util.LinkedHashMap;
import java.util.Map;

@Getter
@Setter
public class ZoneEvent implements Serializable, MessageTransformer {
    private static final long serialVersionUID = 1026799935062018870L;

    @JsonProperty("T")
    private ZoneEventType type;
    @JsonProperty("TS")
    private long timestamp;
    private Map<String, Object> attributes = new LinkedHashMap<>();
}
