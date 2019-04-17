/*
 * SHANGHAI SUNNY EDUCATION, INC. CONFIDENTIAL
 *
 * Copyright 2006-2016 Shanghai Sunny Education, Inc. All Rights Reserved.
 *
 * NOTICE: All information contained herein is, and remains the property of
 * Shanghai Sunny Education, Inc. and its suppliers, if any. The intellectual
 * and technical concepts contained herein are proprietary to Shanghai Sunny
 * Education, Inc. and its suppliers and may be covered by patents, patents
 * in process, and are protected by trade secret or copyright law. Dissemination
 * of this information or reproduction of this material is strictly forbidden
 * unless prior written permission is obtained from Shanghai Sunny Education, Inc.
 */

package com.voxlearning.utopia.service.business.impl.service.teacher.internal.clazzindex;

import com.voxlearning.alps.lang.util.SpringContainerSupport;
import lombok.NoArgsConstructor;
import lombok.extern.slf4j.Slf4j;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

/**
 * @author Rui.Bao
 * @since 2014-09-11 1:42 PM
 */
@Named
@Slf4j
@NoArgsConstructor
public class TeacherClazzIndexDataLoader extends SpringContainerSupport {
    @Inject private LoadClazzInfo loadClazzInfo;
    @Inject private LoadTerminalClazzInfo loadTerminalClazzInfo;
    @Inject private LoadMiscConditions loadMiscConditions;

    private final List<AbstractTeacherClazzIndexDataLoader> chains = new LinkedList<>();

    @Override
    public void afterPropertiesSet() throws Exception {
        super.afterPropertiesSet();
        chains.add(loadClazzInfo); // 必须放在第一个
        chains.add(loadTerminalClazzInfo);
        chains.add(loadMiscConditions);
    }

    public Map<String, Object> process(final TeacherClazzIndexDataContext context) {
        TeacherClazzIndexDataContext contextForUse = context;
        for (AbstractTeacherClazzIndexDataLoader unit : chains) {
            contextForUse = unit.process(contextForUse);
        }
        return contextForUse.getParam();
    }
}
