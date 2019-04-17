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

package com.voxlearning.utopia.service.surl;

import com.voxlearning.alps.embed.jetty.legacy.EmbeddedJettyServer;
import lombok.AccessLevel;
import lombok.NoArgsConstructor;
import com.voxlearning.alps.repackaged.org.apache.commons.lang3.mutable.MutableObject;

@NoArgsConstructor(access = AccessLevel.PRIVATE)
public class EmbeddedServerContainer extends MutableObject<EmbeddedJettyServer> {
    public static final EmbeddedServerContainer INSTANCE = new EmbeddedServerContainer();
}
