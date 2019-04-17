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

import com.voxlearning.utopia.service.business.impl.loader.extension.ExtensionClazzLoader;
import com.voxlearning.utopia.service.user.api.entities.Clazz;
import com.voxlearning.utopia.service.user.api.entities.extension.Teacher;

import javax.inject.Inject;
import javax.inject.Named;
import java.util.List;
import java.util.stream.Collectors;

/**
 * @author Rui.Bao
 * @since 2014-09-11 2:05 PM
 */
@Named
public class LoadClazzInfo extends AbstractTeacherClazzIndexDataLoader {
    @Inject private ExtensionClazzLoader extensionClazzLoader;

    @Override
    protected TeacherClazzIndexDataContext doProcess(TeacherClazzIndexDataContext context) {
        Teacher teacher = context.getTeacher();
        List<Clazz> clazzs = context.getClazzs().stream()
                .filter(Clazz::isPublicClazz)
                .filter(e -> !e.isTerminalClazz())
                .collect(Collectors.toList());

        if (clazzs.isEmpty()) {
            context.setSkipNextAll(true);
            context.getParam().put("noClazz", "redirect:/teacher/showtip.vpage");
            return context;
        }
        context.getParam().put("clazzs", extensionClazzLoader.loadTeacherClazzMappers(teacher, clazzs));
        return context;
    }
}
