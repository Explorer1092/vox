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

package com.voxlearning.utopia.service.newhomework.api.mapper;

import com.voxlearning.alps.core.util.PropertiesUtils;
import com.voxlearning.alps.spi.common.DateFormatParser;
import com.voxlearning.utopia.service.newhomework.api.entity.HomeworkComment;
import lombok.Getter;
import lombok.Setter;

/**
 * Extend homework comment data structure.
 *
 * @author Xiaohai Zhang
 * @serial
 * @since 2013-07-11 12:51
 */
public class ExHomeworkComment extends HomeworkComment {
    private static final long serialVersionUID = -2997206437000039422L;

    @Getter @Setter private String teacherName;
    @Getter @Setter private String teacherImgUrl;
    @Getter @Setter private String studentName;
    @Getter @Setter private String studentImgUrl;

    // this method is for web ftl
    @SuppressWarnings("UnusedDeclaration")
    public String getCommentTime() {
        return DateFormatParser.getInstance().format(getCreateDatetime(), "yyyy-MM-dd");
    }

    public static ExHomeworkComment newInstance(HomeworkComment source) {
        if (source == null) {
            return null;
        }
        ExHomeworkComment target = new ExHomeworkComment();
        PropertiesUtils.copyProperties(target, source);
        return target;
    }
}
