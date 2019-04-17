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

import com.voxlearning.utopia.service.user.api.entities.Clazz;

import javax.inject.Named;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * @author Rui.Bao
 * @since 2014-09-11 2:05 PM
 */
@Named
public class LoadTerminalClazzInfo extends AbstractTeacherClazzIndexDataLoader {

    @Override
    protected TeacherClazzIndexDataContext doProcess(TeacherClazzIndexDataContext context) {
        if (!context.isSkipNextAll()) {
            List<Clazz> terminalClazzs = context.getClazzs().stream()
                    .filter(Clazz::isPublicClazz)
                    .filter(Clazz::isTerminalClazz)
                    .collect(Collectors.toList());
            List<Map<String, Object>> list = new ArrayList<>();
            for (Clazz clazz : terminalClazzs) {
                Map<String, Object> map = new HashMap<>();
                map.put("clazzId", clazz.getId());
                map.put("clazzName", clazz.formalizeClazzName());
                list.add(map);
            }
            context.getParam().put("graduatedClazzs", list);
        }
        return context;
    }
}
