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

import com.voxlearning.utopia.service.business.impl.support.BusinessServiceSpringBean;

/**
 * @author Rui.Bao
 * @since 2014-09-11 1:43 PM
 */
abstract public class AbstractTeacherClazzIndexDataLoader extends BusinessServiceSpringBean {

    final public TeacherClazzIndexDataContext process(TeacherClazzIndexDataContext context) {
        if (context == null) {
            return null;
        }
        try {
            return doProcess(context);
        } catch (Exception ex) {
            logger.error(ex.getMessage(), ex);
            return context;
        }
    }

    abstract protected TeacherClazzIndexDataContext doProcess(TeacherClazzIndexDataContext context);
}
