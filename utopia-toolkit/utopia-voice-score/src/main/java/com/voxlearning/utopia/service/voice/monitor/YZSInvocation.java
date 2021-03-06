/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2011-2018 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.voice.monitor;

import com.voxlearning.alps.annotation.common.Install;
import com.voxlearning.alps.annotation.common.JmxType;
import com.voxlearning.alps.annotation.common.MBeanInfo;
import com.voxlearning.alps.spi.monitor.InvocationMonitorPoint;
import com.voxlearning.alps.spi.monitor.MonitorPointInfo;

@Install
@MBeanInfo(type = JmxType.monitor, name = "YZSInvocation")
@MonitorPointInfo(id = "YZSInvocation", measurement = "voice")
final public class YZSInvocation extends InvocationMonitorPoint {
}
